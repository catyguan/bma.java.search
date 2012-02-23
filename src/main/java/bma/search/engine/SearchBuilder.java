package bma.search.engine;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

/**
 * 搜索适配对象<br/>
 * 用于分析搜索语句，构造搜索器，并解析结果
 * 
 * @author guanzohng
 * 
 */
public interface SearchBuilder {

	/**
	 * 分析搜索语句构造搜索器
	 * 
	 * @param query
	 * @param analyzer
	 * @return
	 */
	public Query createQuery(String query, Analyzer analyzer) throws Exception;

	/**
	 * 解析结果
	 * 
	 * @param doc
	 * @return
	 */
	public Map<String, String> convertResult(Document doc) throws Exception;
}
