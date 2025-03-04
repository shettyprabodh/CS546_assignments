import java.util.*;
import index.*;
import java.nio.*;

public class TopLevelClass{
  public static void main(String[] args){
    String raw_file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(raw_file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");
    String[] index_bin_names = new String[]{"index_uncompressed.bin", "index_compressed.bin"};
    String[] lookup_table_names = new String[]{"lookup_table_uncompressed.json", "lookup_table_compressed.json"};
    String[] data_statistics_names = new String[]{"data_statistics_uncompressed.json", "data_statistics_compressed.json"};

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

    double dc_uncompressed = new_shakespeare_uncompressed_index.getDicesCoefficient("the", "noble");
    double dc_compressed = new_shakespeare_compressed_index.getDicesCoefficient("the", "noble");
    System.out.println("To further show both indices are same. I have calculated Dice's coefficient for \"the\" and \"noble\". un-compressed score:" + dc_uncompressed + " compressedscore: " + dc_compressed);

    System.out.println("====================== Evaluation1 complete ======================");

    System.out.println("====================== Evaluation2: Fetching random terms ======================");
    // Use this only once. Uncomment below code to run.

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
    ArrayList<ArrayList<String>> random_query_terms_7 = QueryTermsReader.fetchQueryTerms("query_terms_7.json");

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

    System.out.println("====================== Evaluation4: Compression Hypothesis ======================");
    System.out.println("====================== Part1: Timing with 7 term queries ======================");
    ArrayList<ArrayList<String>> random_terms_7 = QueryTermsReader.fetchQueryTerms("query_terms_7.json");

    System.out.println("====================== Timings for compressed indices ======================");
    TimingQueries.printTimingForQueries(new_shakespeare_compressed_index, random_terms_7);

    System.out.println("====================== Timings for uncompressed indices ======================");
    TimingQueries.printTimingForQueries(new_shakespeare_uncompressed_index, random_terms_7);

    System.out.println("====================== Part2: Timing with 14 term queries ======================");
    ArrayList<ArrayList<String>> random_terms_14 = QueryTermsReader.fetchQueryTerms("query_terms_14.json");

    System.out.println("====================== Timings for compressed indices ======================");
    TimingQueries.printTimingForQueries(new_shakespeare_compressed_index, random_terms_14);

    System.out.println("====================== Timings for uncompressed indices ======================");
    TimingQueries.printTimingForQueries(new_shakespeare_uncompressed_index, random_terms_14);

    System.out.println("====================== Evaluation4 complete ======================");

    System.out.println("====================== Scene and Play Statistics ======================");
    System.out.println("Average scene length: " + new_shakespeare_compressed_index.getAverageSceneLength() + " words");
    System.out.println("Shortest scene(id): " + new_shakespeare_compressed_index.getShortestScene());
    System.out.println("Longest play(id): " + new_shakespeare_compressed_index.getLongestPlay());
    System.out.println("Shortest play(id): " + new_shakespeare_compressed_index.getShortestPlay());
  }
}
