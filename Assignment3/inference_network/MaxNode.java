package inference_network;

import java.util.*;

public class MaxNode extends BeliefNode{
  public MaxNode(ArrayList<? extends QueryNode> children){
    super(children);
  }

  public double score(int doc_id){
    double max_score = -Double.MAX_VALUE;

    // Since log is an increasing function, no need to convert to prob. scale
    for(int i=0; i<this.children.size(); i++){
      max_score = Math.max(max_score, this.children.get(i).score(doc_id));
    }

    return max_score;
  }
}
