package bma.search.engine.boot;

import java.util.TimerTask;

import bma.search.engine.SearchService;

public class IndexTimer extends TimerTask {

	private SearchService service;
	private String id;
	private boolean fullSynch;
	private String param;

	public SearchService getService() {
		return service;
	}

	public void setService(SearchService service) {
		this.service = service;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isFullSynch() {
		return fullSynch;
	}

	public void setFullSynch(boolean fullSynch) {
		this.fullSynch = fullSynch;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(IndexTimer.class);

	@Override
	public void run() {
		if (log.isInfoEnabled()) {
			log.info("start index timer");
		}
		service.index(id, fullSynch, param);
	}

}
