import java.util.*;
import pre_processors.*;
import inference_network.*;
import index.*;
import cluster.*;
import java.io.*;

public class TopLevelClass{
  public static void main(String[] args){
    String raw_file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(raw_file_name);
    document_crawler.parseJSON();

    String index_bin_name = "index.bin";
    String lookup_table_name = "lookup_table.json";
    String data_statistics_name = "data_statistics.json";
    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.getAllDocuments());
    shakespeare_index.createIndex();
    shakespeare_index.write(true);
    System.out.println("====================== New Index created and written to disk ======================");

    // Destroying current indices
    shakespeare_index = null;

    System.out.println("====================== Reading index ======================");
    shakespeare_index = new InvertedIndex(index_bin_name, lookup_table_name, data_statistics_name);
    shakespeare_index.loadLookupTable();

    DocumentVectorMap doc_vec_map = shakespeare_index.getDocumentVectorMap();
    // System.out.println("doc_vec for doc_id = 0:- " + doc_vec_map.getDocumentVector(0));
    System.out.println("====================== Loaded document vector map ======================");

    MeanLinkage mean_linkage = new MeanLinkage();
    Clusters shakespeare_clusters = null;

    Integer threshold = 0;

    while(threshold <= 100){
      System.out.println("Creating clusters for threshold=" + new Double(threshold)/100.0);
      shakespeare_clusters = new Clusters(doc_vec_map, new Double(threshold)/100.0, mean_linkage);
      shakespeare_clusters.buildClusters();
      shakespeare_clusters.writeToFile();

      threshold += 5;
    }

    System.out.println("Complete");
  }
}
