package retrieval;

import java.util.ArrayList;

public class AndNode extends BeliefNode {

	public AndNode(ArrayList<QueryNode> c) {
		super(c);
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for(QueryNode child : children){
			score += child.score(docId);
		}
		return score;
	}
}
