import java.util.*;
import java.nio.*;
import java.io.*;
import exceptions.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class InvertedIndex{
  ArrayList<Document> raw_data = null;
  Hashtable<String, InvertedList> index = null;

  String index_file_name = null;
  String lookup_table_json_name = null;
  Tokenizer tokenizer = null;
  private boolean is_lookup_table_loaded = false;

  // File accessors
  RandomAccessFile writer = null;
  RandomAccessFile reader = null;

  InvertedIndex(ArrayList<Document> documents, String index_file_name, String lookup_table_json_name){
    this.raw_data = documents;
    this.index = new Hashtable<String, InvertedList>();
    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
    this.tokenizer = new Tokenizer();
    this.is_lookup_table_loaded = false;
  }

  InvertedIndex(String index_file_name, String lookup_table_json_name){
    this.raw_data = null;
    this.index = new Hashtable<String, InvertedList>();
    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
    this.tokenizer = new Tokenizer();
    this.is_lookup_table_loaded = false;
  }

  public void createIndex(){
    if(this.raw_data == null){
      System.out.println("No raw data present. Exiting.");
      System.exit(1);
    }

    for(int i=0; i<this.raw_data.size(); i++){
      Document current_doc = this.raw_data.get(i);
      String[] terms = current_doc.terms;

      for(int term_position=0; term_position < terms.length; term_position++){
        String current_term = terms[term_position];
        InvertedList correct_inverted_list = null;

        // Fetch InvertedList related to current_term
        if(this.index.containsKey(current_term)){
          correct_inverted_list = this.index.get(current_term);
        }
        else{
          correct_inverted_list = new InvertedList();
          this.index.put(current_term, correct_inverted_list);
        }

        // Add posting
        correct_inverted_list.addPosting(current_doc.doc_id, term_position);
      }
    }

    // Technically, lookup table is loaded in memory after creating the index
    this.is_lookup_table_loaded = true;
  }

  private void setWriter(){
    try{
      this.writer = new RandomAccessFile(this.index_file_name, "rw");
    }
    catch (FileNotFoundException e){
      System.out.println(e);
    }
  }

  private void closeWriter(){
    try{
      this.writer.close();
    }
    catch (IOException e){
      System.out.println(e);
    }
  }

  // An array of integers
  public void flushToDisk(){
    this.setWriter();

    Set<String> terms = this.index.keySet();

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      // TODO: Update is_compression_required here
      current_inverted_list.flushToDisk(this.writer, false);
    }

    // Closing writer
    this.closeWriter();
  }

  public void flushLookupTable(){
    Set<String> terms = this.index.keySet();
    JSONObject lookup_table = new JSONObject();

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      JSONObject lookup_table_record = new JSONObject();

      lookup_table_record.put("offset", current_inverted_list.offset);
      lookup_table_record.put("num_bytes", current_inverted_list.num_bytes);

      lookup_table.put(term, lookup_table_record);
    }

    try(FileWriter file = new FileWriter(this.lookup_table_json_name)){
      file.write(lookup_table.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void write(){
    // Internally uses writer object. Or it could be a part of encoder
    this.flushToDisk();
    this.flushLookupTable();
  }


  // Sets reader. If already, does nothing.
  private void setReader(){
    try{
      if(this.reader == null){
        this.reader = new RandomAccessFile(this.index_file_name, "rw");
      }
    }
    catch (FileNotFoundException e){
      System.out.println(e);
    }
  }

  // First step before querying. Loading the lookup table
  public void loadLookupTable(){
    try{
      FileReader file = new FileReader(this.lookup_table_json_name);
      JSONParser parser = new JSONParser();
      JSONObject lookup_table = (JSONObject) parser.parse(file);

      // Recreate InvertedIndex without postings, just the meta-data
      Set<String> terms = lookup_table.keySet();

      for(String term: terms){
        JSONObject current_inverted_list_data = (JSONObject) lookup_table.get(term);
        int temp = ((Long)current_inverted_list_data.get("num_bytes")).intValue();
        // System.out.println(temp.getClass().getSimpleName());
        long offset = (long) current_inverted_list_data.get("offset");
        int num_bytes = ((Long)current_inverted_list_data.get("num_bytes")).intValue();

        // Partial because postings list is not loaded
        InvertedList partial_inverted_list = new InvertedList(offset, (int)num_bytes);
        this.index.put(term, partial_inverted_list);
      }

      this.is_lookup_table_loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public boolean isLookupTableLoaded(){
    return this.is_lookup_table_loaded;
  }

  // Builds whole index from disk. rebuildIndex and createIndex are the
  // only two methods used to create whole index.
  public void rebuildIndex(){
    this.setReader();
    Set<String> terms = this.index.keySet();

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      current_inverted_list.reconstructPostingsFromDisk(this.reader);
    }
  }

  // Assumes query has been stemmed
  // Returns an Arraylist(of size k) of doc_ids with descending scores
  public ArrayList<Integer> getScores(String query){
    if(!this.isLookupTableLoaded()){
      this.loadLookupTable();
      // System.out.println("Lookup table not loaded. Use loadLookupTable first");
      // throw new LookupTableNotLoadedException("Lookup table not loaded. Use loadLookupTable first");
    }
    this.setReader();

    long final_score = 0;
    Hashtable<Integer, Long> accumulator = new Hashtable<Integer, Long>();

    String[] query_terms = this.tokenizer.splitOnSpaces(query);

    for(String query_term: query_terms){
      InvertedList current_inverted_list = this.index.containsKey(query_term) ? this.index.get(query_term) : null;

      // Zero score for query terms not indexed
      if(current_inverted_list == null){
        continue;
      }

      // Load postings to disk
      current_inverted_list.reconstructPostingsFromDisk(this.reader);
      // Document scores format [doc_id_1 score_1 doc_id_2 score_2 ...]
      ArrayList<Integer> document_scores = current_inverted_list.getDocumentWiseScores();
      System.out.println(query_term + " : " + Arrays.toString(document_scores.toArray()));

      for(int i=0; i<document_scores.size(); i+=2){
        Integer doc_id = document_scores.get(i);
        Integer score = document_scores.get(i+1);

        if(accumulator.containsKey(doc_id)){
          Long partial_score = accumulator.get(doc_id);
          partial_score += score.longValue();
          accumulator.put(doc_id, partial_score);
        }
        else{
          accumulator.put(doc_id, score.longValue());
        }
      }
    }

    // Sorting scores
    // TODO: USe PriorityQueue with <Long, Integer>
    PriorityQueue<Integer> pQueue = new PriorityQueue<Integer>();

    return new ArrayList<Integer>();
  }

  // Utility functions
  @Override
  public String toString(){
    String result = "";

    result += ("Displaying index:\n" + index.toString());
    result += "\n";

    return result;
  }

}
