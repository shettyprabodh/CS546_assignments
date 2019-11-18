import java.util.*;
import pre_processors.*;
import inference_network.*;
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

    // Destroying current indices
    shakespeare_index = null;

    System.out.println("====================== Creating new indices with just lookup table ======================");
    InvertedIndex new_shakespeare_index = new InvertedIndex(index_bin_name, lookup_table_name, data_statistics_name);
    new_shakespeare_index.loadLookupTable();
    System.out.println("====================== Loaded lookup tables ======================");

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
    ArrayList<TermNode> children;
    String outfile;
    String run_id;
    int q_num;
    QueryNode query_node;
    ArrayList<PairDoubleInteger> results;
    InferenceNetwork network = new InferenceNetwork();

    // Ordered
    outfile = "ow.trecrun";
    run_id = "pshetty-ow-dir-1500";
    q_num = 0;
    for(String query : queries){
      q_num++;
      children = TopLevelClass.generateTermNodes(query, new_shakespeare_index);
      int window_size = children.size();
      query_node = new OrderedWindowNode(children, window_size, new_shakespeare_index);
      results = network.runQuery(query_node, 10, new_shakespeare_index.getLastDocID());
      ArrayList<String> TREC_formatted_strings = new ArrayList<String>();
      for(int j=1; j<=results.size(); j++){
        String scene_id = new_shakespeare_index.getSceneIdFromDocId(results.get(j-1).getB());
        TREC_formatted_strings.add(TopLevelClass.getTRECFormattedString(q_num, scene_id, j, results.get(j-1).getA(), run_id));
      }

      TopLevelClass.writeTRECStringsToFile(outfile, TREC_formatted_strings);
    }
  }

  public static ArrayList<TermNode> generateTermNodes(String query, InvertedIndex index){
    String[] terms = query.split(" ");
    ArrayList<TermNode> result = new ArrayList<TermNode>();

    for(String term: terms){
      result.add(new TermNode(term, index));
    }

    return result;
  }

  public static String getTRECFormattedString(int question_number, String scene_id, int rank, double score, String run_id){
    String result = "";

    result += "Q" + question_number + " ";
    result += "skip ";
    result += scene_id + " ";
    result += rank + " ";
    result += score + " ";
    result += run_id;

    return result;
  }

  public static void writeTRECStringsToFile(String file_name, ArrayList<String> TREC_formatted_strings){
    try(FileWriter file = new FileWriter(file_name, true)){
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
