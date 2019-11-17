package inference_network;

public class QueryNode{
  double score(int doc_id){
    return 0.0;
  }

  boolean hasMore(){
    return false;
  }

  int getCurrentDocID(){
    return 0;
  }

  void skipTo(int doc_id){
    return;
  }
}
