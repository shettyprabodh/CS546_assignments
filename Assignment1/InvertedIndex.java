import java.util.*;
import java.nio.*;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class InvertedIndex{
  ArrayList<Document> raw_data = null;
  Hashtable<String, InvertedList> index = null;

  String index_file_name = null;
  String lookup_table_json_name = null;
  RandomAccessFile writer = null;
  RandomAccessFile reader = null;
  // compressed_data = {}

  InvertedIndex(ArrayList<Document> documents, String index_file_name, String lookup_table_json_name){
    this.raw_data = documents;
    this.index = new Hashtable<String, InvertedList>();
    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
  }

  InvertedIndex(String index_file_name, String lookup_table_json_name){
    this.raw_data = null;
    this.index = new Hashtable<String, InvertedList>();
    this.index_file_name = index_file_name;
    this.lookup_table_json_name = lookup_table_json_name;
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


  private void setReader(){
    try{
      this.reader = new RandomAccessFile(this.index_file_name, "rw");
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
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }


  // Builds whole index from disk. rebuildIndex and createIndex are the
  // only two methods used to create whole index.
  public void rebuildIndex(){
    this.setReader();
    Set<String> terms = this.index.keySet();

    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      current_inverted_list.reconstructFromDisk(reader);
    }
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
