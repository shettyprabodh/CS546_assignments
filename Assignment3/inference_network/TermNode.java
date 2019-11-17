package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class TermNode extends ProximityNode{
  String term;
  InvertedIndex index;
  final static double mu = 1500.0;


  public TermNode(String term, InvertedIndex index){
    this.term = term;
    this.index = index;
  }

  public double score(int doc_id){
    InvertedIndex index = this.index;

    InvertedList current_inverted_list = index.getInvertedList(this.term);
    RandomAccessFile reader = index.getReader();
    double score = 0.0;

    InvertedList.readCompressionByte(reader);

    if(current_inverted_list == null){
      return score;
    }

    if(!current_inverted_list.arePostingsLoaded()){
      current_inverted_list.reconstructPostingsFromDisk(reader);
    }
    DocumentPostings doc_postings = current_inverted_list.getPostingsListByDocID(doc_id);

    int dl = index.getDocumentLength(doc_id);
    long cl = index.getTotalWordCount();

    int tf = (doc_postings != null) ? doc_postings.getDocumentTermFrequency() : 0;
    long c = current_inverted_list.getTotalTermCount(reader);

    double mu = TermNode.mu;

    double num = (double)tf + mu*((double)c/(double)cl);
    double den = (double)dl + (double)mu;

    return Math.log(num/den);
  }
}
