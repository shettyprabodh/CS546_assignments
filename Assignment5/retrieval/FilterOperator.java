package retrieval;

public abstract class FilterOperator implements QueryNode {
	protected QueryNode query = null;
	protected QueryNode filter;

	public FilterOperator(QueryNode proximityExp, QueryNode q) {
		filter = proximityExp;
		query = q;
	}
	@Override
	public boolean hasMore() {
		return query.hasMore();
	}

	@Override
	public int skipTo(int docId) {
		filter.skipTo(docId);
		query.skipTo(docId);
		return 0;
	}
}
