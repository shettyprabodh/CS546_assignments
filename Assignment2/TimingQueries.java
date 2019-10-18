import java.util.*;
import index.*;

public class TimingQueries{
  public static void printTimingForQueries(InvertedIndex II, ArrayList<ArrayList<String>> random_terms){
    double total_time_milli = 0.0;

    for(int i=0; i<random_terms.size(); i++){
      ArrayList<String> current_query_terms = random_terms.get(i);
      String query = "";
      for(int j=0; j<current_query_terms.size(); j++){
        query += current_query_terms.get(j) + " ";
      }

      long start_time = System.nanoTime();
      ArrayList<Integer> temp = II.getScores(query, 10);
      long end_time = System.nanoTime();

      double duration = (double)(end_time - start_time)/1000000.0;

      // System.out.println("Time taken(milli seconds) for query: " + query + " : " + duration);

      total_time_milli += duration;
    }
    double avg_time_milli = (total_time_milli)/(random_terms.size());
    System.out.println("Average time(milli seconds) for given index is: " + avg_time_milli);

  }
}
