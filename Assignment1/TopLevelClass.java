public class TopLevelClass{
  public static void main(String[] args){
    String file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");

    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.documents, "index.bin", "lookup_table.json");
    shakespeare_index.createIndex();

    System.out.println("====================== Created Inverted Index ======================");
    // System.out.println(shakespeare_index);

    shakespeare_index.write();

    System.out.println("====================== Wrote Inverted Index ======================");
    // Assume "delete shakespeare_index" here
    // shakespeare_index.read();
    InvertedIndex new_shakespeare_index = new InvertedIndex("index.bin", "lookup_table.json");

    new_shakespeare_index.loadLookupTable();

    System.out.println("====================== Lookup table loaded ======================");

    System.out.println("Done");
  }
}
