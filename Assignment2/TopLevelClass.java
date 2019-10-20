import java.util.*;
import index.*;
import java.nio.*;

public class TopLevelClass{
  public static void main(String[] args){
    String raw_file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(raw_file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");
    String index_bin_name = "index.bin";
    String lookup_table_name = "lookup_table.json";
    String data_statistics_name = "data_statistics.json";

    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.getAllDocuments(), index_bin_name, lookup_table_name, data_statistics_name, "Dirichlet");

    System.out.println("====================== Building indices ======================");
    shakespeare_index.createIndex();
    System.out.println("====================== Indices Built ======================");

    System.out.println("====================== Writing to disk ======================");
    shakespeare_index.write(true);
    System.out.println("====================== Wrote to disk ======================");

    // Destroying current indices
    shakespeare_index = null;

    System.out.println("====================== Creating new indices with just lookup table ======================");
    InvertedIndex new_shakespeare_index = new InvertedIndex(index_bin_name, lookup_table_name, data_statistics_name, "Dirichlet");
    new_shakespeare_index.loadLookupTable();
    System.out.println("====================== Loaded lookup tables ======================");

    System.out.println("Avgdl: " + new_shakespeare_index.getAverageDocumentLength());
    System.out.println("Total word count: " + new_shakespeare_index.getTotalWordCount());
    System.out.println("====================== Querying ======================");
    new_shakespeare_index.getScores("servant guard soldier", 10);
  }
}
