package retrieval;

import index.Index;

public class UniformPrior extends PriorNode{
	public UniformPrior(Index ind) {
		super(ind);
	}
	public Double score(Integer docId){
    return this.ind.getPriorProbability(docId, "uniform");
  }
}
