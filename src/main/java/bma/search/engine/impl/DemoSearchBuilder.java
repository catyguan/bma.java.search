package bma.search.engine.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import bma.search.engine.SearchBuilder;

public class DemoSearchBuilder implements SearchBuilder {

	@Override
	public Query createQuery(String query, Analyzer analyzer) throws Exception {

		String[] queries = { query };
		String[] fields = { "name" };
		return MultiFieldQueryParser.parse(Version.LUCENE_35, queries, fields,
				analyzer);
	}

	@Override
	public Map<String, String> convertResult(Document doc) throws Exception {
		Map<String, String> r = new HashMap<String, String>();
		List<Fieldable> flist = doc.getFields();
		for (Fieldable fo : flist) {
			if (fo instanceof Field) {
				Field f = (Field) fo;
				r.put(f.name(), f.stringValue());
			}
		}
		return r;
	}

}
