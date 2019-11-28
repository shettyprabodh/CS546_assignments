package inference_network;

import java.util.*;

public class FilterNode extends QueryNode{
  QueryNode child;
  ArrayList<Integer> doc_ids;

  public FilterNode(QueryNode child, ArrayList<Integer> doc_ids){
    this.child = child;
    this.doc_ids = doc_ids;
  }

  public int nextCandidate(){
    return this.child.nextCandidate();
  }

  public void skipTo(int doc_id){
    this.child.skipTo(doc_id);
  }

  public boolean hasMore(){
    return this.child.hasMore();
  }
}
