package bma.search.engine.boot;

import bma.search.engine.SearchService;

public class ThriftServerConfig {

	private int port = 1234;
	private SearchService service;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public SearchService getService() {
		return service;
	}

	public void setService(SearchService service) {
		this.service = service;
	}

}
