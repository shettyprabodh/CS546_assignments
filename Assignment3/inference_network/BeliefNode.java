package inference_network;

import index.*;
import java.util.*;

public class BeliefNode extends QueryNode{
  ArrayList<? extends QueryNode> children;

  public BeliefNode(ArrayList<? extends QueryNode> children){
    this.children = children;
  }

  public int nextCandidate(){
    int next_candidate = Integer.MAX_VALUE;

    for(int i=0; i<this.children.size(); i++){
      // Without this check leads to infinite loop
      if(this.children.get(i).hasMore()){
        next_candidate = Math.min(next_candidate, this.children.get(i).nextCandidate());
      }
    }

    return next_candidate;
  }

  public void skipTo(int doc_id){
    for(int i=0; i<this.children.size(); i++){
      this.children.get(i).skipTo(doc_id);
    }
  }

  public boolean hasMore(){
    for(int i=0; i<this.children.size(); i++){
      if(this.children.get(i).hasMore()){
        return true;
      }
    }
    return false;
  }
}
