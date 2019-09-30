import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

public class InvertedIndex{
  ArrayList<Document> raw_data = null;
  Hashtable<String, InvertedList> index = null;
  boolean is_delta_encoded = false;
  // compressed_data = {}

  InvertedIndex(ArrayList<Document> documents){
    this.raw_data = documents;
    this.index = new Hashtable<String, InvertedList>();
  }

  public void create_index(){
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
        correct_inverted_list.add_posting(current_doc.doc_id, term_position);
      }
    }
  }

  @Override
  public String toString(){
    String result = "";

    result += ("Displaying index:\n" + index.toString());
    result += "\n";

    return result;
  }

  // Writing to disk functions
  public void compress(){

  }

  public void Encode(){
    // Internally uses Encoder object
  }

  public void Write(){
    // Internally uses writer object. Or it could be a part of encoder
  }



  // Reading from disk functions
  public void Read(){
    // Internally uses reader object. Or it could be a part of decoder
  }

  public void Decode(){
    // Internally uses Decoder(or Encoder) object
  }

  public void Decompress(){
    // Returns  fully deompressed Inverted Index object
  }

}
