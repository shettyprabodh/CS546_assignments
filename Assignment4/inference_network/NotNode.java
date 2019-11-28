package inference_network;

import java.util.*;
// All operations of NotNode are on first child, disregarding other children.
public class NotNode extends BeliefNode{
  public NotNode(ArrayList<? extends QueryNode> children){
    super(children);
  }

  public int nextCandidate(){
    return this.children.get(0).nextCandidate();
  }

  public void skipTo(int doc_id){
    this.children.get(0).skipTo(doc_id);
  }

  public boolean hasMore(){
    return this.children.get(0).hasMore();
  }

  public double score(int doc_id){
    // Returns not of first children
    return Math.log(1-Math.exp(this.children.get(0).score(doc_id)));
  }
}
