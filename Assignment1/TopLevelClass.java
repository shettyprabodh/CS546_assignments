public class TopLevelClass{
  public static void main(String[] args){
    String file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");

    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.documents, "index.bin");
    shakespeare_index.createIndex();

    System.out.println("====================== Created Inverted Index ======================");
    // System.out.println(shakespeare_index);

    shakespeare_index.write();

    System.out.println("Done");
  }
}
