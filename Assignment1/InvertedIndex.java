import java.util.ArrayList;

class InvertedIndex{
  ArrayList<Document> raw_data = null;
  // Map{} inverted_index = {}
  // compressed_data = {}

  InvertedIndex(ArrayList<Document> documents){
    this.raw_data = documents;
  }

  public void index_data(){

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
