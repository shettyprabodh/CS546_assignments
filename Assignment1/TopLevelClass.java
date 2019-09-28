public class TopLevelClass{
  public static void main(String[] args){
    String file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(file_name);
    document_crawler.ParseJSON();

    System.out.println("====================== Parsing JSON done ======================");

    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.documents);

    System.out.println("Done");
  }
}
