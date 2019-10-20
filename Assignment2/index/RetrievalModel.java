package index;

import java.lang.*;

public class RetrievalModel{

  // BM25 scoring
  public static double bm25Score(RetrievalModelParams params){
    // Param extraction
    int N = params.total_document_count;
    int ni = params.ni;
    int tfi = params.tf;
    int qfi = params.qf;
    double k1 = params.k1;
    double k2 = params.k2;
    int dl = params.current_document_length;
    double avdl = params.average_document_length;
    double b = params.b;

    double K = RetrievalModel.getK(k1, b, dl, avdl);
    double term1 = (double)(N-ni+0.5)/(double)(ni+0.5);
    double term2 = (double)((k1+1.0)*tfi)/(double)(K+tfi);
    double term3 = (double)((k2+1.0)*qfi)/(double)(k2*qfi);

    // Summing up logs might help in overflow problems
    return Math.log(term1) + Math.log(term2) + Math.log(term3);
  }

  private static double getK(double k1, double b, int dl, double avdl){
    return k1*((1-b) + b*((double)dl/avdl));
  }


  // Generic function for language models
  public static double genericLanguageModelScoring(double foreground_score, double background_score, double alpha){
    return (1-alpha)*foreground_score + alpha*background_score;
  }

  // Dirichlet scoring
  public static double dirichletScoring(RetrievalModelParams params){
    int tf = params.tf;
    int dl = params.current_document_length;
    long c = params.total_term_count;
    long cl = params.total_word_count;
    double mu = params.mu;

    double foreground_score = (double)tf/(double)dl;
    double background_score = (double)c/(double)cl;
    double alpha = mu/(dl+mu);

    return Math.log(RetrievalModel.genericLanguageModelScoring(foreground_score, background_score, alpha));
  }

  // Jelinik Mercer scoring
  public static double jelinikMercerScoring(RetrievalModelParams params){
    int tf = params.tf;
    int dl = params.current_document_length;
    long c = params.total_term_count;
    long cl = params.total_word_count;
    double lambda = params.lambda;

    double foreground_score = (double)tf/(double)dl;
    double background_score = (double)c/(double)cl;
    double alpha = lambda;

    return Math.log(RetrievalModel.genericLanguageModelScoring(foreground_score, background_score, alpha));
  }
}
