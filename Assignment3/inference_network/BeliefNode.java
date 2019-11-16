package inference_network;

import index.*;

public class BeliefNode extends QueryNode{
  ArrayList<? extends QueryNode> children;

  public BeliefNode(ArrayList<? extends QueryNode> children){
    this.children = children;
  }

  public double score(int doc_id){
    return;
  }

  public double hasMoreDocuments(){
    return;
  }
}
