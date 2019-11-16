import java.util.*;
import pre_processors.*;
import index.*;
import java.io.*;

public class TopLevelClass{
  public static void main(String[] args){
    String raw_file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(raw_file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");
    String index_bin_name = "index.bin";
    String lookup_table_name = "lookup_table.json";
    String data_statistics_name = "data_statistics.json";
    //
    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.getAllDocuments());

    System.out.println("====================== Building indices ======================");
    shakespeare_index.createIndex();
    System.out.println("====================== Indices Built ======================");

    System.out.println("====================== Writing to disk ======================");
    shakespeare_index.write(true);
    System.out.println("====================== Wrote to disk ======================");
    //
    // // Destroying current indices
    // shakespeare_index = null;
    //
    // System.out.println("====================== Creating new indices with just lookup table ======================");
    // InvertedIndex new_shakespeare_index = new InvertedIndex(index_bin_name, lookup_table_name, data_statistics_name);
    // new_shakespeare_index.loadLookupTable();
    // System.out.println("====================== Loaded lookup tables ======================");
    //
    // System.out.println("Avgdl: " + new_shakespeare_index.getAverageDocumentLength());
    // System.out.println("Total word count: " + new_shakespeare_index.getTotalWordCount());
    // System.out.println("====================== Querying ======================");

  }

  public static String getTRECFormattedString(int question_number, String scene_id, int rank, double score, String retrieval_model_name){
    String result = "";

    result += "Q" + question_number + " ";
    result += "skip ";
    result += scene_id + " ";
    result += rank + " ";
    result += score + " ";

    if(retrieval_model_name == "BM25")
      result += "pshetty-bm25-1.2-700-0.75";
    else if(retrieval_model_name == "JelinikMercer"){
      result += "pshetty-ql-jm-0.1";
    }
    else if(retrieval_model_name == "Dirichlet"){
      result += "pshetty-ql-dir-1500";
    }
    else{
      // Default retrieval model is count based
      result += "pshetty-count";
    }

    return result;
  }

  public static void writeTRECStringsToFile(String file_name, ArrayList<String> TREC_formatted_strings, String[] queries){
    try(FileWriter file = new FileWriter(file_name)){
      file.write("Queries:\n");
      for(int i=0; i<queries.length; i++){
        file.write(queries[i]+"\n");
      }

      file.write("Results:\n");
      for(int i=0; i<TREC_formatted_strings.size(); i++){
        file.write(TREC_formatted_strings.get(i) + "\n");
      }
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }
}
