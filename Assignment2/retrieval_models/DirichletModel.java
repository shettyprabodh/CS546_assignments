package retrieval_models;

public class DirichletModel extends LanguageModel{
  public static double score(int tf, int dl, int c, int cl, double mu){
    double foreground_score = (double)tf/(double)dl;
    double background_score = (double)c/(double)cl;
    double alpha = mu/(dl+mu);

    return Math.log(LanguageModel.score(foreground_score, background_score, alpha));
  }
}
