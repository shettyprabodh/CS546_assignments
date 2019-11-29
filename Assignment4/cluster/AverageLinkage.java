package cluster;

import java.util.*;
import index.*;

public class AverageLinkage implements LinkageInterface {
  public double score(DocumentVector doc_vec, ArrayList<DocumentVector> cluster_doc_vecs){
    Double score = 0.0;

    for(DocumentVector cluster_doc_vec: cluster_doc_vecs){
      Double current_score = doc_vec.dot(cluster_doc_vec);

      score += current_score;
    }

    return score/cluster_doc_vecs.size();
  }
}
