package inference_network;

import java.util.*;

public class WsumNode extends BeliefNode{
  ArrayList<Double> weights;
  double weight_sum;

  // Assumes size of weights == size of children
  public WsumNode(ArrayList<? extends QueryNode> children, ArrayList<Double> weights){
    super(children);

    assert(weights.size() == children.size());
    this.weights = weights;
    this.weight_sum = 0.0;
    for(int i=0; i<weights.size(); i++){
      this.weight_sum += (weights.get(i));
    }
  }

  public double score(int doc_id){
    double score = 0.0;

    for(int i=0; i<this.children.size(); i++){
      score += (this.weights.get(i))*(Math.exp(this.children.get(i).score(doc_id)));
    }
    score /= (this.weight_sum);

    return Math.log(score);
  }
}
