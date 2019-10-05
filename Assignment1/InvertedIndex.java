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
  String data_statistics_json_name = null;
  Tokenizer tokenizer = null;
  private boolean is_lookup_table_loaded = false;
  private boolean are_data_statistics_loaded = false;

  // Data statistics
  private int last_doc_id = 0;
  private int num_of_docs = 0;

  // File accessors
  RandomAccessFile writer = null;
  RandomAccessFile reader = null;

  InvertedIndex(ArrayList<Document> documents, String index_file_name, String lookup_table_json_name, String data_statistics_json_name){
    this.raw_data = documents;
    this.index = new Hashtable<String, InvertedList>();
    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
    this.data_statistics_json_name = data_statistics_json_name;
    this.tokenizer = new Tokenizer();
    this.is_lookup_table_loaded = false;
    this.are_data_statistics_loaded = false;
  }

  InvertedIndex(String index_file_name, String lookup_table_json_name, String data_statistics_json_name){
    this.raw_data = null;
    this.index = new Hashtable<String, InvertedList>();
    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
    this.data_statistics_json_name = data_statistics_json_name;
    this.tokenizer = new Tokenizer();
    this.is_lookup_table_loaded = false;
    this.are_data_statistics_loaded = false;
  }

  public int getLastDocID(){
    return this.last_doc_id;
  }

  public int getNumOfDocs(){
    return this.num_of_docs;
  }

  public void createIndex(){
    if(this.raw_data == null){
      System.out.println("No raw data present. Exiting.");
      System.exit(1);
    }

    for(int i=0; i<this.raw_data.size(); i++){
      Document current_doc = this.raw_data.get(i);
      String[] terms = current_doc.terms;

      this.last_doc_id = (this.last_doc_id < current_doc.doc_id) ? current_doc.doc_id : this.last_doc_id;
      this.num_of_docs++;

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

    // Technically, lookup table and statistics are loaded in memory after creating the index
    this.is_lookup_table_loaded = true;
    this.are_data_statistics_loaded = true;
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

  public void flushDataStatistics(){
    JSONObject data_statistics = new JSONObject();

    data_statistics.put("last_doc_id", this.getLastDocID());
    data_statistics.put("num_of_docs", this.getNumOfDocs());

    try(FileWriter file = new FileWriter(this.data_statistics_json_name)){
      file.write(data_statistics.toJSONString());
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
    this.flushDataStatistics();
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

  public void loadDataStatistics(){
    try{
      FileReader file = new FileReader(this.data_statistics_json_name);
      JSONParser parser = new JSONParser();
      JSONObject data_statistics = (JSONObject) parser.parse(file);

      this.last_doc_id = ((Long)data_statistics.get("last_doc_id")).intValue();
      this.num_of_docs = ((Long)data_statistics.get("num_of_docs")).intValue();

      this.are_data_statistics_loaded = true;
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

  public boolean areDataStatisticsLoaded(){
    return this.are_data_statistics_loaded;
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
  public ArrayList<Integer> getScores(String query, Integer result_size){
    if(!this.isLookupTableLoaded()){
      this.loadLookupTable();
    }
    if(!this.areDataStatisticsLoaded()){
      this.loadDataStatistics();
    }

    this.setReader();
    PriorityQueue<PairLongInteger> R = new PriorityQueue<PairLongInteger>();
    String[] query_terms = this.tokenizer.splitOnSpaces(query);

    for(int doc_id=0; doc_id<this.getLastDocID(); doc_id++){
      long total_score = 0;

      for(String query_term: query_terms){
        InvertedList current_inverted_list = (this.index.containsKey(query_term) ? (this.index.get(query_term)) : null);

        // query term is not indexed. Thus, a score of zero.
        if(current_inverted_list == null){
          continue;
        }

        total_score += current_inverted_list.getDocumentWiseScore(doc_id, this.reader);
      }

      R.add(new PairLongInteger(total_score, doc_id));

      // Maintaining top result_size values
      while(R.size() > result_size){
        R.poll();
      }
    }

    ArrayList<Integer> result = new ArrayList<Integer>();
    Stack st = new Stack();

    while(R.size() > 0){
      PairLongInteger temp = R.poll();
      System.out.println("A: " + temp.getA() + " B: " +  temp.getB());
      st.push(new Integer(temp.getB()));
    }

    while(st.size() > 0){
      result.add((Integer)st.pop());
    }

    return result;
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
