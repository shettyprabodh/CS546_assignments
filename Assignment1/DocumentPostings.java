import java.util.*;

public class DocumentPostings{
  int doc_id = 0;
  boolean is_delta_encoded = false;
  ArrayList<Integer> positions = null;

  // Document related meta data
  int document_term_frequency = 0;

  DocumentPostings(int doc_id){
    this.doc_id = doc_id;
    this.is_delta_encoded = false;
    this.positions = new ArrayList<Integer>();
    this.document_term_frequency = 0;
  }

  // Will be used while reading II from disk
  DocumentPostings(int doc_id, boolean is_delta_encoded, ArrayList<Integer> positions){
    this.doc_id = doc_id;
    this.is_delta_encoded = false;
    this.positions = positions;
    this.document_term_frequency = this.positions.size();
  }

  public void addPosition(Integer position){
    this.positions.add(position);

    // Update related meta data
    this.document_term_frequency++;
  }

  public void deltaEncodePositions(){
    for(int i=1; i<this.positions.size(); i++){
      this.positions.set(i, this.positions.get(i) - this.positions.get(i-1));
    }
    this.is_delta_encoded = true;
  }

  @Override
  public String toString(){
    String result = "";

    result += "{";
    result += "document id: " + doc_id + ", ";
    result += "document_term_frequency: " + document_term_frequency;
    result += "}";
    // result += "\n";

    return result;
  }
}
