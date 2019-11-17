package inference_network;

import index.*;
import java.util.*;

public class BeliefNode extends QueryNode{
  ArrayList<? extends QueryNode> children;

  public BeliefNode(ArrayList<? extends QueryNode> children){
    this.children = children;
  }

  public void skipTo(int doc_id){
    return;
  }
}
