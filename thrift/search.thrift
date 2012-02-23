namespace java bma.search.engine.api

/** 搜索结果项 */
struct SearchItem {
	/** 结果项的数据 */
	1:map<string,string> data,
}

/** 搜索结果 */
struct SearchResult {
	/** 结果说明，=ok表示成功，其他为错误说明 */
	1:string msg,
	/** 符合结果的总数 */
	2:i32 total,
	/** 游戏模糊姓名的列表 */
	3:list<SearchItem> result,
}

/** 搜索服务 */
service SearchService {
	
	/**
	 * 搜索
	 */
	SearchResult search(1:string query,2:i32 page,3:i32 pageSize);
	
	/**
	 * 同步数据源
	 */
	bool synchDatabase(1:string id,2:bool fullSynch,3:string param);	

}
