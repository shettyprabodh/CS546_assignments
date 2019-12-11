package retrieval;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import index.Index;
import index.Posting;
import index.PostingList;

public class BM25 {
	private double k1,k2,b,avgDocLen;
	private int numDocs;
	private Index index;
	
	public BM25(Index ind, double k1, double k2, double b) {
		this.k1 = k1;
		this.k2 = k2;
		this.b = b;
		index = ind;
		this.avgDocLen = index.getAverageDocLength();
		this.numDocs = index.getDocCount();
	}

	// now add Doc at a time retrieval to get occurrences to score...
	   /**
     * @return a list of the top k documents in descending order with respect to scores.
     * key = sceneId, value = score
     * Does document at a time retrieval using the BM25 model
     */
    public List<Map.Entry<Integer, Double>> retrieveQuery(String query, int k) {
		PriorityQueue<Map.Entry<Integer, Double>> result = 
				new PriorityQueue<>(Map.Entry.<Integer, Double>comparingByValue());
		String [] queryTerms = query.split("\\s+");
		Map <String, Integer> qTerms = new HashMap<String, Integer> ();
		List<String> terms = new ArrayList<String>(); 
		// convert terms to set with frequency
		for (String qterm: queryTerms) {
			if (qTerms.containsKey(qterm)) {
				int value = qTerms.get(qterm) + 1;
				qTerms.replace(qterm, value);
				// increment counter
			} else {
				// insert
				qTerms.put(qterm, 1);
				terms.add(qterm);
			}
		}
		PostingList[] lists = new PostingList[terms.size()];
		for (int i = 0; i < terms.size(); i++) {
			lists[i] = index.getPostings(terms.get(i));
		}
		for (int doc = 1; doc <= numDocs; doc++) {
			Double curScore = 0.0;
			boolean scored = false;
			for (int i = 0; i < lists.length; i++) {
				PostingList p = lists[i];
				String term = terms.get(i);
				p.skipTo(doc);
				Posting post = p.getCurrentPosting();
				if (post!= null && post.getDocId() == doc) {
					// This is where our score function gets used later
					int tf = post.getTermFreq();
					int dtf = index.getDocFreq(term);
					int dlen = index.getDocLength(doc);
					int qtf = qTerms.get(term);
					curScore += scoreOccurrence(tf, dtf, dlen, qtf);
					scored = true;
				}
			}
			if (scored)  {
				result.add(new AbstractMap.SimpleEntry<Integer, Double>(doc, curScore));
			}
			// trim the queue if necessary
			if (result.size() > k) {
				result.poll();
			}
		}
		// reverse the queue
		ArrayList<Map.Entry<Integer, Double>> scores = new ArrayList<Map.Entry<Integer, Double>>();
		scores.addAll(result);
		scores.sort(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder()));
		return scores;
    }
    
	public double scoreOccurrence(int tf, int tdf, int docLen, int qtf) {
		double K = k1 * ((1-b) + b*(docLen/avgDocLen));
		double idfPart = Math.log((numDocs -tdf + 0.5)/(tdf + 0.5)); // log of this one
		double docPart = ((k1 + 1) * tf)/(K + tf);
		double qPart = ((k2 + 1) * qtf)/(k2 + qtf);
		return idfPart * docPart * qPart;
	}	
	
}	
