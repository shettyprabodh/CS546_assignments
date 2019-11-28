package inference_network;

import java.util.*;
import index.*;
import java.nio.*;
import java.io.*;

public class WindowNode extends ProximityNode{
  ArrayList<TermNode> children;
  int window_size;
  InvertedIndex index;
  ArrayList<MultiPositionDocPostings> final_postings_list;

  public WindowNode(ArrayList<TermNode> children, int window_size, InvertedIndex index){
    super();
    this.children = children;
    this.window_size = window_size;
    this.index = index;
    this.final_postings_list = null;
    this.initialize();
  }

  public void loadInvertedList(){
    for(int i=0; i<this.children.size(); i++){
      InvertedList current_child = this.children.get(i).getInvertedList();
      if(!current_child.arePostingsLoaded()){
        current_child.reconstructPostingsFromDisk(this.index.getReader());
      }
    }
  }

  public void initialize(){
    RandomAccessFile reader = this.index.getReader();
    InvertedList.readCompressionByte(reader);
    this.loadInvertedList();
    this.constructPostingList();
  }

  public void constructPostingList(){
    if(this.children.size() == 1){
      this.final_postings_list = this.children.get(0).getNewInvertedList();
      return;
    }

    ArrayList<MultiPositionDocPostings> final_postings_list = this.getCommonPostingList(this.children.get(0).getNewInvertedList(), this.children.get(1).getNewInvertedList());

    for(int i=2; i<this.children.size(); i++){
      final_postings_list = this.getCommonPostingList(final_postings_list, this.children.get(i).getNewInvertedList());
    }

    this.final_postings_list = final_postings_list;
  }

  public ArrayList<MultiPositionDocPostings> getCommonPostingList(ArrayList<MultiPositionDocPostings> IL_1, ArrayList<MultiPositionDocPostings> IL_2){
    int window_size = this.window_size;
    ArrayList<MultiPositionDocPostings> final_postings_list = new ArrayList<MultiPositionDocPostings>();
    int IL_1_postings_pointer = 0, IL_2_postings_pointer = 0;

    while(IL_1_postings_pointer < IL_1.size() && IL_2_postings_pointer < IL_2.size()){
      MultiPositionDocPostings pl_1 = IL_1.get(IL_1_postings_pointer);
      MultiPositionDocPostings pl_2 = IL_2.get(IL_2_postings_pointer);

      int doc_id_1 = pl_1.getDocId();
      int doc_id_2 = pl_2.getDocId();

      if(doc_id_1 == doc_id_2){
        MultiPositionDocPostings intersected_list = UnorderedWindowNode.getIntersectedList(pl_1, pl_2, window_size);

        if(intersected_list != null){
          final_postings_list.add(intersected_list);
        }

        IL_1_postings_pointer++;
        IL_2_postings_pointer++;
      }
      else{
        if(doc_id_1 < doc_id_2){
          IL_1_postings_pointer++;
        }
        else{
          IL_2_postings_pointer++;
        }
      }
    }

    return final_postings_list;
  }

  public static MultiPositionDocPostings getIntersectedList(MultiPositionDocPostings pl_1, MultiPositionDocPostings pl_2, int window_size){
    MultiPositionDocPostings final_postings = null;
    int doc_id = pl_1.getDocId();
    ArrayList< ArrayList<Integer> > positions_1 = pl_1.getPositions();
    ArrayList< ArrayList<Integer> > positions_2 = pl_2.getPositions();
    ArrayList< ArrayList<Integer> > common_positions = new ArrayList< ArrayList<Integer> >();
    ArrayList< Boolean > taken = new ArrayList< Boolean >(Collections.nCopies(positions_2.size(), false));

    for(int i=0; i<positions_1.size(); i++){
      for(int j=0; j<positions_2.size(); j++){
        if((!taken.get(j)) && UnorderedWindowNode.isInsideWindow(positions_1.get(i), positions_2.get(j), window_size)){
          ArrayList<Integer> temp = new ArrayList<Integer>();

          for(int i_p=0; i_p<positions_1.get(i).size(); i_p++){
            temp.add(positions_1.get(i).get(i_p));
          }
          for(int i_p=0; i_p<positions_2.get(j).size(); i_p++){
            temp.add(positions_2.get(j).get(i_p));
          }

          common_positions.add(temp);
          taken.set(j, true);
        }
      }
    }

    if(common_positions.size() > 0){
      final_postings = new MultiPositionDocPostings(doc_id);
      final_postings.setPositions(common_positions);
    }

    return final_postings;
  }

  public static boolean isInsideWindow(ArrayList<Integer> positions_1, ArrayList<Integer> positions_2, int window_size){
    int min_1 = Collections.min(positions_1);
    int max_1 = Collections.max(positions_1);

    int min_2 = Collections.min(positions_2);
    int max_2 = Collections.max(positions_2);

    int lower_bound = Math.min(min_1, min_2);
    int upper_bound = Math.max(max_1, max_2);

    boolean inside_window = (Math.abs(upper_bound - lower_bound) + 1 <= window_size);

    ArrayList<Integer> common = new ArrayList<Integer>(positions_1);
    common.retainAll(positions_2);
    boolean double_dipping = (common.size() > 0);

    return (inside_window && (!double_dipping));
  }

  public MultiPositionDocPostings getPostingsListByDocID(int doc_id){
    if(this.final_postings_list == null){
      return null;
    }

    for(int i=0; i<this.final_postings_list.size(); i++){
      MultiPositionDocPostings current_list = this.final_postings_list.get(i);
      if(current_list.getDocId() == doc_id){
        return current_list;
      }
    }

    return null;
  }

  public long getTotalWindowCount(){
    long total_window_count = 0;
    if(this.final_postings_list == null){
      return total_window_count;
    }

    for(int i=0; i<this.final_postings_list.size(); i++){
      total_window_count += (long)this.final_postings_list.get(i).getWindowFrequency();
    }

    return total_window_count;
  }
}
