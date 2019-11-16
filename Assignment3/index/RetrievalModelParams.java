package index;

// Class to store all kinds of parameters for RetrievalModel
public class RetrievalModelParams{
  int total_document_count = 0;
  double average_document_length = 0;
  int current_document_length = 0;

  // BM25 parameters
  int qf = 0;
  int ni = 0;
  int tf = 0;
  // BM25 Hyperparameters
  double k1 = 1.2;
  double k2 = 700;
  double b = 0.75;


  // Language model params
  long total_word_count = 0;
  long total_term_count = 0;
  double lambda = 0.1;
  double mu = 1500;


  @Override
  public String toString(){
    String result = "";

    result += "{";
    result += "total_document_count: " + total_document_count + ", ";
    result += "average_document_length: " + average_document_length + ", ";
    result += "current_document_length: " + current_document_length + ", ";
    result += "qf: " + qf + ", ";
    result += "ni: " + ni + ", ";
    result += "tf: " + tf + ", ";
    result += "k1: " + k1 + ", ";
    result += "k2: " + k2 + ", ";
    result += "b: " + b + ", ";
    result += "total_word_count: " + total_word_count + ", ";
    result += "total_term_count: " + total_term_count + ", ";
    result += "lambda: " + lambda + ", ";
    result += "mu: " + mu + ", ";
    result += "}";
    // result += "\n";

    return result;
  }
}
