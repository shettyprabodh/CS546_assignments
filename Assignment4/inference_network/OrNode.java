package inference_network;

import java.util.*;

public class OrNode extends BeliefNode{
  public OrNode(ArrayList<? extends QueryNode> children){
    super(children);
  }

  public double score(int doc_id){
    double total_probability = 1.0;

    // Product of (1-p_i)
    for(int i=0; i<this.children.size(); i++){
      total_probability *= (1 - Math.exp(this.children.get(i).score(doc_id)));
    }

    return Math.log(1-total_probability);
  }
}
