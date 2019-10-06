import java.util.*;
import index.Crawler;
import index.*;
import java.nio.*;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class TopLevelClass{
  public static void main(String[] args){
    String raw_file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(raw_file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");
    String[] index_bin_names = new String[]{"index_uncompressed.bin", "index_compressed.bin"};
    String[] lookup_table_names = new String[]{"lookup_table_uncompressed.bin", "lookup_table_compressed.bin"};
    String[] data_statistics_names = new String[]{"data_statistics_uncompressed.bin", "data_statistics_compressed.bin"};

    InvertedIndex shakespeare_uncompressed_index = new InvertedIndex(document_crawler.getAllDocuments(), index_bin_names[0], lookup_table_names[0], data_statistics_names[0]);
    InvertedIndex shakespeare_compressed_index = new InvertedIndex(document_crawler.getAllDocuments(), index_bin_names[1], lookup_table_names[1], data_statistics_names[1]);

    System.out.println("====================== Building indices ======================");
    shakespeare_uncompressed_index.createIndex();
    shakespeare_compressed_index.createIndex();
    System.out.println("====================== Indices Built ======================");

    System.out.println("====================== Writing to disk ======================");
    shakespeare_uncompressed_index.write(false);
    shakespeare_compressed_index.write(true);
    System.out.println("====================== Wrote to disk ======================");

    // Destroying current indices
    shakespeare_uncompressed_index = null;
    shakespeare_compressed_index = null;

    System.out.println("====================== Creating new indices with just lookup table ======================");
    InvertedIndex new_shakespeare_uncompressed_index = new InvertedIndex(index_bin_names[0], lookup_table_names[0], data_statistics_names[0]);
    InvertedIndex new_shakespeare_compressed_index = new InvertedIndex(index_bin_names[1], lookup_table_names[1], data_statistics_names[1]);

    new_shakespeare_uncompressed_index.loadLookupTable();
    new_shakespeare_compressed_index.loadLookupTable();
    System.out.println("====================== Loaded lookup tables ======================");

    System.out.println("====================== Evaluation1: Comparing Vocabs ======================");
    Set<String> new_shakespeare_uncompressed_vocab = new_shakespeare_uncompressed_index.getVocabulary();
    Set<String> new_shakespeare_compressed_vocab = new_shakespeare_compressed_index.getVocabulary();
    System.out.println("Are vocabs equal? :-" + (new_shakespeare_uncompressed_vocab.equals(new_shakespeare_compressed_vocab)));
    System.out.println("====================== Evaluation1 complete ======================");

    System.out.println("====================== Evaluation2: Fetching random terms ======================");
    // Use this only once. Uncomment below code to run.
    // query_terms.json already created

    // ArrayList<String> term_list = new ArrayList<String>();
    // term_list.addAll(new_shakespeare_compressed_vocab);
    // JSONArray random_terms = new JSONArray();
    //
    // for(int i=0; i<100; i++){
    //   Set<String> seven_terms = RandomListGenerator.getRandomTermsWithoutRepetition(term_list, 7);
    //   System.out.println(seven_terms.toString());
    //   JSONArray current_random_terms = new JSONArray();
    //
    //   for(String term: seven_terms){
    //     current_random_terms.add(term);
    //   }
    //
    //   random_terms.add(current_random_terms);
    // }
    //
    // try(FileWriter file = new FileWriter("query_terms_7.json")){
    //   file.write(random_terms.toJSONString());
    //   file.flush();
    // }
    // catch(IOException e){
    //   System.out.println(e);
    // }
    System.out.println("====================== Evaluation2 complete ======================");

    System.out.println("====================== Evaluation3: Dice's coefficient ======================");
    ArrayList<ArrayList<String>> random_query_terms_7 = new ArrayList<ArrayList<String>>();

    // First fetch random terms from query_terms_7.json
    try{
      FileReader file = new FileReader("query_terms_7.json");
      JSONParser parser = new JSONParser();
      JSONArray random_terms = (JSONArray) parser.parse(file);

      for(int i=0; i<random_terms.size(); i++){
        JSONArray current_random_terms = (JSONArray) random_terms.get(i);
        ArrayList<String> temp_list = new ArrayList<String>();

        for(int j=0; j<current_random_terms.size(); j++){
          temp_list.add((String)current_random_terms.get(j));
        }

        random_query_terms_7.add(temp_list);
      }
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }

    for(int i=0; i<random_query_terms_7.size(); i++){
      double top_score = -1.0;
      String top_a = null;
      String top_b = null;

      ArrayList<String> current_random_terms = random_query_terms_7.get(i);

      for(String A: current_random_terms){
        for(String B: current_random_terms){
          double dc = new_shakespeare_compressed_index.getDicesCoefficient(A,B);

          if(top_score < dc){
            top_score = dc;
            top_a = A;
            top_b = B;
          }
        }
      }

      System.out.println("Top scores for " + i + "th iteration is: " + top_a + " " + top_b + " with score: " + top_score);
    }

    System.out.println("====================== Evaluation3 complete ======================");
  }
}
