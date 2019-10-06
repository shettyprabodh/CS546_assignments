import java.util.*;

public class RandomListGenerator{
  public static Set<String> getRandomTermsWithoutRepetition(ArrayList<String> terms, int k){
    Random rand = new Random();
    Set<String> result = new HashSet<String>();

    // Keep k low, otherwise this loop might go on forever.
    while(result.size() < k){
      int random_index = rand.nextInt(terms.size());
      result.add(terms.get(random_index));
    }

    return result;
  }
}
