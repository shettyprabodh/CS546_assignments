import java.util.ArrayList;

class InvertedIndex{
  ArrayList<Document> raw_data = null;
  // Map{} inverted_index = {}
  // compressed_data = {}

  InvertedIndex(ArrayList<Document> documents){
    this.raw_data = documents;
  }

  public void create_index(){
    if(this.raw_data == null){
      System.out.println("No raw data present. Exiting.");
      System.exit(1);
    }

    for(int i=0; i<documents.size(); i++){
      Document current_doc = documents.get(i);
    }
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
