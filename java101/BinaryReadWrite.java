import java.io.*;
import java.util.*;
import java.nio.*;

public class BinaryReadWrite{

  public void WriteToFile(){
    try{
      RandomAccessFile arrayWriter = new RandomAccessFile("temp_write_file.bin", "rw");
      int[] array = new int[]{1,2,3,4,5,10,-100,8,7,6};
      System.out.println(array.length + " " + Integer.BYTES);
      ByteBuffer byte_buffer = ByteBuffer.allocate(array.length * (Integer.BYTES));
      IntBuffer int_buffer = byte_buffer.asIntBuffer();
      int_buffer.put(array);

      byte[] b_array = byte_buffer.array();
      System.out.println(b_array);
      arrayWriter.write(b_array);

      long offset = arrayWriter.getFilePointer();
      arrayWriter.close();
    }
    catch (FileNotFoundException e){
      System.out.println(e);
    }
    catch (IOException e){
      System.out.println(e);
    }
  }

  public int byteArrayToInt(byte[] byte_array, int number_of_bytes){
    assert(byte_array.length == number_of_bytes);
    long shifts = 0; // 2^i bit shifts
    int result = 0;

    for(int i=number_of_bytes-1; i >= 0; i--){
      result = result | (byte_array[i]*(1 << shifts));
      shifts += 8;
    }

    return result;
  }

  public void ReadFromFile(){
    try{
      RandomAccessFile reader = new RandomAccessFile("temp_write_file.bin", "rw");
      int buff_length = 10*(Integer.BYTES);
      int offset = 0;

      byte[] temp_arr = new byte[buff_length];
      int bytesRead = reader.read(temp_arr, 0, buff_length);

      for(int off=0; off<temp_arr.length; off+=4){
        System.out.println(this.byteArrayToInt(Arrays.copyOfRange(temp_arr, off, off+4), Integer.BYTES));
      }
    }
    catch(FileNotFoundException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }
}
