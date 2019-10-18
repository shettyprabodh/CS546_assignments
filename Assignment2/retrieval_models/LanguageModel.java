package retrieval_models;

import java.lang.*;

public class LanguageModel{
  public static double score(double foreground_score, double background_score, double alpha){
    return (1-alpha)*foreground_score + alpha*background_score;
  }
}
