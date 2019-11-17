package inference_network;

import index.*;
import java.util.*;

public class BeliefNode extends QueryNode{
  ArrayList<? extends QueryNode> children;

  public BeliefNode(ArrayList<? extends QueryNode> children){
    this.children = children;
  }

  public double score(int doc_id){
    return 0.0;
  }

  public boolean hasMoreDocuments(){
    return false;
  }
}
