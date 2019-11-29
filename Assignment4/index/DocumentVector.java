package index;

import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class DocumentVector{
  private Hashtable<String, Double> count_map;

  public DocumentVector(){
    this.count_map = new Hashtable<String, Double>();
  }

  public DocumentVector(JSONObject json_doc_vec){
    this();

    Set<String> words = json_doc_vec.keySet();

    for(String word: words){
      this.count_map.put(word, (Double)json_doc_vec.get(word));
    }
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

  public Double getWordCount(String word){
    Double word_count = this.count_map.get(word);

    if(word_count != null){
      return word_count;
    }
    else{
      return 0.0;
    }
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

  public Double getVectorLength(){
    Double length = 0.0;
    Set<String> words = this.count_map.keySet();

    for(String word: words){
      Double word_score = this.count_map.get(word);
      length += word_score*word_score;
    }

    return Math.sqrt(length);
  }

  public void normalize(){
    Double length = this.getVectorLength();
    Set<String> words = this.count_map.keySet();

    for(String word: words){
      Double word_score = this.count_map.get(word);
      this.count_map.put(word, word_score/length);
    }
  }

  /*
    Assumes both this and other doc vecs are notmalized and uses tf-idf scoring.
    i.e convertToTfIdfScore and normalize has been called for by this and other.
  */
  public Double dot(DocumentVector other){
    Set<String> words = this.count_map.keySet();
    Double final_score = 0.0;

    for(String word: words){
      Double this_score = this.count_map.get(word);
      Double other_score = other.getWordCount(word);

      final_score += this_score*other_score;
    }

    return final_score;
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

  @Override
  public String toString(){
    String result = "";
    result += "\n{doc_vec: " + count_map + "}";
    return result;
  }
}
