package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class BooleanAndNode extends WindowNode{
  int current_postings_id = 0;
  int current_doc_id = this.final_postings_list != null ? this.final_postings_list.get(current_postings_id).getDocId() : 0;

  public BooleanAndNode(ArrayList<TermNode> children, InvertedIndex index){
    super(children, Integer.MAX_VALUE, index);
  }

  public int nextCandidate(){
    return this.current_doc_id;
  }

  public void skipTo(int doc_id){
    while((this.current_doc_id < doc_id) && (this.current_postings_id < this.final_postings_list.size())){
      this.current_postings_id++;
      this.current_doc_id = (this.current_postings_id < this.final_postings_list.size()) ? this.final_postings_list.get(this.current_postings_id).getDocId() : this.current_doc_id;
    }
  }

  public boolean hasMore(){
    return (this.final_postings_list != null) && (current_postings_id < this.final_postings_list.size());
  }

  public double score(int doc_id){
    int dl = 1;
    long cl = this.index.getDocumentCount();

    MultiPositionDocPostings doc_postings = this.getPostingsListByDocID(doc_id);

    int tf = (doc_postings != null) ? doc_postings.getWindowFrequency() : 0;
    long c = this.getTotalWindowCount();

    double score = 0.0;

    double mu = BooleanAndNode.mu;

    double num = (double)tf + mu*((double)c/(double)cl);
    double den = (double)dl + (double)mu;

    return Math.log(num/den);

  }
}
