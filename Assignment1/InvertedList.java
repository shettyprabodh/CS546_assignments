import java.util.*;
import java.nio.*;
import java.io.*;
import exceptions.*;

// More or less a lookup_table along with postings list.
// Whenever the lookup table is loaded, except for
// postings everything is loaded.
public class InvertedList{
  private ArrayList<DocumentPostings> postings = null;
  private int current_postings_pointer = 0;
  private boolean are_postings_loaded = false;

  // Lookup table params
  private long offset = 0;
  private int num_bytes = 0;

  // Term related meta data
  int term_frequency = 0;

  // For debugging purposes
  int[] encoded_list = null;
  ArrayList<Integer> decoded_list = null;

  InvertedList(){
    this.postings = new ArrayList<DocumentPostings>();
    this.current_postings_pointer = 0;
    this.term_frequency = 0;
    this.offset = 0;
    this.num_bytes = 0;
    this.are_postings_loaded = false;
  }

  InvertedList(long offset, int num_bytes){
    this.postings = new ArrayList<DocumentPostings>();
    this.current_postings_pointer = 0;
    // Will be updated whenever postings list is loaded
    this.term_frequency = 0;
    this.offset = offset;
    this.num_bytes = num_bytes;
    this.are_postings_loaded = false;
  }

  public long getOffset(){
    return this.offset;
  }

  public int getNumBytes(){
    return this.num_bytes;
  }

  public int getTermFrequency(RandomAccessFile reader){
    if(!this.arePostingsLoaded()){
      this.reconstructPostingsFromDisk(reader);
    }
    int total_count = 0;

    for(int i=0; i<this.postings.size(); i++){
      total_count += (this.postings.get(i).getDocumentTermFrequency());
    }

    return total_count;
  }

  public int getDocumentCount(RandomAccessFile reader){
    if(!this.arePostingsLoaded()){
      this.reconstructPostingsFromDisk(reader);
    }

    return this.postings.size();
  }

  // Reason for not using current_postings_pointer is because, if
  // for some reason, someone iterates through postings
  // list before indexing all the data,
  // current_postings_pointer might point to some
  // postings other than the last one and thus breaking the ordering
  // of postings list.
  public void addPosting(Integer doc_id, Integer position){
    DocumentPostings correct_doc_postings = null;

    // Search for doc posting
    // TODO: Can be improved using HashSet or using the ordering of doc_ids
    for(int i=0; i<this.postings.size(); i++){
      DocumentPostings doc_postings = this.postings.get(i);
      if(doc_postings.getDocId() == doc_id){
        correct_doc_postings = doc_postings;
        break;
      }
    }

    if(correct_doc_postings == null){
      correct_doc_postings = new DocumentPostings(doc_id);
      this.postings.add(correct_doc_postings);
    }

    correct_doc_postings.addPosition(position);

    // Update meta data
    this.term_frequency++;

    // Technically, adding posting basically means postings are already loaded
    //  into memory. Nevertheless, this might lead to some bugs.
    this.are_postings_loaded = true;
  }

  // NOTE: Did not delta encode doc_ids list
  private void deltaEncodePostings(){
    for(int i=0; i<this.postings.size(); i++){
      DocumentPostings doc_postings = this.postings.get(i);
      doc_postings.deltaEncodePositions();
    }
  }

