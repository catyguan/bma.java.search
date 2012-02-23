package bma.search.engine;

import java.util.List;
import java.util.Map;

public class SearchResult {

	private int total;
	private List<Map<String, String>> result;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<Map<String, String>> getResult() {
		return result;
	}

	public void setResult(List<Map<String, String>> result) {
		this.result = result;
	}

}
