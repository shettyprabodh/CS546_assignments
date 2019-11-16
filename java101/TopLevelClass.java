public class TopLevelClass{
  public static void main(String[] args){
    BinaryReadWrite temp_writer = new BinaryReadWrite();
    temp_writer.WriteToFile();

    temp_writer.ReadFromFile();
  }
}
