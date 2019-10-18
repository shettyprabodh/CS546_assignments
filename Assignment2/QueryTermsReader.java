import java.util.*;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class QueryTermsReader{
  public static ArrayList<ArrayList<String>> fetchQueryTerms(String file_name){
    ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

    try{
      FileReader file = new FileReader(file_name);
      JSONParser parser = new JSONParser();
      JSONArray random_terms = (JSONArray) parser.parse(file);

      for(int i=0; i<random_terms.size(); i++){
        JSONArray current_random_terms = (JSONArray) random_terms.get(i);
        ArrayList<String> temp_list = new ArrayList<String>();

        for(int j=0; j<current_random_terms.size(); j++){
          temp_list.add((String)current_random_terms.get(j));
        }

        result.add(temp_list);
      }

      return result;
    }
    catch(ParseException e){
      System.out.println(e);
      return null;
    }
    catch(IOException e){
      System.out.println(e);
      return null;
    }
  }
}
