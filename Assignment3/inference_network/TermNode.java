package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class TermNode extends ProximityNode{
  String term;
  InvertedIndex index;
  InvertedList inverted_list;

  int current_postings_id;
  int current_doc_id;

  final static double mu = 1500.0;


  public TermNode(String term, InvertedIndex index){
    this.term = term;
    this.index = index;
    this.inverted_list = index.getInvertedList(term);
    this.initialize();
  }

  public void initialize(){
    RandomAccessFile reader = this.index.getReader();
    InvertedList.readCompressionByte(reader);
    if(!this.inverted_list.arePostingsLoaded()){
      this.inverted_list.reconstructPostingsFromDisk(this.index.getReader());
    }

    this.current_postings_id = 0;
    this.current_doc_id = this.inverted_list.postings.get(this.current_postings_id).getDocId();
  }

  public int nextCandidate(){
    return this.current_doc_id;
  }

  public void skipTo(int doc_id){
    while((this.current_doc_id < doc_id) && (this.current_postings_id < this.inverted_list.postings.size())){
      this.current_postings_id++;
      this.current_doc_id = (this.current_postings_id < this.inverted_list.postings.size()) ? this.inverted_list.postings.get(this.current_postings_id).getDocId() : this.current_doc_id;
    }
  }

  public boolean hasMore(){
    return (this.current_postings_id < this.inverted_list.postings.size());
  }

  public double score(int doc_id){
    InvertedIndex index = this.index;
    RandomAccessFile reader = index.getReader();
    double score = 0.0;

    DocumentPostings doc_postings = this.inverted_list.getPostingsListByDocID(doc_id);

    int dl = index.getDocumentLength(doc_id);
    long cl = index.getTotalWordCount();

    int tf = (doc_postings != null) ? doc_postings.getDocumentTermFrequency() : 0;
    long c = this.inverted_list.getTotalTermCount(reader);

    double mu = TermNode.mu;

    double num = (double)tf + mu*((double)c/(double)cl);
    double den = (double)dl + (double)mu;

    return Math.log(num/den);
  }
}
