package inference_network;

import java.util.*;

public class FilterRejectNode extends FilterNode{
  public FilterRejectNode(QueryNode child, ArrayList<Integer> doc_ids){
    super(child, doc_ids);
  }

  public double score(int doc_id){
    if(!this.doc_ids.contains(doc_id)){
      return this.child.score(doc_id);
    }
    else{
      return -Double.MAX_VALUE;
    }
  }
}
