package retrieval;

import java.util.ArrayList;

/**
 * Evidence combination (belief) nodes
 *
 */
public abstract class BeliefNode implements QueryNode {
	protected ArrayList<QueryNode> children;
	protected PriorNode prior;

	public BeliefNode(ArrayList<QueryNode> c) {
		children = c;
		prior = null;
	}

	public BeliefNode(ArrayList<QueryNode> c, PriorNode p){
		children = c;
		prior = p;
	}

	@Override
	public Integer nextCandidate() {
		int min = Integer.MAX_VALUE;
		for (QueryNode q : children) {
			if (q.hasMore()) {
				min = Math.min(min, q.nextCandidate());
			}
		}
		return min != Integer.MAX_VALUE ? min : null;
	}


	@Override
	public boolean hasMore() {
		for (QueryNode child : children) {
			if (child.hasMore()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int skipTo(int docId) {
		for (QueryNode child : children) {
				child.skipTo(docId);
		}
		return 0;
	}

	// Returns log probability of prior
	public Double priorScore(Integer docId){
		if(prior != null){
			return prior.score(docId);
		}
		else{
			return 0.0;
		}
	}
}
