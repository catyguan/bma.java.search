package bma.search.engine;

import org.apache.lucene.index.IndexWriter;

/**
 * 索引数据源
 * 
 * @author guanzhong
 * 
 */
public interface IndexDataSource {

	/**
	 * 数据源的标示
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * 构建索引数据
	 * 
	 * @param engine
	 * @param fullSynch
	 * @param param
	 */
	public void build(IndexWriter writer, boolean fullSynch, String param)
			throws Exception;
}
