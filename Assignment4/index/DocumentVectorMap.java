package index;

import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.io.*;

public class DocumentVectorMap{
  Hashtable<Integer, DocumentVector> document_vector_map;
  static String file_name = "doc_vec_map.json";

  public DocumentVectorMap(){
    this.document_vector_map = new Hashtable<Integer, DocumentVector>();
  }

  // TODO: Directly parse JSONObject to Vector map
  // public DocumentVectorMap(){
  //
  // }

  public void addDocument(Integer doc_id){
    if(this.document_vector_map.containsKey(doc_id)){
      System.out.println("Doc id already exists. Exiting.");
      System.exit(1);
    }

    this.document_vector_map.put(doc_id, new DocumentVector());
  }

  public void addWord(Integer doc_id, String word){
    if(!this.document_vector_map.containsKey(doc_id)){
      this.addDocument(doc_id);
    }

    DocumentVector current_doc_vec = this.document_vector_map.get(doc_id);
    current_doc_vec.addWord(word);
  }

  public String getFileName(){
    return this.file_name;
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
