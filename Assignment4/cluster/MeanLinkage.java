package cluster;

import java.util.*;
import index.*;

public class MeanLinkage implements LinkageInterface {
  public double score(DocumentVector doc_vec, ArrayList<DocumentVector> cluster_doc_vecs){
    Double score = 0.0;
    DocumentVector centroid = new DocumentVector();

    for(DocumentVector cluster_doc_vec: cluster_doc_vecs){
      centroid = centroid.add(cluster_doc_vec);
    }
    centroid = centroid.scale((1.0/cluster_doc_vecs.size()));

    score = centroid.dot(doc_vec);

    return score;
  }
}
