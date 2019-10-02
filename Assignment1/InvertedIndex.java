import java.util.*;
import java.nio.*;
import java.io.*;
import org.json.simple.*;

public class InvertedIndex{
  ArrayList<Document> raw_data = null;
  Hashtable<String, InvertedList> index = null;
  boolean is_delta_encoded = false;
  ArrayList<Integer> encoded_index = null;

  String index_file_name = null;
  String lookup_table_json_name = null;
  RandomAccessFile writer = null;
  RandomAccessFile reader = null;
  // compressed_data = {}

  InvertedIndex(ArrayList<Document> documents, String index_file_name, String lookup_table_json_name){
    this.raw_data = documents;
    this.index = new Hashtable<String, InvertedList>();
    this.is_delta_encoded = false;
    this.encoded_index = new ArrayList<Integer>();
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

  // Delta encoding
  public void deltaEncode(){
    if(this.index == null){
      System.out.println("Index doesn't exist. Please create one and then compress. Exiting.");
      System.exit(1);
    }

    Set<String> terms = this.index.keySet();
    for(String term: terms){
      InvertedList current_inverted_list = this.index.get(term);
      current_inverted_list.deltaEncodePostings();
    }
    this.is_delta_encoded = true;
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
      current_inverted_list.flushToDisk(this.writer);
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
    this.deltaEncode();
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

  // Reading from disk functions
  public void read(){
    // Internally uses reader object. Or it could be a part of decoder
    this.setReader();

    // TODO: Need to read this from flushed lookup table
    Set<String> terms = this.index.keySet();

    for(String term: terms){
      // TODO: This should create new IL
      InvertedList current_inverted_list = this.index.get(term);
      current_inverted_list.reconstructInvertedListFromDisk(reader);
    }
  }

  public void decompress(){
    // Returns  fully deompressed Inverted Index object
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
