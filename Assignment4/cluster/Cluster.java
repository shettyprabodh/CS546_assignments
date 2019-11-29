package cluster;

import java.util.*;
import index.*;

public class Cluster{
  private Integer id;
  private ArrayList<Integer> doc_ids;

  public Cluster(Integer id){
    this.id = id;
    this.doc_ids = new ArrayList<Integer>();
  }


  // Single doc_vec operations
  public void addDocId(Integer doc_id){
    this.doc_ids.add(doc_id);
  }

  public Double score(DocumentVector doc_vec, DocumentVectorMap doc_vec_map, LinkageInterface linkage){
    if(doc_vec == null){
      return 0.0;
    }

    ArrayList<DocumentVector> cluster_doc_vecs = new ArrayList<DocumentVector>();

    for(Integer doc_id: this.doc_ids){
      cluster_doc_vecs.add(doc_vec_map.getDocumentVector(doc_id));
    }

    return linkage.score(doc_vec, cluster_doc_vecs);
  }


  // Get operations
  public ArrayList<Integer> getDocIds(){
    return this.doc_ids;
  }

  public Integer getDocIdCount(){
    return this.doc_ids.size();
  }

  public Integer getId(){
    return this.id;
  }
}
