package index;

import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.io.*;

public class DocumentVectorMap{
  private Hashtable<Integer, DocumentVector> document_vector_map;
  private Integer no_of_docs;
  private boolean loaded;

  static String file_name = "doc_vec_map.json";

  public DocumentVectorMap(){
    this.document_vector_map = new Hashtable<Integer, DocumentVector>();
    this.no_of_docs = 0;
    this.loaded = false;
  }

  public boolean isLoaded(){
    return this.loaded;
  }

  public String getFileName(){
    return this.file_name;
  }

  public Set<Integer> getDocIds(){
    return this.document_vector_map.keySet();
  }

  public DocumentVector getDocumentVector(Integer doc_id){
    return this.document_vector_map.get(doc_id);
  }


  public void addDocument(Integer doc_id){
    if(this.document_vector_map.containsKey(doc_id)){
      System.out.println("Doc id already exists. Exiting.");
      System.exit(1);
    }

    this.document_vector_map.put(doc_id, new DocumentVector());
    this.no_of_docs++;
    this.loaded = true;
  }

  public void addWord(Integer doc_id, String word){
    if(!this.document_vector_map.containsKey(doc_id)){
      this.addDocument(doc_id);
    }

    DocumentVector current_doc_vec = this.document_vector_map.get(doc_id);
    current_doc_vec.addWord(word);
  }

  public void convertToTfIdfScore(Hashtable<String, Integer> doc_count_map){
    Set<Integer> doc_ids = this.document_vector_map.keySet();

    for(Integer doc_id: doc_ids){
      this.document_vector_map.get(doc_id).convertToTfIdfScore(doc_count_map, this.no_of_docs);
    }
  }

  public void normalize(){
    Set<Integer> doc_ids = this.document_vector_map.keySet();

    for(Integer doc_id: doc_ids){
      this.document_vector_map.get(doc_id).normalize();
    }
  }



  public void read(){
    try{
      FileReader file = new FileReader(this.getFileName());
      JSONParser parser = new JSONParser();
      JSONObject term_count_map = (JSONObject) parser.parse(file);

      Set<String> doc_ids = term_count_map.keySet();

      for(String doc_id: doc_ids){
        DocumentVector doc_vec = new DocumentVector((JSONObject)term_count_map.get(doc_id));
        this.document_vector_map.put(Integer.parseInt(doc_id), doc_vec);
      }

      this.loaded = true;
    }
    catch(ParseException e){
      System.out.println(e);
    }
    catch(IOException e){
      System.out.println(e);
    }
  }

  public void write(){
    Set<Integer> doc_ids = this.document_vector_map.keySet();
    JSONObject doc_vec_map = new JSONObject();

    for(Integer doc_id: doc_ids){
      DocumentVector current_doc_vec = this.document_vector_map.get(doc_id);
      JSONObject json_doc_vec = current_doc_vec.getJSON();

      doc_vec_map.put(doc_id, json_doc_vec);
    }

    try(FileWriter file = new FileWriter(this.getFileName())){
      file.write(doc_vec_map.toJSONString());
      file.flush();
      file.close();
    }
    catch(IOException e){
      System.out.println(e);
    }
  }
}
