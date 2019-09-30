import java.util.ArrayList;

public class DocumentPostings{
  long doc_id = 0;
  boolean is_delta_encoded = false;
  ArrayList<Long> positions = null;

  // Document related meta data
  long document_term_frequency = 0;

  DocumentPostings(long doc_id){
    this.doc_id = doc_id;
    this.is_delta_encoded = false;
    this.positions = new ArrayList<Long>();
    this.document_term_frequency = 0;
  }

  public void add_position(long position){
    this.positions.add(position);

    // Update related meta data
    this.document_term_frequency++;
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
