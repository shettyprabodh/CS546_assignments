import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class Crawler{
  String file_name = null;
  JSONParser parser = null;
  FileReader reader = null;
  File file = null;
  ArrayList<Document> documents = null;

  Crawler(String file_name){
    this.file_name = file_name;
    this.file = new File(file_name);
    this.parser = new JSONParser();
    this.documents = new ArrayList<Document>();
  }

  public void ParseJSON(){
    File temp_file = this.file;
    try
    {
        this.reader = new FileReader(temp_file);
        JSONObject parsed_file = (JSONObject) this.parser.parse(this.reader);
        JSONArray corpus = (JSONArray)parsed_file.get("corpus");

        for(int i=0; i<corpus.size(); i++){
          JSONObject current_document = (JSONObject) corpus.get(i);

          String text = (String) current_document.get("text");
          String play_id = (String) current_document.get("playId");
          String scene_id = (String) current_document.get("sceneId");
          long scene_num = (long) current_document.get("sceneNum");
          long doc_id = i;

          String[] tokenized_text = text.split(" ");

          this.documents.add(new Document(play_id, scene_id, tokenized_text, scene_num, doc_id));
        }

        System.out.println(this.documents.get(0));
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }
  }
}
