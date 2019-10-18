package retrieval_models;

import java.lang.*;

public class BM25{
  public static double score(int N, int ni, int tfi, int qfi, double k1, double k2, int dl, double avdl, double b){
    double K = BM25.getK(k1, b, dl, avdl);
    double term1 = (double)(N-ni+0.5)/(double)(ni+0.5);
    double term2 = (double)((k1+1.0)*tfi)/(double)(K+tfi);
    double term3 = (double)((k2+1.0)*qfi)/(double)(k2*qfi);

    return Math.log(term1) + Math.log(term2) + Math.log(term3);
  }

  private static double getK(double k1, double b, int dl, double avdl){
    return k1*((1-b) + b*((double)dl/avdl));
  }
}
