package retrieval;

import java.util.ArrayList;

public class OrNode extends BeliefNode {

	public OrNode(ArrayList<QueryNode> c) {
		super(c);
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for (QueryNode child : children) {
			score += Math.log(1 - Math.exp(child.score(docId)));
		}
		return Math.log(1 - Math.exp(score));
	}
}
