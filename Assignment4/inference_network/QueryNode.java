package inference_network;

public class QueryNode{
  public double score(int doc_id){
    return 0.0;
  }

  public boolean hasMore(){
    return true;
  }

  public int nextCandidate(){
    return 0;
  }

  public void skipTo(int doc_id){
    return;
  }
}
