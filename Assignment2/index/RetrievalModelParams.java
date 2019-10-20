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
}
