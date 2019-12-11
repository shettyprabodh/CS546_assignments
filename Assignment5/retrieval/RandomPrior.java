package retrieval;

import index.Index;

public class RandomPrior extends PriorNode{
	public RandomPrior(Index ind) {
		super(ind);
	}
	public Double score(Integer docId){
    return this.ind.getPriorProbability(docId, "random");
  }
}
