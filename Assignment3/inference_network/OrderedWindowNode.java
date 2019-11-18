package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class OrderedWindowNode extends WindowNode{
  int current_postings_id;
  int current_doc_id;

  public OrderedWindowNode(ArrayList<TermNode> children, int window_size, InvertedIndex index){
    super(children, window_size, index);
    this.removeUnsortedPostingList();
    this.current_postings_id = 0;
    this.current_doc_id = this.final_postings_list != null ? this.final_postings_list.get(current_postings_id).getDocId() : 0;
  }

  /*
    Since, positions of each term is added to final_postings_list in same
    sequence as child terms are read, having unsorted positions array
    basically implies some term occurred earlier than it should be. Thus,
    remove all such positions arrays.
  */
  public void removeUnsortedPostingList(){
    if(this.final_postings_list == null){
      return;
    }

    ArrayList<MultiPositionDocPostings> final_postings_list = new ArrayList<MultiPositionDocPostings>();

    for(int i=0; i<this.final_postings_list.size(); i++){
      MultiPositionDocPostings current_postings_list = this.final_postings_list.get(i);
      ArrayList< ArrayList<Integer> > all_positions = current_postings_list.getPositions();
      ArrayList< ArrayList<Integer> > sorted_positions = new ArrayList< ArrayList<Integer> >();

      for(int j=0; j<all_positions.size(); j++){
        if(OrderedWindowNode.isSorted(all_positions.get(j))){
          sorted_positions.add(all_positions.get(j));
        }
      }

      if(sorted_positions.size() > 0){
        MultiPositionDocPostings temp = new MultiPositionDocPostings(current_postings_list.getDocId());
        temp.setPositions(sorted_positions);

        final_postings_list.add(temp);
      }
    }

    if(final_postings_list.size() > 0){
      this.final_postings_list = final_postings_list;
    }
    else{
      this.final_postings_list = null;
    }
  }

  public static boolean isSorted(ArrayList<Integer> A){
    boolean result = true;

    for(int i=0; i<A.size()-1; i++){
      if(A.get(i) > A.get(i+1)){
        return false;
      }
    }

    return result;
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
    int dl = index.getDocumentLength(doc_id) - this.window_size + 1;
    long cl = index.getTotalWordCount() - (long)(this.window_size - 1)*(this.index.getDocumentCount());

    MultiPositionDocPostings doc_postings = this.getPostingsListByDocID(doc_id);

    int tf = (doc_postings != null) ? doc_postings.getWindowFrequency() : 0;
    long c = this.getTotalWindowCount();

    double score = 0.0;

    double mu = OrderedWindowNode.mu;

    double num = (double)tf + mu*((double)c/(double)cl);
    double den = (double)dl + (double)mu;

    return Math.log(num/den);

  }
}