  // Get size of encoded list
  private int getEncodedListSize(){
    int num_of_integers = 0;
    for(int i=0; i<this.postings.size(); i++){
      DocumentPostings doc_postings = this.postings.get(i);
      num_of_integers += (2 + doc_postings.getPositionsSize());
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
    for(int i=0; i<this.postings.size(); i++){
      DocumentPostings doc_postings = this.postings.get(i);
      ArrayList<Integer> positions = doc_postings.getAllPositions();

      encoded_list[encoded_list_pointer++] = doc_postings.getDocId();
      encoded_list[encoded_list_pointer++] = doc_postings.getDocumentTermFrequency();
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
    long offset = this.offset;
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
  // int[] needs size before hand. Since calculating it is a bit pain,
  // I am using ArrayList instead.
  private ArrayList<Integer> getDecodedList(byte[] byte_array){
    ArrayList<Integer> decoded_list = new ArrayList<Integer>();

    // Uncompressed version
    int decoded_list_pointer = 0;
    for(int i=0; i<byte_array.length; i+=4){
      decoded_list.add(this.byteArrayToInt(Arrays.copyOfRange(byte_array, i, i+4)));
    }

    return decoded_list;
  }

  // Decoded list format [doc_id dtf positions ...]
  private void reconstructPostings(ArrayList<Integer> decoded_list){
    int decoded_list_pointer = 0;
    while(decoded_list_pointer<decoded_list.size()){
      int doc_id = decoded_list.get(decoded_list_pointer++);
      int dtf = decoded_list.get(decoded_list_pointer++);
      ArrayList<Integer> positions = new ArrayList<Integer>();

      for(int i=0; i<dtf; i++){
        positions.add(decoded_list.get(decoded_list_pointer++));
      }

      DocumentPostings doc_postings = new DocumentPostings(doc_id, false, positions);
      this.postings.add(doc_postings);
      this.term_frequency += dtf;
    }
  }

  public void reconstructPostingsFromDisk(RandomAccessFile reader){
    if(reader == null){
      System.out.println("No reader found. Exiting.");
      System.exit(1);
    }
    byte[] byte_array = this.readFromDisk(reader);
    ArrayList<Integer> decoded_list = this.getDecodedList(byte_array);
    this.decoded_list = decoded_list;
    this.reconstructPostings(decoded_list);
    this.are_postings_loaded = true;
  }


  // Utility functions
  public boolean isPostingsListEmpty(){
    return this.postings == null;
  }

  public boolean arePostingsLoaded(){
    return this.are_postings_loaded;
  }

  public DocumentPostings getCurrentPostings(){
    if(!this.isPostingsListEmpty()){
      return this.postings.get(current_postings_pointer);
    }
    else{
      return null;
    }
  }

  public boolean hasMorePostings(){
    return (!this.isPostingsListEmpty()) && (this.current_postings_pointer < this.postings.size());
  }

  // Reset postings pointer to start of postings list if it exists
  // Always reset before traversing postings list
  // Usage:-
  //
  // IL.resetPointer()
  // while(IL.hasMorePostings()){
  // current_postings = IL.getCurrentPostings();
  // .....
  // IL.setPointerToNextPostings();
  // }
  public void resetPointer(){
    this.current_postings_pointer = 0;
  }

  public void setPointerToNextPostings(){
    this.current_postings_pointer++;
  }

  // Returns DocumentPostings for given doc_id.
  // Returns null if it doesn't exist.
  public DocumentPostings getPostingsListByDocID(int doc_id){
    if(this.postings == null){
      return null;
    }

    // Because doc_ids are ordered, using binary search.
    int start_pointer = 0;
    int end_pointer = this.postings.size() - 1;
    int mid_pointer = 0;

    while(start_pointer <= end_pointer){
      mid_pointer = (start_pointer + end_pointer) / 2;
      DocumentPostings mid_postings = this.postings.get(mid_pointer);
      if(mid_postings.getDocId() == doc_id){
        return mid_postings;
      } else if (mid_postings.getDocId() < doc_id){
        start_pointer = mid_pointer + 1;
      }
      else{
        end_pointer = mid_pointer - 1;
      }
    }

    return null;
  }

  // Get score corresponding to given term(i.e this InvertedList) and doc_id.
  // If doc_id doesn't exist return 0
  // Scoring: Count based
  public Integer getDocumentWiseScore(int doc_id, RandomAccessFile reader){
    if(!this.arePostingsLoaded()){
      this.reconstructPostingsFromDisk(reader);
    }

    DocumentPostings doc_postings = this.getPostingsListByDocID(doc_id);
    return (doc_postings != null) ? doc_postings.getDocumentTermFrequency() : 0;
  }

  // term-at-a-time based querying
  // Returns ArrayList of scores of the form [doc_id_1 score_1 doc_id_2 score_2 ...]
  // Scoring: Count based
  public ArrayList<Integer> getDocumentWiseScores(RandomAccessFile reader){
    ArrayList<Integer> document_scores = new ArrayList<Integer>();

    // Postings list not loaded. Load it first.
    if(!this.arePostingsLoaded()){
      this.reconstructPostingsFromDisk(reader);
      // System.out.println("Postings list has not been read. Use reconstructPostingsFromDisk first");
      // throw new PostingsListNotLoadedException("Postings list has not been read. Use reconstructPostingsFromDisk first");
    }

    this.resetPointer();
    while(this.hasMorePostings()){
      DocumentPostings current_postings = this.getCurrentPostings();
      document_scores.add(current_postings.getDocId());
      document_scores.add(current_postings.getDocumentTermFrequency());
      this.setPointerToNextPostings();
    }

    return document_scores;
  }

  @Override
  public String toString(){
    String result = "";

    result += "{ ";
    result += ("term_frequency: " + term_frequency + "\n");
    result += ("postings: " + postings.toString() + "\n");
    result += ("offset: " + offset + "\n");
    result += ("num_bytes: " + num_bytes + "\n");
    // result += ("encoded_list: " + encoded_list[0] + "\n");
    // result += ("decoded_list: " + Arrays.toString(decoded_list.toArray()) + "\n");
    // result += ("decoded_list_length: " + decoded_list.size() + "\n");
    result += "}";
    result += "\n";

    return result;
  }
}
