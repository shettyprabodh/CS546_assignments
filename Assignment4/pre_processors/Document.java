package pre_processors;

import java.util.Arrays;

public class Document{
  public String play_id = "";
  public String scene_id = "";
  public String[] terms = null;
  public long scene_num = 0;
  public Integer doc_id = 0;

  Document(String play_id, String scene_id, String[] tokenized_text, long scene_num, Integer doc_id){
    this.play_id = play_id;
    this.scene_id = scene_id;
    this.terms = tokenized_text;
    this.scene_num = scene_num;
    this.doc_id = doc_id;
  }

  // For debugging purposes
  @Override
  public String toString(){
    return getClass().getSimpleName() + "\n{play_id: " + play_id + "\nscene_id: " + scene_id + "\nterms: " + Arrays.toString(terms) + "\nscene_num: " + scene_num + "\ndoc_id: " + doc_id + "}";
  }
}
