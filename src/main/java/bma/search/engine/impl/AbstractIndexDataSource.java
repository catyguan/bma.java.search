package bma.search.engine.impl;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import bma.search.engine.IndexDataSource;
import bma.search.engine.impl.IndexAction.TYPE;

public abstract class AbstractIndexDataSource<DATA_TYPE> implements
		IndexDataSource {

	final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(AbstractIndexDataSource.class);

	private String id;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void build(IndexWriter writer, boolean fullSynch, String param)
			throws Exception {
		if (fullSynch) {
			if (log.isInfoEnabled()) {
				log.info("delete all data");
			}
			deleteAllData(writer, param);
			if (log.isInfoEnabled()) {
				log.info("rebuild all data");
			}
			buildAllData(writer, param);
		} else {
			if (log.isInfoEnabled()) {
				log.info("build modify data");
			}
			buildModifyData(writer, param);
		}
	}

	/**
	 * 删除原有的所有数据
	 * 
	 * @param writer
	 * @param param
	 * @throws Exception
	 */
	protected abstract void deleteAllData(IndexWriter writer, String param)
			throws Exception;

	/**
	 * 构建全部的数据
	 * 
	 * @param writer
	 * @param param
	 * @throws Exception
	 */
	protected void buildAllData(IndexWriter writer, String param)
			throws Exception {
		List<DATA_TYPE> list = null;
		for (int i = 0;; i++) {
			if (log.isInfoEnabled()) {
				log.info("queryAllData({},{})", i, param);
			}
			list = queryAllData(i, param);
			if (list == null || list.isEmpty()) {
				if (log.isInfoEnabled()) {
					log.info("queryAllData end at {}", i);
				}
				break;
			}
			for (DATA_TYPE data : list) {
				Document doc = convertDocument(data, param);
				if (doc == null) {
					if (log.isDebugEnabled()) {
						log.debug("skip data - {}", data);
					}
					continue;
				}
				if (log.isDebugEnabled()) {
					log.debug("add data - {}", data);
				}
				writer.addDocument(doc);
			}
		}
	}

	protected abstract List<DATA_TYPE> queryAllData(int pos, String param);

	protected abstract Document convertDocument(DATA_TYPE data, String param);

	/**
	 * 
	 * @param writer
	 * @param param
	 * @throws Exception
	 */
	protected void buildModifyData(IndexWriter writer, String param)
			throws Exception {
		IndexReader r = IndexReader.open(writer, false);
		IndexSearcher s = new IndexSearcher(r);
		try {
			List<IndexAction<DATA_TYPE>> list = null;
			for (int i = 0;; i++) {
				if (log.isInfoEnabled()) {
					log.info("queryModifyData({},{})", i, param);
				}
				list = queryModifyData(i, param);
				if (list == null || list.isEmpty()) {
					if (log.isInfoEnabled()) {
						log.info("queryModifyData end at {}", i);
					}
					break;
				}

				for (IndexAction<DATA_TYPE> action : list) {
					DATA_TYPE data = action.getData();
					TYPE type = action.getType();
					Term term = null;
					if (type.equals(TYPE.DELETE)) {
						term = getDataTerm(data);
						writer.deleteDocuments(term);
						if (log.isDebugEnabled()) {
							log.debug("delete data - {}", data);
						}
						continue;
					}
					Document doc = convertDocument(data, param);
					if (doc == null) {
						if (log.isDebugEnabled()) {
							log.debug("skip data - {}", data);
						}
						continue;
					}
					if (type.equals(TYPE.SET)) {
						type = TYPE.ADD;
						term = getDataTerm(data);
						if (term != null) {
							TopDocs docs = s.search(new TermQuery(term), 1);
							if (docs.totalHits != 0) {
								type = TYPE.UPDATE;
							}
						}
					}
					if (type.equals(TYPE.ADD)) {
						if (log.isDebugEnabled()) {
							log.debug("add data - {}", data);
						}
						writer.addDocument(doc);
						continue;
					}
					if (type.equals(TYPE.UPDATE)) {
						if (log.isDebugEnabled()) {
							log.debug("update data - {}", data);
						}
						writer.updateDocument(term, doc);
						continue;
					}
				}
			}
		} finally {
			s.close();
			r.close();
		}
	}

	protected abstract List<IndexAction<DATA_TYPE>> queryModifyData(int pos,
			String param);

	protected abstract Term getDataTerm(DATA_TYPE data);

}
