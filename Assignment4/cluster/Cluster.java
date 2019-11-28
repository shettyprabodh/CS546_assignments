package cluster;

import java.util.*;
import index.*;

public class Cluster{
  private ArrayList<Integer> doc_ids;

  public Cluster(){
    this.doc_ids = new ArrayList<Integer>();
  }


  // Single doc_vec operations
  public void addDocId(Integer doc_id){
    this.doc_ids.add(doc_id);
  }

  public Double score(DocumentVector doc_vec){
    return 1.0
  }


  // Get operations
  public ArrayList<Integer> getDocIds(){
    return this.doc_ids;
  }
}
