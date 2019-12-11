package retrieval;

import java.util.ArrayList;

public class SumNode extends BeliefNode  {

	public SumNode(ArrayList<QueryNode> c) {
		super(c);
	}

	public SumNode(ArrayList<QueryNode> c, PriorNode p){
		super(c,p);
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for (QueryNode child : children) {
			score += Math.exp(child.score(docId));
		}
		return Math.log(score/children.size()) + priorScore(docId);
	}
}
