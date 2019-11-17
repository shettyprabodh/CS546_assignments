package inference_network;

import java.util.*;
import index.*;

public class InferenceNetwork{

  public ArrayList<PairDoubleInteger> runQuery(QueryNode query_node, int k, int last_doc_id){
    PriorityQueue<PairDoubleInteger> R = new PriorityQueue<PairDoubleInteger>();

    while(query_node.hasMore()){
      int current_doc_id = query_node.nextCandidate();
      double score = query_node.score(current_doc_id);
      R.add(new PairDoubleInteger(score, current_doc_id));

      while(R.size() > k){
        R.poll();
      }

      query_node.skipTo(current_doc_id+1);
    }

    ArrayList<PairDoubleInteger> result = new ArrayList<PairDoubleInteger>();
    Stack st = new Stack();

    while(R.size() > 0){
      PairDoubleInteger temp = R.poll();
      st.push(temp);
    }

    while(st.size() > 0){
      result.add((PairDoubleInteger)st.pop());
    }

    return result;
  }
}
