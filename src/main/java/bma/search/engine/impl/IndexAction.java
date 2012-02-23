package bma.search.engine.impl;

public class IndexAction<DATA_TYPE> {

	public static enum TYPE {
		SET, ADD, UPDATE, DELETE
	}

	private TYPE type;
	private DATA_TYPE data;

	public IndexAction() {
		super();
	}

	public IndexAction(TYPE type, DATA_TYPE data) {
		super();
		this.type = type;
		this.data = data;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public DATA_TYPE getData() {
		return data;
	}

	public void setData(DATA_TYPE data) {
		this.data = data;
	}

	public static <DT> IndexAction<DT> set(DT data) {
		return new IndexAction<DT>(TYPE.SET, data);
	}

	public static <DT> IndexAction<DT> add(DT data) {
		return new IndexAction<DT>(TYPE.ADD, data);
	}

	public static <DT> IndexAction<DT> update(DT data) {
		return new IndexAction<DT>(TYPE.UPDATE, data);
	}

	public static <DT> IndexAction<DT> delete(DT data) {
		return new IndexAction<DT>(TYPE.DELETE, data);
	}
}
