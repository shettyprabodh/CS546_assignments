package inference_network;

import java.util.*;

public class WandNode extends BeliefNode{
  ArrayList<Double> weights;

  // Assumes size of weights == size of children
  public WandNode(ArrayList<? extends QueryNode> children, ArrayList<Double> weights){
    super(children);

    assert(weights.size() == children.size());
    this.weights = weights;
  }

  public double score(int doc_id){
    Double score = 0.0;

    for(int i=0; i<this.children.size(); i++){
      score += (this.weights.get(i))*(this.children.get(i).score(doc_id));
    }

    return score;
  }
}
