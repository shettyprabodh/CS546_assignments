import exceptions.*;
import java.util.*;

public class TopLevelClass{
  public static void main(String[] args){
    String file_name = "shakespeare-scenes.json";

    Crawler document_crawler = new Crawler(file_name);
    document_crawler.parseJSON();

    System.out.println("====================== Parsing JSON done ======================");

    InvertedIndex shakespeare_index = new InvertedIndex(document_crawler.documents, "index.bin", "lookup_table.json", "data_statistics.json");
    shakespeare_index.createIndex();

    System.out.println("====================== Created Inverted Index ======================");
    // System.out.println(shakespeare_index);

    shakespeare_index.write(true);

    System.out.println("====================== Wrote Inverted Index ======================");
    // Assume "delete shakespeare_index" here
    // shakespeare_index.read();
    InvertedIndex new_shakespeare_index = new InvertedIndex("index.bin", "lookup_table.json", "data_statistics.json");

    new_shakespeare_index.loadLookupTable();

    System.out.println("====================== Lookup table loaded ======================");

    // new_shakespeare_index.rebuildIndex();
    //
    // System.out.println("====================== Index rebuilt ======================");

    // System.out.println(new_shakespeare_index);
    System.out.println(Arrays.toString(new_shakespeare_index.getScores("the frolic cern", 10).toArray()));

    System.out.println("====================== Querying done ======================");

    System.out.println("DC: " + new_shakespeare_index.getDicesCoefficient("the", "noble"));

    System.out.println("====================== Dice's coefficient done ======================");

    System.out.println("Done");
  }
}
