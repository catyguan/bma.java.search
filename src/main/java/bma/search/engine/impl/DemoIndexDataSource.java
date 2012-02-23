package bma.search.engine.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import bma.search.engine.impl.DemoIndexDataSource.DemoData;
import bma.search.engine.impl.IndexAction.TYPE;

public class DemoIndexDataSource extends AbstractIndexDataSource<DemoData> {

	final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DemoIndexDataSource.DemoData.class);

	public static class DemoData {
		private int id;
		private String name;
		private int refId;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getRefId() {
			return refId;
		}

		public void setRefId(int refId) {
			this.refId = refId;
		}

		@Override
		public String toString() {
			return "DemoData[" + id + "]";
		}

	}

	public final static String TYPE_FIELD_NAME = "data_type";
	public final static String TYPE_VALUE = "demo";

	private JdbcTemplate jdbcTemplate;
	private int pageSize = 100;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	protected void deleteAllData(IndexWriter writer, String param)
			throws Exception {
		// writer.deleteAll();
		Query query = new TermQuery(new Term(TYPE_FIELD_NAME, TYPE_VALUE));
		writer.deleteDocuments(query);
	}

	@Override
	protected List<DemoData> queryAllData(int pos, String param) {
		int s = pos * pageSize;
		String sql = "SELECT * FROM activity_info ORDER BY id LIMIT " + s + ","
				+ pageSize;
		return jdbcTemplate.query(sql, new RowMapper<DemoData>() {
			@Override
			public DemoData mapRow(ResultSet rs, int row) throws SQLException {
				DemoData r = new DemoData();
				r.setId(rs.getInt("id"));
				r.setName(rs.getString("actName"));
				r.setRefId(rs.getInt("game_id"));
				return r;
			}
		});
	}

	@Override
	protected List<IndexAction<DemoData>> queryModifyData(int pos, String param) {
		int s = pos * pageSize;
		String sql = "SELECT * FROM activity_info ORDER BY id LIMIT " + s + ","
				+ pageSize;
		return jdbcTemplate.query(sql, new RowMapper<IndexAction<DemoData>>() {
			@Override
			public IndexAction<DemoData> mapRow(ResultSet rs, int row)
					throws SQLException {
				DemoData data = new DemoData();
				data.setId(rs.getInt("id"));
				data.setName(rs.getString("actName"));
				data.setRefId(rs.getInt("game_id"));

				IndexAction<DemoData> r = new IndexAction<DemoData>();
				r.setType(TYPE.SET);
				r.setData(data);
				return r;
			}
		});
	}

	@Override
	protected Document convertDocument(DemoData data, String param) {
		Document doc = new Document();
		if (true) {
			Field fd = new Field(TYPE_FIELD_NAME, TYPE_VALUE, Field.Store.YES,
					Field.Index.NOT_ANALYZED);
			doc.add(fd);
		}
		if (true) {
			Field fd = new Field("termId", TYPE_VALUE + "_" + data.getId(),
					Field.Store.NO, Field.Index.NOT_ANALYZED);
			doc.add(fd);
		}
		if (true) {
			Field fd = new Field("id", Integer.toString(data.getId()),
					Field.Store.YES, Field.Index.NO);
			doc.add(fd);
		}
		if (true) {
			Field fd = new Field("name", data.getName(), Field.Store.YES,
					Field.Index.ANALYZED,
					Field.TermVector.WITH_POSITIONS_OFFSETS);
			doc.add(fd);
		}
		if (true) {
			Field fd = new Field("refId", Integer.toString(data.getRefId()),
					Field.Store.YES, Field.Index.NO);
			doc.add(fd);
		}
		return doc;
	}

	@Override
	protected Term getDataTerm(DemoData data) {
		return new Term("termId", TYPE_VALUE + "_" + data.getId());
	}

}
