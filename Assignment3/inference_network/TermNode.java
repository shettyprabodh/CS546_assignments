package inference_network;

import java.util.*;
import index.*;

public class TermNode extends ProximityNode{
  String term;
  InvertedIndex index;
  final static double mu = 1500.0;


  public TermNode(String term, InvertedIndex index){
    this.term = term;
    this.index = index;
  }

  public score(int doc_id){
    InvertedList current_inverted_list = (this.index.containsKey(query_term) ? (this.index.get(query_term)) : null);
    RandomAccessFile reader = this.index.getReader();
    double score = 0.0;

    if(current_inverted_list == null){
      return score;
    }

    if(current_inverted_list.arePostingsLoaded()){
      current_inverted_list.reconstructPostingsFromDisk(reader);
    }
    DocumentPostings doc_postings = this.getPostingsListByDocID(doc_id);

    int dl = this.index.getDocumentLength(doc_id);
    long cl = this.index.getTotalWordCount();

    int tf = (doc_postings != null) ? doc_postings.getDocumentTermFrequency() : 0;
    long c = current_inverted_list.getTotalTermCount(reader);

    double mu = TermNode.mu;

    double num = (double)tf + mu*((double)c/(double)cl);
    double den = (double)dl + (double)mu;

    return Math.log(num/den);
  }
}
