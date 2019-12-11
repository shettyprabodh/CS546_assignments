package retrieval;

public class FilterReject extends FilterOperator {

	public FilterReject(QueryNode proximityExp, QueryNode q) {
		super(proximityExp, q);
	}

	@Override
	public Integer nextCandidate() {
		return query.nextCandidate();	
	}
	
	@Override
	public Double score(Integer docId) {
		if (filter.skipTo(docId) != docId)
			return query.score(docId);
		else
			return null;
	}
}
