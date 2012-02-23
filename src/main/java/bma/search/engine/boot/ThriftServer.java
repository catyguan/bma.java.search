package bma.search.engine.boot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import bma.search.engine.api.SearchItem;
import bma.search.engine.api.SearchResult;
import bma.search.engine.api.SearchService.Iface;
import bma.search.engine.api.SearchService.Processor;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class ThriftServer {

	public static void main(String[] args) {
		runServer();
	}

	public static void runServer() {

		FileSystemXmlApplicationContext context;

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

		context = new FileSystemXmlApplicationContext("search.xml");

		final org.slf4j.Logger log = org.slf4j.LoggerFactory
				.getLogger(ThriftServer.class);

		try {

			final ThriftServerConfig cfg = context
					.getBean(ThriftServerConfig.class);

			TServerSocket serverTransport = new TServerSocket(cfg.getPort());

			Processor<Iface> process = new Processor<Iface>(new Iface() {

				@Override
				public SearchResult search(String query, int page, int pageSize)
						throws TException {
					try {
						bma.search.engine.SearchResult sr = cfg.getService()
								.search(query, page, pageSize);
						SearchResult r = new SearchResult();
						r.setMsg("ok");
						r.setTotal(sr.getTotal());
						List<SearchItem> ilist = new ArrayList<SearchItem>(
								pageSize);
						List<Map<String, String>> rlist = sr.getResult();
						for (Map<String, String> data : rlist) {
							SearchItem item = new SearchItem();
							item.setData(data);
							ilist.add(item);
						}
						r.setResult(ilist);
						return r;
					} catch (Exception e) {
						log.warn("search fail", e);
						return new SearchResult(e.toString(), 0, null);
					}
				}

				@Override
				public boolean synchDatabase(String id, boolean fullSynch,
						String param) throws TException {
					try {
						return cfg.getService().index(id, fullSynch, param);
					} catch (Exception e) {
						log.warn("index fail", e);
						return false;
					}
				}

			});

			Factory portFactory = new TBinaryProtocol.Factory(true, true);

			Args args = new Args(serverTransport);
			args.processor(process);
			args.protocolFactory(portFactory);

			TServer server = new TThreadPoolServer(args);
			log.debug("server start");
			server.serve();

		} catch (TTransportException e) {
			e.printStackTrace();
		} finally {
			if (context != null)
				context.close();
		}
	}
}
