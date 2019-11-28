package index;

import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class DocumentVector{
  private Hashtable<String, Double> count_map;

  public DocumentVector(){
    this.count_map = new Hashtable<String, Double>();
  }

  public void addWord(String word){
    Double current_word_count = 0.0;
    if(this.count_map.containsKey(word)){
      current_word_count = this.count_map.get(word);
    }
    this.count_map.put(word, current_word_count+1.0);
  }

  public void setWordCount(String word, Double count){
    this.count_map.put(word, count);
  }

  public void convertToTfIdfScore(Hashtable<String, Integer> doc_count_map, Integer N){
    Set<String> words = this.count_map.keySet();

    for(String word: words){
      Double tf = this.count_map.get(word);
      Double ni = doc_count_map.containsKey(word) ? doc_count_map.get(word).doubleValue() : 0.0;

      Double idf = Math.log((N.doubleValue()+1.0)/(ni+0.5));

      this.count_map.put(word, tf*idf);
    }
  }

  public Double dot(DocumentVector other){
    return 1.0;
  }

  public JSONObject getJSON(){
    JSONObject converted_object = new JSONObject();
    Set<String> words = this.count_map.keySet();

    for(String word: words){
      Double count = this.count_map.get(word);

      converted_object.put(word, count);
    }

    return converted_object;
  }
}
