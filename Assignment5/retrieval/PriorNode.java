package retrieval;

import index.Index;

public class PriorNode{
  Index ind;

	public PriorNode(Index ind) {
		this.ind = ind;
	}

  public Double score(Integer docId){
    return 0.0;
  }
}
