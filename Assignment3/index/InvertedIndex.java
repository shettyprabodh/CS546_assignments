package index;

import java.util.*;
import java.nio.*;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import pre_processors.*;

public class InvertedIndex{
  ArrayList<Document> raw_data = null;
  Hashtable<String, InvertedList> index = null;

  // Scoring models
  RetrievalModel retrieval_model = null;

  String index_file_name = "index.bin";
  String lookup_table_json_name = "lookup_table.json";
  String data_statistics_json_name = "data_statistics.json";
  String scene_id_map_json_name = "scene_id_map.json";
  String play_id_map_json_name = "play_id_map.json";
  String doc_length_name = "doc_length.json";
  String term_count_name = "term_count.json";
  String doc_scene_id_map_name = "doc_scene_id.json";

  private boolean is_lookup_table_loaded = false;
  private boolean are_data_statistics_loaded = false;
  private boolean is_scene_id_map_loaded = false;
  private boolean is_play_id_map_loaded = false;
  private boolean is_doc_length_loaded = false;
  private boolean is_term_count_loaded = false;
  private boolean is_doc_scene_id_map_loaded = false;

  // Data statistics
  private int last_doc_id = 0;
  private int document_count = 0;
  private Hashtable<String, TermStatistics> term_statistics = null;
  private Hashtable<String, ArrayList<Integer>> scene_id_map = null;
  private Hashtable<String, ArrayList<Integer>> play_id_map = null;
  private Hashtable<Integer, Integer> doc_length = null;
  private Hashtable<String, Long> term_count = null;
  private Hashtable<Integer, String> doc_scene_id_map = null;

  // File accessors
  RandomAccessFile writer = null;
  RandomAccessFile reader = null;

  public InvertedIndex(ArrayList<Document> documents){
    this.raw_data = documents;
    this.index = new Hashtable<String, InvertedList>();

    this.is_lookup_table_loaded = false;

    this.are_data_statistics_loaded = false;
    this.term_statistics = new Hashtable<String, TermStatistics>();
    this.scene_id_map = new Hashtable<String, ArrayList<Integer>>();
    this.play_id_map = new Hashtable<String, ArrayList<Integer>>();
    this.doc_length = new Hashtable<Integer, Integer>();
    this.term_count = new Hashtable<String, Long>();
    this.doc_scene_id_map = new Hashtable<Integer, String>();

    this.retrieval_model = new RetrievalModel();
  }

  public InvertedIndex(String index_file_name, String lookup_table_json_name, String data_statistics_json_name){
    this.raw_data = null;
    this.index = new Hashtable<String, InvertedList>();

    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
    this.data_statistics_json_name = data_statistics_json_name;

    this.is_lookup_table_loaded = false;

    this.are_data_statistics_loaded = false;
    this.term_statistics = new Hashtable<String, TermStatistics>();
    this.scene_id_map = new Hashtable<String, ArrayList<Integer>>();
    this.play_id_map = new Hashtable<String, ArrayList<Integer>>();
    this.doc_length = new Hashtable<Integer, Integer>();
    this.term_count = new Hashtable<String, Long>();
    this.doc_scene_id_map = new Hashtable<Integer, String>();

    this.retrieval_model = new RetrievalModel();
  }

