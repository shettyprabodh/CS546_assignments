package retrieval;

import java.util.ArrayList;

/**
 * Evidence combination (belief) nodes
 *
 */
public abstract class BeliefNode implements QueryNode {
	protected ArrayList<QueryNode> children;

	public BeliefNode(ArrayList<QueryNode> c) {
		children = c;
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
}

