package retrieval;

import java.util.ArrayList;

public class AndNode extends BeliefNode {

	public AndNode(ArrayList<QueryNode> c) {
		super(c);
	}

	public AndNode(ArrayList<QueryNode> c, PriorNode p){
		super(c,p);
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for(QueryNode child : children){
			score += child.score(docId);
		}
		return score + priorScore(docId);
	}
}
