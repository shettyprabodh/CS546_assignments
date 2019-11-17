package inference_network;

import java.util.*;
import index.*;

public class InferenceNetwork{

  public static ArrayList<PairDoubleInteger> runQuery(QueryNode query_node, int k, int last_doc_id){
    PriorityQueue<PairDoubleInteger> R = new PriorityQueue<PairDoubleInteger>();

    for(int doc_id=0; doc_id<last_doc_id; doc_id++){
      double score = query_node.score(doc_id);

      R.add(new PairDoubleInteger(score, doc_id));

      while(R.size() > k){
        R.poll();
      }
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
