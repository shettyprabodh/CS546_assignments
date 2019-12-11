package retrieval;

import java.util.ArrayList;

public class NotNode extends BeliefNode{

	public NotNode(ArrayList<QueryNode> c) {
		super(c);
	}

	public NotNode(ArrayList<QueryNode> c, PriorNode p){
		super(c,p);
	}

	@Override
	public Double score(Integer docId) {
		return Math.log(1 - Math.exp(children.get(0).score(docId))) + priorScore(docId);
	}
}
