package bma.search.engine;

import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;

import bma.search.engine.api.SearchItem;
import bma.search.engine.api.SearchResult;
import bma.search.engine.api.SearchService.Client;
import bma.search.engine.boot.ThriftServer;

public class SearchServerTest {

	@Test
	public void testServer() throws Exception {
		ThriftServer.runServer();
	}

	@Test
	public void testClient() throws Exception {
		SearchResult r = invoke("act", 1, 10);
		if ("ok".equals(r.getMsg())) {
			System.out.println(r.getTotal());
			List<SearchItem> l = r.getResult();
			for (SearchItem m : l) {
				System.out.println(m.getData());
			}
		} else {
			System.err.println("ERROR:" + r.getMsg());
		}
	}

	public SearchResult invoke(String query, int page, int pageSize)
			throws Exception {
		TTransport transport = null;
		try {
			transport = new TSocket("localhost", 1234);
			TProtocol protocol = new TBinaryProtocol(transport);
			Client client = new Client(protocol);
			transport.open();
			return client.search(query, page, pageSize);
		} finally {
			if (transport != null)
				transport.close();
		}
	}
}
