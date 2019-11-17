package inference_network;

import java.util.*;

public class NotNode extends BeliefNode{
  public NotNode(ArrayList<? extends QueryNode> children){
    super(children);
  }

  public double score(int doc_id){
    // Returns not of first children
    return Math.log(1-Math.exp(this.children.get(0).score(doc_id)));
  }
}
