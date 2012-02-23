package bma.search.engine;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import bma.search.engine.impl.SearchServiceImpl;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class SearchServiceTest {

	FileSystemXmlApplicationContext context;

	@Before
	public void setUp() throws Exception {
		if (true) {
			String type = "org.springframework";
			String level = "INFO";
			LoggerContext lc = (LoggerContext) LoggerFactory
					.getILoggerFactory();
			ch.qos.logback.classic.Logger log = lc.getLogger(type);
			log.setLevel(Level.valueOf(level));
		}

		if (true) {
			String type = "com.mchange.v2";
			String level = "INFO";
			LoggerContext lc = (LoggerContext) LoggerFactory
					.getILoggerFactory();
			ch.qos.logback.classic.Logger log = lc.getLogger(type);
			log.setLevel(Level.valueOf(level));
		}

		context = new SpringTestcaseUtil.ApplicationContextBuilder().project(
				"src/test/java/bma/search/engine/search.xml").build();
	}

	@After
	public void tearDown() throws Exception {
		if (context != null)
			context.close();
	}

	@Test
	public void testIndex_AllFull() {
		SearchServiceImpl s = context.getBean(SearchServiceImpl.class);
		s._index(null, true, null);
	}

	@Test
	public void testIndex_AllModify() {
		SearchServiceImpl s = context.getBean(SearchServiceImpl.class);
		s._index(null, false, null);
	}

	@Test
	public void testIndex_Query() throws Exception {
		SearchServiceImpl s = context.getBean(SearchServiceImpl.class);
		SearchResult r = s.search("act", 0, 10);
		if (r == null) {
			System.out.println("error");
		}
		System.out.println(r.getTotal());
		List<Map<String, String>> l = r.getResult();
		for (Map<String, String> m : l) {
			System.out.println(m);
		}
	}

}
