package inference_network;

import java.util.*;

public class AndNode extends BeliefNode{
  public AndNode(<? extends QueryNode> children){
    super(children);
  }

  public double score(doc_id){
    double score = 0.0;

    for(int i=0; i<this.children.size(); i++){
      score += this.children[i].score(doc_id);
    }

    return score;
  }
}
