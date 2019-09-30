import java.util.ArrayList;

public class InvertedList{
  ArrayList<DocumentPostings> doc_postings = null;

  // Term related meta data
  long term_frequency = 0;

  InvertedList(){
    this.doc_postings = new ArrayList<DocumentPostings>();
    this.term_frequency = 0;
  }

  public void add_posting(long doc_id, long position){
    DocumentPostings correct_doc_postings = null;

    // Search for doc posting
    // TODO: Can be improved using HashSet or using the ordering of doc_ids
    for(int i=0; i<this.doc_postings.size(); i++){
      DocumentPostings current_doc_postings = this.doc_postings.get(i);
      if(current_doc_postings.doc_id == doc_id){
        correct_doc_postings = current_doc_postings;
        break;
      }
    }

    if(correct_doc_postings == null){
      correct_doc_postings = new DocumentPostings(doc_id);
      this.doc_postings.add(correct_doc_postings);
    }

    correct_doc_postings.add_position(position);

    // Update meta data
    this.term_frequency++;
  }

  @Override
  public String toString(){
    String result = "";

    result += "{ ";
    result += ("term_frequency: " + term_frequency + "\n");
    result += ("doc_postings: " + doc_postings.toString() + "\n");
    result += "}";
    result += "\n";

    return result;
  }
}
