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
  private int document_count = 0;
  // NOTE: TreeMap can be used here if we want to save space
  private Hashtable term_statistics = null;

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
    this.term_statistics = new Hashtable<String, TermStatistics>();
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
    this.term_statistics = new Hashtable<String, TermStatistics>();
  }

  public int getLastDocID(){
    return this.last_doc_id;
  }

  public int getDocumentCount(){
    return this.document_count;
  }

  public Set<String> getVocabulary(){
    if(!this.isLookupTableLoaded()){
      this.loadLookupTable();
    }

    return this.index.keySet();
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
      this.document_count++;

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
      if(this.writer == null){
        this.writer = new RandomAccessFile(this.index_file_name, "rw");
      }
    }
    catch (FileNotFoundException e){
      System.out.println(e);
    }
  }

  private RandomAccessFile getWriter(){
    this.setWriter();
    return this.writer;
  }

  private void closeWriter(){
    try{
      this.writer.close();
    }
    catch (IOException e){
      System.out.println(e);
    }
  }

  public void setCompressionByte(boolean is_compression_required){
    RandomAccessFile writer = this.getWriter();
    byte compression_byte = (byte)(is_compression_required ? 1: 0);
    try{
      writer.write(compression_byte);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  // An array of integers
  public void flushToDisk(boolean is_compression_required){
    Set<String> terms = this.index.keySet();

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      // TODO: Update is_compression_required here
      current_inverted_list.flushToDisk(this.getWriter(), is_compression_required);
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

      lookup_table_record.put("offset", current_inverted_list.getOffset());
      lookup_table_record.put("num_bytes", current_inverted_list.getNumBytes());

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

    Set<String> terms = this.index.keySet();
    JSONObject data_statistics = new JSONObject();

    data_statistics.put("last_doc_id", this.getLastDocID());
    data_statistics.put("document_count", this.getDocumentCount());

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      JSONObject term_statistics = new JSONObject();

      term_statistics.put("term_frequency", current_inverted_list.getTermFrequency(this.getReader()));
      term_statistics.put("document_count", current_inverted_list.getDocumentCount(this.getReader()));
    }

    data_statistics.put("term_statistics", term_statistics);

    try(FileWriter file = new FileWriter(this.data_statistics_json_name)){
      file.write(data_statistics.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void write(boolean is_compression_required){
    // Internally uses writer object. Or it could be a part of encoder
    this.setCompressionByte(is_compression_required);
    this.flushToDisk(is_compression_required);
    this.flushLookupTable();
    this.flushDataStatistics();
  }


  // Sets reader. If already set, does nothing.
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

  private RandomAccessFile getReader(){
    this.setReader();
    return this.reader;
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
      this.document_count = ((Long)data_statistics.get("document_count")).intValue();

      JSONObject term_statistics = (JSONObject)data_statistics.get("term_statistics");
      Set<String> terms = term_statistics.keySet();

      for(String term: terms){
        JSONObject current_term_statistics = (JSONObject) term_statistics.get(term);
        int term_frequency = ((Long)current_term_statistics.get("term_frequency")).intValue();
        int document_count = ((Long)current_term_statistics.get("document_count")).intValue();

        this.term_statistics.put(term, new TermStatistics(term_frequency, document_count));
      }

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
    Set<String> terms = this.index.keySet();
    InvertedList.readCompressionByte(this.getReader());
    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      current_inverted_list.reconstructPostingsFromDisk(this.getReader());
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

    PriorityQueue<PairLongInteger> R = new PriorityQueue<PairLongInteger>();
    String[] query_terms = this.tokenizer.splitOnSpaces(query);

    InvertedList.readCompressionByte(this.getReader());

    for(int doc_id=0; doc_id<this.getLastDocID(); doc_id++){
      long total_score = 0;

      for(String query_term: query_terms){
        InvertedList current_inverted_list = (this.index.containsKey(query_term) ? (this.index.get(query_term)) : null);

        // query term is not indexed. Thus, a score of zero.
        if(current_inverted_list == null){
          continue;
        }

        total_score += current_inverted_list.getDocumentWiseScore(doc_id, this.getReader());
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

  public double getDicesCoefficient(String a, String b){
    if(!this.isLookupTableLoaded()){
      this.loadLookupTable();
    }

    InvertedList.readCompressionByte(this.getReader());

    InvertedList IL_a = this.index.containsKey(a) ? this.index.get(a) : null;
    InvertedList IL_b = this.index.containsKey(b) ? this.index.get(b) : null;

    if((IL_a == null) || (IL_b == null)){
      return 0.0;
    }

    IL_a.reconstructPostingsFromDisk(this.getReader());
    IL_b.reconstructPostingsFromDisk(this.getReader());

    int n_a = IL_a.getTermFrequency(this.getReader());
    int n_b = IL_b.getTermFrequency(this.getReader());
    int n_a_b = 0;

    IL_a.resetPointer();
    IL_b.resetPointer();

    while(IL_a.hasMorePostings() && IL_b.hasMorePostings()){
      DocumentPostings current_postings_a = IL_a.getCurrentPostings();
      DocumentPostings current_postings_b = IL_b.getCurrentPostings();

      if(current_postings_a.getDocId() == current_postings_b.getDocId()){
        n_a_b += (current_postings_a.getAdjacentCount(current_postings_b));
        IL_a.setPointerToNextPostings();
        IL_b.setPointerToNextPostings();
      }
      else if(current_postings_a.getDocId() < current_postings_b.getDocId()){
        IL_a.setPointerToNextPostings();
      }
      else{
        IL_b.setPointerToNextPostings();
      }
    }
    System.out.println("a: " + n_a + " b: " + n_b + " ab: " + n_a_b);
    double d_c = ((double)n_a_b)/(n_a + n_b);
    return d_c;
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
