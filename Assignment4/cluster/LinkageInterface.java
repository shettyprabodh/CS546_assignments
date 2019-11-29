package cluster;

import java.util.*;
import index.DocumentVector;

public interface LinkageInterface {
  public double score(DocumentVector doc_vec, ArrayList<DocumentVector> cluster_doc_vecs);
}
