package retrieval;

import index.Index;

public class JelinikMercer extends RetrievalModel {
	private double lambda;
	
	public JelinikMercer(Index ind, double lambda) {
		this.lambda = lambda;
		this.index = ind;
		this.collSize = index.getCollectionSize();
	}

	@Override
	public double scoreOccurrence(int tf, int ctf, int docLen) {
		return Math.log((1 - lambda) * (tf*1.0/docLen) + lambda * ctf/collSize);	
	}	
}	
