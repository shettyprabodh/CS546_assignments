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

    SingleLinkage min_linkage = new SingleLinkage();
    Clusters shakespeare_clusters = new Clusters(doc_vec_map, 0.5, min_linkage);
    shakespeare_clusters.buildClusters();
    shakespeare_clusters.writeToFile();
  }
}
