package retrieval;

import java.util.ArrayList;

public class OrNode extends BeliefNode {

	public OrNode(ArrayList<QueryNode> c) {
		super(c);
	}

	public OrNode(ArrayList<QueryNode> c, PriorNode p){
		super(c,p);
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for (QueryNode child : children) {
			score += Math.log(1 - Math.exp(child.score(docId)));
		}
		return Math.log(1 - Math.exp(score)) + priorScore(docId);
	}
}
