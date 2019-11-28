package inference_network;

import java.util.*;

/*
  Functionally same as DocumentPostings from index package, except this one
  keeps an array of positions instead of Integer for position. Each element in
  array of positions correspond to position of a term. This is primarily used
  for posting lists in WindowNode.
*/
public class MultiPositionDocPostings{
  int doc_id;
  ArrayList< ArrayList<Integer> > positions;

  public MultiPositionDocPostings(int doc_id){
    this.doc_id = doc_id;
    this.positions = new ArrayList< ArrayList<Integer> >();
  }

  public MultiPositionDocPostings(int doc_id, ArrayList<Integer> positions){
    this.doc_id = doc_id;
    this.positions = new ArrayList< ArrayList<Integer> >();

    for(int i=0; i<positions.size(); i++){
      ArrayList<Integer> temp_list = new ArrayList<Integer>();
      temp_list.add(positions.get(i));

      this.positions.add(temp_list);
    }
  }
  //
  // public MultiPositionDocPostings(int doc_id, ArrayList< ArrayList<Integer> > positions){
  //   this.doc_id = doc_id;
  //   this.positions = positions;
  // }

  public void setPositions(ArrayList< ArrayList<Integer> > positions){
    this.positions = positions;
  }

  public int getDocId(){
    return this.doc_id;
  }

  public ArrayList< ArrayList<Integer> > getPositions(){
    return this.positions;
  }

  public int getWindowFrequency(){
    return this.positions.size();
  }

  @Override
  public String toString(){
    String result = "";

    result += "{";
    result += "document id: " + doc_id + ", ";
    result += "positions: " + positions;
    result += "}";
    // result += "\n";

    return result;
  }
}
