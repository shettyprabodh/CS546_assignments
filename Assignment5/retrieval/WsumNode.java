package retrieval;

import java.util.ArrayList;

public class WsumNode extends BeliefNode {

	ArrayList<Double> wts = null;

	public WsumNode(ArrayList<QueryNode> c, ArrayList<Double> weights) {
		super(c);
		wts = weights;
	}

	public WsumNode(ArrayList<QueryNode> c, ArrayList<Double> weights, PriorNode p){
		super(c,p);
		wts = weights;
	}

	@Override
	public Double score(Integer docId) {
		Double score = 0.0;
		Double sum_wts = 0.0;
		for (int i = 0; i < children.size(); i++){
			Double cur_wt = wts.get(i);
			score += cur_wt * Math.exp(children.get(i).score(docId));
			sum_wts += cur_wt;
		}
		return Math.log(score/sum_wts) + priorScore(docId);
	}
}
