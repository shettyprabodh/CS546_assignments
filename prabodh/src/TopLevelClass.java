import java.util.*;
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

    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.getAllDocuments(), index_bin_name, lookup_table_name, data_statistics_name);

    System.out.println("====================== Building indices ======================");
    shakespeare_index.createIndex();
    System.out.println("====================== Indices Built ======================");

    System.out.println("====================== Writing to disk ======================");
    shakespeare_index.write(true);
    System.out.println("====================== Wrote to disk ======================");

    // Destroying current indices
    shakespeare_index = null;

    System.out.println("====================== Creating new indices with just lookup table ======================");
    InvertedIndex new_shakespeare_index = new InvertedIndex(index_bin_name, lookup_table_name, data_statistics_name);
    new_shakespeare_index.loadLookupTable();
    System.out.println("====================== Loaded lookup tables ======================");

    System.out.println("Avgdl: " + new_shakespeare_index.getAverageDocumentLength());
    System.out.println("Total word count: " + new_shakespeare_index.getTotalWordCount());
    System.out.println("====================== Querying ======================");
    String[] queries = new String[]{"the king queen royalty",
                                    "servant guard soldier",
                                    "hope dream sleep",
                                    "ghost spirit",
                                    "fool jester player",
                                    "to be or not to be",
                                    "alas",
                                    "alas poor",
                                    "alas poor yorick",
                                    "antony strumpet"};
    String[] retrieval_models = new String[]{"BM25", "JelinikMercer", "Dirichlet"};

    for(String retrieval_model: retrieval_models){
      ArrayList<String> TREC_formatted_strings = new ArrayList<String>();

      for(int i=0; i<queries.length; i++){
        String query = queries[i];

        ArrayList<PairDoubleInteger> results = new_shakespeare_index.getScores(query, 10, retrieval_model);

        for(int j=1; j<=results.size(); j++){
          String scene_id = new_shakespeare_index.getSceneIdFromDocId(results.get(j-1).getB());
          TREC_formatted_strings.add(TopLevelClass.getTRECFormattedString(i+1, scene_id, j, results.get(j-1).getA(), retrieval_model));
        }
      }

      String file_name = "";
      if(retrieval_model == "BM25"){
        file_name = "bm25.trecrun";
      }
      else if(retrieval_model == "JelinikMercer"){
        file_name = "ql-jm.trecrun";
      }
      else if(retrieval_model == "Dirichlet"){
        file_name = "ql-dir.trecrun";
      }
      else {
        file_name = "count.trecrun";
      }

      TopLevelClass.writeTRECStringsToFile(file_name, TREC_formatted_strings, queries);
    }

    System.out.println("=============================== Results for query \"setting the scene\"===============================" );
    ArrayList<PairDoubleInteger> results = new_shakespeare_index.getScores("setting the scene", 10, "BM25");
    System.out.println("BM2: " + results.toString());
    results = new_shakespeare_index.getScores("setting the scene", 10, "JelinikMercer");
    System.out.println("JelinikMercer: " + results.toString());
    results = new_shakespeare_index.getScores("setting the scene", 10, "Dirichlet");
    System.out.println("Dirichlet: " + results.toString());
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
