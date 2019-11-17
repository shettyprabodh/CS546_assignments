package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class OrderedWindowNode extends WindowNode{
  public OrderedWindowNode(ArrayList<TermNode> children, int window_size, InvertedIndex index){
    super(children, window_size, index);
    this.initialize();
  }

  public void initialize(){
    RandomAccessFile reader = this.index.getReader();
    InvertedList.readCompressionByte(reader);
    this.loadInvertedList();
    this.constructPostingList();
  }

  public void loadInvertedList(){
    for(int i=0; i<this.children.size(); i++){
      InvertedList current_child = this.children.get(i).inverted_list;
      if(!current_child.arePostingsLoaded()){
        current_child.reconstructPostingsFromDisk(this.index.getReader());
      }
    }
  }

  public void constructPostingList(){

  }

  public int nextCandidate(){
    return 1;
  }

  public void skipTo(int doc_id){

  }

  public boolean hasMore(){
    return false;
  }

  public double score(int doc_id){
    return 1.0;
  }
}
