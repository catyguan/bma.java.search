package bma.search.engine.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import bma.search.engine.IndexDataSource;
import bma.search.engine.SearchBuilder;
import bma.search.engine.SearchResult;
import bma.search.engine.SearchService;

public class SearchServiceImpl implements SearchService {

	final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(SearchServiceImpl.class);

	/**
	 * 执行线程
	 */
	private Executor indexExecutor;
	/**
	 * 相关的数据源
	 */
	private List<IndexDataSource> dataSourceList;
	/**
	 * 相关的搜索构造器
	 */
	private SearchBuilder searchBuilder;

	/**
	 * 索引文件
	 */
	private String file;
	private transient IndexSearcher indexSearcher;

	/**
	 * 字典的位置
	 */
	private String dicHome;
	private transient Analyzer analyzer;

	/**
	 * 优化参数：合并时候的最大段
	 */
	private int maxNumSegments = 1;

	public Executor getIndexExecutor() {
		return indexExecutor;
	}

	public void setIndexExecutor(Executor indexExecutor) {
		this.indexExecutor = indexExecutor;
	}

	public List<IndexDataSource> getDataSourceList() {
		return dataSourceList;
	}

	public void setDataSourceList(List<IndexDataSource> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}

	public int getMaxNumSegments() {
		return maxNumSegments;
	}

	public void setMaxNumSegments(int maxNumSegments) {
		this.maxNumSegments = maxNumSegments;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getDicHome() {
		return dicHome;
	}

	public void setDicHome(String dicHome) {
		this.dicHome = dicHome;
	}

	public SearchBuilder getSearchBuilder() {
		return searchBuilder;
	}

	public void setSearchBuilder(SearchBuilder searchBuilder) {
		this.searchBuilder = searchBuilder;
	}

	public void close() {
		if (indexSearcher != null) {
			try {
				indexSearcher.close();
			} catch (IOException e) {
				log.warn("close IndexReader fail", e);
			}
		}
	}

	public Analyzer sureAnalyzer() {
		synchronized (this) {
			if (analyzer == null) {
				if (dicHome == null || dicHome.isEmpty()) {
					log.error("dic home is null");
					return null;
				}
				File dic = new File(dicHome);
				if (!dic.exists() && !dic.isDirectory()) {
					log.error("wrong dic home - {}", dicHome);
					return null;
				}

				System.setProperty("paoding.dic.home", dicHome);
				analyzer = new PaodingAnalyzer();
			}
		}
		return analyzer;
	}

	@Override
	public boolean index(final String id, final boolean fullSynch,
			final String param) {
		indexExecutor.execute(new Runnable() {

			@Override
			public void run() {
				_index(id, fullSynch, param);
			}
		});
		return true;
	}

	public void _index(String id, boolean fullSynch, String param) {
		try {
			if (log.isInfoEnabled()) {
				log.info("start index task({},{},{})", new Object[] { id,
						fullSynch, param });
			}
			if (dataSourceList == null) {
				log.warn("dataSourceList is empty, return");
				return;
			}
			if (file == null || file.isEmpty()) {
				log.error("index file is null");
				return;
			}
			File fs = new File(file);
			if (fs.exists() && !fs.isDirectory()) {
				log.error("index file is not a directory");
				return;
			}

			Analyzer analyzer = sureAnalyzer();
			if (analyzer == null)
				return;

			if (log.isInfoEnabled()) {
				log.info("open index file - {}", fs);
			}

			// Windows需要修改为SimpleFSDirectory
			Directory indexDir = new NIOFSDirectory(fs);
			IndexWriter writer = new IndexWriter(indexDir,
					new IndexWriterConfig(Version.LUCENE_35, analyzer));
			for (IndexDataSource ds : dataSourceList) {
				if (id != null && !id.isEmpty()) {
					if (!id.equalsIgnoreCase(ds.getId())) {
						continue;
					}
					if (log.isDebugEnabled()) {
						log.debug("skip build dataosource({})", ds);
					}
				}
				if (log.isInfoEnabled()) {
					log.info("build datasource({},{})", id, ds);
				}
				ds.build(writer, fullSynch, param);
			}
			if (log.isInfoEnabled()) {
				log.info("start merge index({})", maxNumSegments);
			}
			long st = System.currentTimeMillis();
			writer.forceMerge(maxNumSegments, true);
			long useTime = System.currentTimeMillis() - st;
			if (log.isInfoEnabled()) {
				log.info("end merge index,use time - {}", useTime);
			}
			writer.close();
			synchronized (this) {
				if (indexSearcher != null) {
					indexSearcher = new IndexSearcher(
							IndexReader.open(indexDir));
				}
			}
		} catch (Exception e) {
			log.error("index(" + id + "," + fullSynch + "," + param + ") fail",
					e);
		}
	}

	@Override
	public SearchResult search(String query, int page, int pageSize)
			throws Exception {
		try {
			if (log.isDebugEnabled()) {
				log.debug("search({},{},{})", new Object[] { query, page,
						pageSize });
			}
			if (searchBuilder == null) {
				log.warn("searchBuilder is empty, return");
				throw new Exception("searchBuilder is empty");
			}
			IndexSearcher searcher = null;
			synchronized (this) {
				if (indexSearcher == null) {
					if (file == null || file.isEmpty()) {
						log.error("index file is null");
						throw new Exception("index file is null");
					}
					File fs = new File(file);
					if (!fs.exists() || !fs.isDirectory()) {
						log.error("index file is not a directory");
						throw new Exception("index file is not a directory");
					}

					if (log.isInfoEnabled()) {
						log.info("open index file - {}", fs);
					}

					// Windows需要修改为SimpleFSDirectory
					Directory indexDir = new NIOFSDirectory(fs);
					indexSearcher = new IndexSearcher(
							IndexReader.open(indexDir));
				}
				searcher = indexSearcher;
			}

			Analyzer analyzer = sureAnalyzer();
			if (analyzer == null)
				throw new Exception("create analyzer fail");
			Query q = searchBuilder.createQuery(query, analyzer);
			if (log.isDebugEnabled()) {
				log.debug("query({} ==> {})", query, q);
			}
			if (q == null) {
				throw new Exception("create query fail");
			}
			long st = System.currentTimeMillis();
			int s = page * pageSize;
			int e = (page + 1) * pageSize;
			TopDocs hits = searcher.search(q, e);
			long useTime = System.currentTimeMillis() - st;
			if (log.isInfoEnabled()) {
				log.info("search use time - {}", useTime);
			}
			List<Map<String, String>> list = new ArrayList<Map<String, String>>(
					pageSize);
			for (int i = s; i < e && i < hits.scoreDocs.length; i++) {
				int docId = hits.scoreDocs[i].doc;
				Document hit = searcher.doc(docId);
				Map<String, String> data = searchBuilder.convertResult(hit);
				if (data != null) {
					list.add(data);
				}
			}
			SearchResult r = new SearchResult();
			r.setTotal(hits.totalHits);
			r.setResult(list);
			return r;
		} catch (Exception e) {
			log.error("search(" + query + "," + page + "," + pageSize
					+ ") fail", e);
			throw e;
		}
	}
}