  public int getLastDocID(){
    if(!this.areDataStatisticsLoaded()){
      this.loadDataStatistics();
      this.loadDocLength();
    }

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

      // Updating scene id map
      ArrayList<Integer> correct_scene_id_doc_list = null;
      if(this.scene_id_map.containsKey(current_doc.scene_id)){
        correct_scene_id_doc_list = this.scene_id_map.get(current_doc.scene_id);
      }
      else{
        correct_scene_id_doc_list = new ArrayList<Integer>();
        this.scene_id_map.put(current_doc.scene_id, correct_scene_id_doc_list);
      }
      correct_scene_id_doc_list.add(current_doc.doc_id);

      // Updating play id map
      ArrayList<Integer> correct_play_id_doc_list = null;
      if(this.play_id_map.containsKey(current_doc.play_id)){
        correct_play_id_doc_list = this.play_id_map.get(current_doc.play_id);
      }
      else{
        correct_play_id_doc_list = new ArrayList<Integer>();
        this.play_id_map.put(current_doc.play_id, correct_play_id_doc_list);
      }
      correct_play_id_doc_list.add(current_doc.doc_id);

      this.doc_length.put(current_doc.doc_id, terms.length);
      this.doc_scene_id_map.put(current_doc.doc_id, current_doc.scene_id);

      for(int term_position=0; term_position < terms.length; term_position++){
        String current_term = terms[term_position];
        InvertedList correct_inverted_list = null;
        Long current_term_count = new Long(0);

        // Fetch InvertedList related to current_term
        if(this.index.containsKey(current_term)){
          correct_inverted_list = this.index.get(current_term);
        }
        else{
          correct_inverted_list = new InvertedList();
          this.index.put(current_term, correct_inverted_list);
        }

        // Updating/creating terms count to be used in Language Models
        if(this.term_count.containsKey(current_term)){
          current_term_count = this.term_count.get(current_term);
          current_term_count++;
        }
        else{
          current_term_count++;
        }
        this.term_count.put(current_term, current_term_count);

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

  private void setCompressionByte(boolean is_compression_required){
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
  private void flushToDisk(boolean is_compression_required){
    Set<String> terms = this.index.keySet();

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      current_inverted_list.flushToDisk(this.getWriter(), is_compression_required);
    }

    // Closing writer
    this.closeWriter();
  }

  private void flushLookupTable(){
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

  private void flushDataStatistics(){

    Set<String> terms = this.index.keySet();
    JSONObject data_statistics = new JSONObject();
    JSONObject term_statistics = new JSONObject();

    data_statistics.put("last_doc_id", this.getLastDocID());
    data_statistics.put("document_count", this.getDocumentCount());

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      JSONObject temp_statistics = new JSONObject();
      temp_statistics.put("term_frequency", current_inverted_list.getTermFrequency(this.getReader()));
      temp_statistics.put("document_count", current_inverted_list.getDocumentCount(this.getReader()));

      term_statistics.put(term, temp_statistics);
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

  private void flushSceneIdMap(){
    Set<String> scene_ids = this.scene_id_map.keySet();
    JSONObject scene_id_map = new JSONObject();

    for(String scene_id: scene_ids){
      ArrayList<Integer> current_doc_list = this.scene_id_map.get(scene_id);
      JSONArray doc_ids = new JSONArray();

      for(Integer doc_id: current_doc_list){
        doc_ids.add(doc_id);
      }

      scene_id_map.put(scene_id, doc_ids);
    }

    try(FileWriter file = new FileWriter(this.scene_id_map_json_name)){
      file.write(scene_id_map.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  private void flushPlayIdMap(){
    Set<String> play_ids = this.play_id_map.keySet();
    JSONObject play_id_map = new JSONObject();

    for(String play_id: play_ids){
      ArrayList<Integer> current_doc_list = this.play_id_map.get(play_id);
      JSONArray doc_ids = new JSONArray();

      for(Integer doc_id: current_doc_list){
        doc_ids.add(doc_id);
      }

      play_id_map.put(play_id, doc_ids);
    }

    try(FileWriter file = new FileWriter(this.play_id_map_json_name)){
      file.write(play_id_map.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  private void flushDocLength(){
    Set<Integer> doc_ids = this.doc_length.keySet();
    JSONObject doc_length_map = new JSONObject();

    for(Integer doc_id: doc_ids){
      doc_length_map.put(doc_id, this.doc_length.get(doc_id));
    }

    try(FileWriter file = new FileWriter(this.doc_length_name)){
      file.write(doc_length_map.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  private void flushDocScenenIdMap(){
    Set<Integer> doc_ids = this.doc_scene_id_map.keySet();
    JSONObject doc_scene_id_map = new JSONObject();

    for(Integer doc_id: doc_ids){
      doc_scene_id_map.put(doc_id, this.doc_scene_id_map.get(doc_id));
    }

    try(FileWriter file = new FileWriter(this.doc_scene_id_map_name)){
      file.write(doc_scene_id_map.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  private void flushTermCount(){
    Set<String> terms = this.term_count.keySet();
    JSONObject term_count_map = new JSONObject();

    for(String term: terms){
      term_count_map.put(term, this.term_count.get(term));
    }

    try(FileWriter file = new FileWriter(this.term_count_name)){
      file.write(term_count_map.toJSONString());
      file.flush();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void write(boolean is_compression_required){
    this.setCompressionByte(is_compression_required);
    this.flushToDisk(is_compression_required);
    this.flushLookupTable();
    this.flushDataStatistics();
    this.flushSceneIdMap();
    this.flushPlayIdMap();
    this.flushDocLength();
    this.flushTermCount();
    this.flushDocScenenIdMap();
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

  public RandomAccessFile getReader(){
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

  public void loadSceneIdMap(){
    try{
      FileReader file = new FileReader(this.scene_id_map_json_name);
      JSONParser parser = new JSONParser();
      JSONObject scene_id_map = (JSONObject) parser.parse(file);

      Set<String> scene_ids = scene_id_map.keySet();

      for(String scene_id: scene_ids){
        JSONArray doc_ids = (JSONArray)scene_id_map.get(scene_id);
        ArrayList<Integer> doc_ids_list = new ArrayList<Integer>();

        for(int i=0; i<doc_ids.size(); i++){
          doc_ids_list.add(((Long)doc_ids.get(i)).intValue());
        }


        this.scene_id_map.put(scene_id, doc_ids_list);
      }

      this.is_scene_id_map_loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void loadPlayIdMap(){
    try{
      FileReader file = new FileReader(this.play_id_map_json_name);
      JSONParser parser = new JSONParser();
      JSONObject play_id_map = (JSONObject) parser.parse(file);

      Set<String> play_ids = play_id_map.keySet();

      for(String play_id: play_ids){
        JSONArray doc_ids = (JSONArray)play_id_map.get(play_id);
        ArrayList<Integer> doc_ids_list = new ArrayList<Integer>();

        for(int i=0; i<doc_ids.size(); i++){
          doc_ids_list.add(((Long)doc_ids.get(i)).intValue());
        }


        this.play_id_map.put(play_id, doc_ids_list);
      }

      this.is_play_id_map_loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void loadDocLength(){
    try{
      FileReader file = new FileReader(this.doc_length_name);
      JSONParser parser = new JSONParser();
      JSONObject doc_length_map = (JSONObject) parser.parse(file);

      Set<String> doc_ids = doc_length_map.keySet();

      for(String doc_id: doc_ids){
        this.doc_length.put(Integer.parseInt(doc_id), ((Long)doc_length_map.get(doc_id)).intValue());
      }

      this.is_doc_length_loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void loadTermCount(){
    try{
      FileReader file = new FileReader(this.term_count_name);
      JSONParser parser = new JSONParser();
      JSONObject term_count_map = (JSONObject) parser.parse(file);

      Set<String> terms = term_count_map.keySet();

      for(String term: terms){
        this.term_count.put(term, (Long)term_count_map.get(term));
      }

      this.is_term_count_loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void loadDocSceneIdMap(){
    try{
      FileReader file = new FileReader(this.doc_scene_id_map_name);
      JSONParser parser = new JSONParser();
      JSONObject doc_scene_id_map = (JSONObject) parser.parse(file);

      Set<String> doc_ids = doc_scene_id_map.keySet();

      for(String doc_id: doc_ids){
        this.doc_scene_id_map.put(Integer.parseInt(doc_id), (String)doc_scene_id_map.get(doc_id));
      }

      this.is_doc_scene_id_map_loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public String getSceneIdFromDocId(Integer doc_id){
    if(!this.is_doc_scene_id_map_loaded){
      this.loadDocSceneIdMap();
    }

    return this.doc_scene_id_map.get(doc_id);
  }

  public boolean isLookupTableLoaded(){
    return this.is_lookup_table_loaded;
  }

  public boolean areDataStatisticsLoaded(){
    return this.are_data_statistics_loaded;
  }

  public Integer getDocumentLength(int doc_id){
    if(!this.is_doc_length_loaded){
      this.loadDocLength();
    }

    int result = 0;
    if(this.doc_length.containsKey(doc_id)){
      result = (this.doc_length.get(doc_id));
    }

    return result;
  }

  public double getAverageDocumentLength(){
    if(!this.is_doc_length_loaded){
      this.loadDocLength();
    }

    long total_length = 0;
    Set<Integer> doc_ids = this.doc_length.keySet();
    int num_of_docs = doc_ids.size();

    for(Integer doc_id:doc_ids){
      total_length += (this.doc_length.get(doc_id));
    }

    return (double)total_length/(double)num_of_docs;
  }

  public Long getTotalWordCount(){
    if(!this.is_term_count_loaded){
      this.loadTermCount();
    }
    Long total_word_count = new Long(0);

    Set<String> terms = this.term_count.keySet();

    for(String term: terms){
      total_word_count += (this.term_count.get(term));
    }

    return total_word_count;
  }

  public InvertedList getInvertedList(String term){
    return (this.index.containsKey(term) ? (this.index.get(term)) : null);
  }

  // Assumes query has been stemmed
  // Returns an Arraylist(of size k) of doc_ids with descending scores
  // public ArrayList<PairDoubleInteger> getScores(String query, Integer result_size, String retrieval_model_name){
  //   if(!this.isLookupTableLoaded()){
  //     this.loadLookupTable();
  //   }
  //   if(!this.areDataStatisticsLoaded()){
  //     this.loadDataStatistics();
  //     this.loadDocLength();
  //   }
  //   if(!this.is_term_count_loaded){
  //     this.loadTermCount();
  //   }
  //
  //   PriorityQueue<PairDoubleInteger> R = new PriorityQueue<PairDoubleInteger>();
  //   String[] query_terms = this.tokenizer.splitOnSpaces(query);
  //
  //   InvertedList.readCompressionByte(this.getReader());
  //
  //   double average_document_length = this.getAverageDocumentLength();
  //   int total_document_count = this.getDocumentCount();
  //   Long total_word_count = this.getTotalWordCount();
  //
  //   for(int doc_id=0; doc_id<this.getLastDocID(); doc_id++){
  //     double total_score = 0.0;
  //     int current_document_length = this.getDocumentLength(doc_id);
  //
  //     for(String query_term: query_terms){
  //       InvertedList current_inverted_list = (this.index.containsKey(query_term) ? (this.index.get(query_term)) : null);
  //
  //       // query term is not indexed. Thus, a score of zero.
  //       if(current_inverted_list == null){
  //         continue;
  //       }
  //
  //       int query_term_frequency = 0;
  //       for(String term: query_terms){
  //         if(term == query_term){
  //           query_term_frequency++;
  //         }
  //       }
  //
  //       RetrievalModelParams retrieval_model_params = new RetrievalModelParams();
  //       retrieval_model_params.average_document_length = average_document_length;
  //       retrieval_model_params.total_document_count = total_document_count;
  //       retrieval_model_params.current_document_length = current_document_length;
  //       retrieval_model_params.total_word_count = total_word_count;
  //       retrieval_model_params.qf = query_term_frequency;
  //
  //       total_score += current_inverted_list.getDocumentWiseScore(doc_id, this.retrieval_model, retrieval_model_name, retrieval_model_params, this.getReader());
  //     }
  //
  //     R.add(new PairDoubleInteger(total_score, doc_id));
  //
  //     // Maintaining top result_size values
  //     while(R.size() > result_size){
  //       R.poll();
  //     }
  //   }
  //
  //   ArrayList<PairDoubleInteger> result = new ArrayList<PairDoubleInteger>();
  //   Stack st = new Stack();
  //
  //   while(R.size() > 0){
  //     PairDoubleInteger temp = R.poll();
  //     st.push(temp);
  //   }
  //
  //   while(st.size() > 0){
  //     result.add((PairDoubleInteger)st.pop());
  //   }
  //
  //   return result;
  // }

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
