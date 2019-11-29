package cluster;

import java.util.*;
import java.io.*;
import index.*;

public class Clusters{
  ArrayList<Cluster> clusters;
  DocumentVectorMap doc_vec_map;
  LinkageInterface linkage;
  Double threshold;

  String base_file_name;

  public Clusters(DocumentVectorMap doc_vec_map, Double threshold, LinkageInterface linkage){
    this.clusters = new ArrayList<Cluster>();
    this.doc_vec_map = doc_vec_map;
    this.linkage = linkage;
    this.threshold = threshold;

    this.base_file_name = "cluster-" + threshold + ".out";
  }

  public ArrayList<Cluster> getClusters(){
    return this.clusters;
  }

  public void buildClusters(){
    Set<Integer> doc_ids = this.doc_vec_map.getDocIds();
    Integer cluster_id = 0;

    for(Integer doc_id: doc_ids){
      DocumentVector doc_vec_to_be_scored = this.doc_vec_map.getDocumentVector(doc_id);
      Double best_score = Double.MIN_VALUE;
      Cluster best_cluster = null;
      // System.out.println("Scoring doc:- " + doc_id);
      for(Cluster cluster: this.clusters){
        Double score = cluster.score(doc_vec_to_be_scored, doc_vec_map, this.linkage);

        if(best_score < score){
          best_score = score;
          best_cluster = cluster;
        }
      }
      if(best_cluster != null && best_score > this.threshold){
        best_cluster.addDocId(doc_id);
      }
      else{
        Cluster new_cluster = new Cluster(cluster_id++);
        new_cluster.addDocId(doc_id);
        this.clusters.add(new_cluster);
      }
    }
  }

  public void writeToFile(){
    String string_to_write = "";

    for(Cluster cluster: this.clusters){
      ArrayList<Integer> doc_ids = cluster.getDocIds();
      for(Integer doc_id: doc_ids){
        string_to_write += cluster.getId() + " " + doc_id + "\n";
      }
    }

    try(FileWriter file = new FileWriter(this.base_file_name)){
      file.write(string_to_write);
      file.flush();
      file.close();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  @Override
  public String toString(){
    String result = "";
    result += "\n{cluster-doc_count-map: ";

    for(int i=0; i<clusters.size(); i++){
      result += i + " : " + clusters.get(i).getDocIdCount() + "\n";
    }
    result += "}\n";
    return result;
  }
}
