package retrieval;

import java.util.ArrayList;

public class SumNode extends BeliefNode  {

	public SumNode(ArrayList<QueryNode> c) {
		super(c);
	}
	
	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for (QueryNode child : children) {
			score += Math.exp(child.score(docId));
		}
		return Math.log(score/children.size());
	}
}
