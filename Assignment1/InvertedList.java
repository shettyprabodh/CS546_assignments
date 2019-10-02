import java.util.*;
import java.nio.*;
import java.io.*;

public class InvertedList{
  ArrayList<DocumentPostings> doc_postings = null;

  // Lookup table params
  long offset = 0;
  int num_bytes = 0;

  // Term related meta data
  int term_frequency = 0;
  boolean is_delta_encoded = false;

  InvertedList(){
    this.doc_postings = new ArrayList<DocumentPostings>();
    this.term_frequency = 0;
    this.offset = 0;
    this.num_bytes = 0;
    this.is_delta_encoded = false;
  }

  public void addPosting(Integer doc_id, Integer position){
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

    correct_doc_postings.addPosition(position);

    // Update meta data
    this.term_frequency++;
  }

  // NOTE: Did not delta encode doc_ids list
  public void deltaEncodePostings(){
    for(int i=0; i<this.doc_postings.size(); i++){
      DocumentPostings current_doc_postings = this.doc_postings.get(i);
      current_doc_postings.deltaEncodePositions();
    }

    this.is_delta_encoded = true;
  }

  // Get size of encoded list
  private int getEncodedListSize(){
    int num_of_integers = 0;
    for(int i=0; i<this.doc_postings.size(); i++){
      DocumentPostings current_doc_postings = this.doc_postings.get(i);
      num_of_integers += (2 + current_doc_postings.positions.size());
    }

    return num_of_integers;
  }

  // TODO: Need to complete v_byte compression
  private int[] getEncodedList(){
    int[] encoded_list = new int[this.getEncodedListSize()];

    int encoded_list_pointer = 0;
    for(int i=0; i<this.doc_postings.size(); i++){
      DocumentPostings current_doc_postings = this.doc_postings.get(i);
      ArrayList<Integer> positions = current_doc_postings.positions;

      encoded_list[encoded_list_pointer++] = current_doc_postings.doc_id;
      encoded_list[encoded_list_pointer++] = current_doc_postings.document_term_frequency;
      for(int j=0; j<positions.size(); j++){
        encoded_list[encoded_list_pointer++] = positions.get(j);
      }
    }

    // TODO: Need to complete v_byte compression

    return encoded_list;
  }

  // To get offset and number_of_bytes, check updated offset and num_bytes
  // Writing to disk in following format [doc_id_1 count_1 positions_1
  // doc_id_2 count_2 positions_2 ....]
  public void flushToDisk(RandomAccessFile writer){
    if(writer == null){
      System.out.println("No writer found. Exiting.");
      System.exit(1);
    }

    try{
      long current_offset = writer.getFilePointer();
      int[] encoded_list = this.getEncodedList();
      System.out.println("============ Encoded list =============="+encoded_list[0]);
      int num_bytes = (encoded_list.length)*(Integer.BYTES);
      ByteBuffer byte_buffer = ByteBuffer.allocate(num_bytes);
      IntBuffer int_buffer = byte_buffer.asIntBuffer();
      int_buffer.put(encoded_list);

      this.offset = current_offset;
      this.num_bytes = num_bytes;

      byte[] b_array = byte_buffer.array();
      writer.write(b_array);
    }
    catch(IOException e){
      System.out.println(e);
    }
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
