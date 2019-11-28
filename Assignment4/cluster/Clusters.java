package cluster;

import java.util.*;
import index.*;

public class Clusters{
  ArrayList<Cluster> clusters;
  DocumentVectorMap doc_vec_map;
  LinkageInterface linkage;

  public Clusters(DocumentVectorMap doc_vec_map, LinkageInterface linkage){
    this.clusters = new ArrayList<Cluster>();
    this.doc_vec_map = doc_vec_map;
    this.linkage = linkage;
  }
}
