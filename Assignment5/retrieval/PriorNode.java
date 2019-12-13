package retrieval;

import index.Index;

public class PriorNode implements QueryNode{
  Index ind;
  String priorName;

	public PriorNode(Index ind, String priorName) {
		this.ind = ind;
    this.priorName = priorName;
	}

  public Double score(Integer docId){
    return this.ind.getPriorProbability(docId, this.priorName);
  }

  // Dummy functions. Should never be used with PriorNode
  public int skipTo(int docId){
    return 0;
  }

  public boolean hasMore(){
    return false;
  }

  public Integer nextCandidate(){
    return 0;
  }
}
