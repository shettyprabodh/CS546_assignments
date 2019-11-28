package inference_network;

import java.util.*;

public class SumNode extends BeliefNode{
  public SumNode(ArrayList<? extends QueryNode> children){
    super(children);
  }

  public double score(int doc_id){
    double score = 0.0;

    for(int i=0; i<this.children.size(); i++){
      score += Math.exp(this.children.get(i).score(doc_id));
    }
    score /= (this.children.size());

    return Math.log(score);
  }
}
