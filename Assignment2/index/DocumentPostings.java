package index;

import java.util.*;

public class DocumentPostings{
  private int doc_id = 0;
  private ArrayList<Integer> positions = null;

  // Document related meta data
  private int document_term_frequency = 0;

  DocumentPostings(int doc_id){
    this.doc_id = doc_id;
    this.positions = new ArrayList<Integer>();
    this.document_term_frequency = 0;
  }

  // Will be used while reading II from disk
  DocumentPostings(int doc_id, ArrayList<Integer> positions){
    this.doc_id = doc_id;
    this.positions = positions;
    this.document_term_frequency = this.positions.size();
  }

  public int getDocId(){
    return this.doc_id;
  }

  public int getDocumentTermFrequency(){
    return this.document_term_frequency;
  }

  public ArrayList<Integer> getAllPositions(){
    return this.positions;
  }

  public int getPositionsSize(){
    return this.positions.size();
  }

  public void addPosition(Integer position){
    this.positions.add(position);

    // Update related meta data
    this.document_term_frequency++;
  }

  public void deltaEncodePositions(){
    for(int i=this.positions.size()-1; i>=1; i--){
      this.positions.set(i, this.positions.get(i) - this.positions.get(i-1));
    }
  }

  public void deltaDecodePositions(){
    for(int i=1; i<this.positions.size(); i++){
      this.positions.set(i, this.positions.get(i) + this.positions.get(i-1));
    }
  }

  // Gets count of phrases like "this other" (e.g "White House") occuring together.
  public int getAdjacentCount(DocumentPostings other){
    int adjacent_count = 0;
    int this_pointer = 0;
    int other_pointer = 0;

    ArrayList<Integer> this_positions = this.getAllPositions();
    ArrayList<Integer> other_positions = other.getAllPositions();

    while(this_pointer < this_positions.size() && other_pointer < other_positions.size()){
      int this_position = this_positions.get(this_pointer);
      int other_position = other_positions.get(other_pointer);

      if(this_position+1 == other_position){
        adjacent_count++;
        this_pointer++;
        other_pointer++;
      }
      else if(this_position+1 < other_position){
        this_pointer++;
      }
      else{
        other_pointer++;
      }
    }

    return adjacent_count;
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
