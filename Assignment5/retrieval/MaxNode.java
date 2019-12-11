package retrieval;

import java.util.ArrayList;

public class MaxNode extends BeliefNode{

	public MaxNode(ArrayList<QueryNode> c) {
		super(c);
	}

	public MaxNode(ArrayList<QueryNode> c, PriorNode p){
		super(c,p);
	}

	@Override
	public Double score(Integer docId) {
		Double score = (-1.0) *  Double.MAX_VALUE;
		for (QueryNode child : children) {
			score = Math.max(score, child.score(docId));
		}
		return score + priorScore(docId);
	}
}
