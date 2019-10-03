import java.util.*;
import java.nio.*;
import java.io.*;

// More or less a lookup_table along with postings list.
// Whenever the lookup table is loaded, except for
// doc_postings everything is loaded.
public class InvertedList{
  ArrayList<DocumentPostings> doc_postings = null;

  // Lookup table params
  long offset = 0;
  int num_bytes = 0;

  // Term related meta data
  int term_frequency = 0;
  boolean is_delta_encoded = false;
  boolean is_v_byte_compressed = false;
  boolean are_postings_loaded = false;

  // For debugging purposes
  int[] encoded_list = null;
  int[] decoded_list = null;
  static int count = 0;

  InvertedList(){
    this.doc_postings = new ArrayList<DocumentPostings>();
    this.term_frequency = 0;
    this.offset = 0;
    this.num_bytes = 0;
    this.is_delta_encoded = false;
    this.are_postings_loaded = false;
  }

  InvertedList(long offset, int num_bytes, boolean is_delta_encoded, boolean is_v_byte_compressed){
    this.doc_postings = new ArrayList<DocumentPostings>();
    // Will be updated whenever postings list is loaded
    this.term_frequency = 0;
    this.offset = offset;
    this.num_bytes = num_bytes;
    this.is_delta_encoded = is_delta_encoded;
    this.are_postings_loaded = is_v_byte_compressed;
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
    this.are_postings_loaded = true;
  }

  // NOTE: Did not delta encode doc_ids list
  private void deltaEncodePostings(){
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
  private int[] getEncodedList(boolean is_compression_required){
    int[] encoded_list = new int[this.getEncodedListSize()];

    if(is_compression_required){
      this.deltaEncodePostings();
    }

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

  private void writeToDisk(int[] encoded_list, RandomAccessFile writer){
    try{
      long current_offset = writer.getFilePointer();
      int num_bytes = (encoded_list.length)*(Integer.BYTES);
      ByteBuffer byte_buffer = ByteBuffer.allocate(num_bytes);
      IntBuffer int_buffer = byte_buffer.asIntBuffer();
      int_buffer.put(encoded_list);

      byte[] b_array = byte_buffer.array();
      writer.write(b_array);

      this.offset = current_offset;
      this.num_bytes = (int)(writer.getFilePointer() - current_offset);
      this.encoded_list = encoded_list;
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  // To get offset and number_of_bytes, check updated offset and num_bytes
  // Writing to disk in following format [doc_id_1 count_1 positions_1
  // doc_id_2 count_2 positions_2 ....]
  public void flushToDisk(RandomAccessFile writer, boolean is_compression_required){
    if(writer == null){
      System.out.println("No writer found. Exiting.");
      System.exit(1);
    }

    int[] encoded_list = this.getEncodedList(is_compression_required);
    this.writeToDisk(encoded_list, writer);
  }

  private byte[] readFromDisk(RandomAccessFile reader){
    byte[] b_array = new byte[this.num_bytes];
    int offset = (int)(this.offset);
    try{
      reader.seek(offset);
      reader.read(b_array, 0, this.num_bytes);
    }
    catch(IOException e){
      System.out.println(e);
    }

    return b_array;
  }

  private int byteArrayToInt(byte[] byte_array){
    assert(byte_array.length == Integer.BYTES);
    int shifts = 0; // 2^i bit shifts
    int result = 0;

    for(int i=Integer.BYTES - 1; i >= 0; i--){
      // Magic. Without "& 0xFF", we are getting negative numbers in some cases.
      result = result | ((byte_array[i]&(0xFF))*(1 << shifts));
      shifts += 8;
    }

    return result;
  }

  // TODO: Need to handle v_byte decoding
  private int[] getDecodedList(byte[] byte_array){
    int[] decoded_list = new int[this.num_bytes];

    // Uncompressed version
    int decoded_list_pointer = 0;
    for(int i=0; i<byte_array.length; i+=4){
      decoded_list[decoded_list_pointer++] = this.byteArrayToInt(Arrays.copyOfRange(byte_array, i, i+4));
    }

    return decoded_list;
  }

  public void reconstructInvertedListFromDisk(RandomAccessFile reader){
    if(reader == null){
      System.out.println("No reader found. Exiting.");
      System.exit(1);
    }
    count = 0;
    byte[] byte_array = this.readFromDisk(reader);
    int[] decoded_list = this.getDecodedList(byte_array);
    this.decoded_list = decoded_list;
  }

  @Override
  public String toString(){
    String result = "";

    result += "{ ";
    result += ("term_frequency: " + term_frequency + "\n");
    result += ("doc_postings: " + doc_postings.toString() + "\n");
    result += ("offset: " + offset + "\n");
    result += ("num_bytes: " + num_bytes + "\n");
    // result += ("encoded_list: " + encoded_list[0] + "\n");
    // result += ("decoded_list: " + decoded_list[0] + "\n");
    result += "}";
    result += "\n";

    return result;
  }
}
