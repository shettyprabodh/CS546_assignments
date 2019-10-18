package retrieval_models;

public class JelinikMercerModel extends LanguageModel{
  public static double score(int tf, int dl, int c, int cl, double lambda){
    double foreground_score = (double)tf/(double)dl;
    double background_score = (double)c/(double)cl;
    double alpha = lambda;

    return Math.log(LanguageModel.score(foreground_score, background_score, alpha));
  }
}
