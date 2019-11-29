package cluster;

import java.util.*;
import index.*;

public class SingleLinkage implements LinkageInterface {
  public double score(DocumentVector doc_vec, ArrayList<DocumentVector> cluster_doc_vecs){
    Double min_score = Double.MAX_VALUE;

    for(DocumentVector cluster_doc_vec: cluster_doc_vecs){
      Double current_score = doc_vec.dot(cluster_doc_vec);

      min_score = Math.min(min_score, current_score);
    }

    return min_score;
  }
}
