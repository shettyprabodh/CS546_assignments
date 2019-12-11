package retrieval;

import java.util.ArrayList;

public class WandNode extends BeliefNode{

	ArrayList<Double> wts = null;

	public WandNode(ArrayList<QueryNode> c, ArrayList<Double> weights) {
		super(c);
		wts = weights;
	}

	public WandNode(ArrayList<QueryNode> c, ArrayList<Double> weights, PriorNode p){
		super(c,p);
		wts = weights;
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		for (int i = 0; i < children.size(); i++){
			QueryNode child = children.get(i);
			score += wts.get(i) * child.score(docId);
		}
		return score + priorScore(docId);
	}
}
