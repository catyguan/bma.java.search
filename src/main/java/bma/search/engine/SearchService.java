package bma.search.engine;

/**
 * 搜索服务接口
 * 
 * @author guanzhong
 * 
 */
public interface SearchService {

	public boolean index(String id, boolean fullSynch, String param);

	public SearchResult search(String query, int page, int pageSize)
			throws Exception;
}
