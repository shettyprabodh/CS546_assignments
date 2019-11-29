package cluster;

import java.util.*;
import index.*;

public class CompleteLinkage implements LinkageInterface {
  public double score(DocumentVector doc_vec, ArrayList<DocumentVector> cluster_doc_vecs){
    Double max_score = Double.MIN_VALUE;

    for(DocumentVector cluster_doc_vec: cluster_doc_vecs){
      Double current_score = doc_vec.dot(cluster_doc_vec);

      max_score = Math.max(max_score, current_score);
    }

    return max_score;
  }
}
