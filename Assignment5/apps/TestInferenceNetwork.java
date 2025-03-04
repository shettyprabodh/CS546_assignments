package apps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import index.Index;
import index.InvertedIndex;
import retrieval.AndNode;
import retrieval.Dirichlet;
import retrieval.InferenceNetwork;
import retrieval.OrderedWindow;
import retrieval.ProximityNode;
import retrieval.QueryNode;
import retrieval.RetrievalModel;
import retrieval.OrNode;
import retrieval.TermNode;
import retrieval.UnorderedWindow;
import retrieval.PriorNode;

/*
 * *
Q1: the king queen royalty
Q2: servant guard soldier
Q3: hope dream sleep
Q4: ghost spirit
Q5: fool jester player
Q6: to be or not to be
Q7: alas
Q8: alas poor
Q9: alas poor yorick
Q10: antony strumpet

Please run these queries using the two phrase operators, ordered window and unordered window.
For ordered, use a distance of 1 (exact phrase), for unordered, use a window width
3*|Q| (three times the length of the query). Please run these queries with each of the
operators: SUM, AND, OR, and MAX. Use dirichlet smoothing with μ=1500 for all runs.
 */
public class TestInferenceNetwork {

	public static void main(String[] args) {
		int k = Integer.parseInt(args[0]);
		boolean compressed = Boolean.parseBoolean(args[1]);
		Index index = new InvertedIndex();
		index.load(compressed);

		RetrievalModel model = new Dirichlet(index, 1500);
		List<Map.Entry<Integer, Double>> results;
		InferenceNetwork network = new InferenceNetwork();
		QueryNode queryNode;
		ArrayList<QueryNode> children;
		PriorNode prior;

		// read in the queries
		String queryFile = args[2];
		List<String> queries = new ArrayList<String>();
		try {
			String query;

			BufferedReader reader = new BufferedReader(new FileReader(queryFile));
			while (( query = reader.readLine()) != null) {
				queries.add(query);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String outfile, runId, qId;
		int qNum = 0;

		// Uniform
		outfile = "uniform.trecrun";
		runId = "pshetty-and-dir-1500";
		qNum = 0;
		for (String query : queries) {
			qNum++;
			// make each of the required query nodes and run the queries
			children = genTermNodes(query, index, model);
			System.out.println("Outside:- " + index);
			prior = new PriorNode(index, "uniform");
			queryNode = new AndNode(children, prior);
			results = network.runQuery(queryNode, k);
			qId = "Q" + qNum;
			boolean append = qNum > 1;
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(outfile, append));
				printResults(results, index, writer, runId, qId);
				writer.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		outfile = "random.trecrun";
		runId = "pshetty-and-dir-1500";
		qNum = 0;
		for (String query : queries) {
			qNum++;
			// make each of the required query nodes and run the queries
			children = genTermNodes(query, index, model);
			prior = new PriorNode(index, "random");
			queryNode = new AndNode(children, prior);
			results = network.runQuery(queryNode, k);
			qId = "Q" + qNum;
			boolean append = qNum > 1;
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(outfile, append));
				printResults(results, index, writer, runId, qId);
				writer.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void printResults(List<Entry<Integer, Double>> results,
			Index index, PrintWriter writer, String runId, String qId) {
		int rank = 1;
		for (Map.Entry<Integer, Double> entry : results) {
			String sceneId = index.getDocName(entry.getKey());
			String resultLine = qId + " skip " + sceneId + " " + rank + " "
					+ entry.getValue() + " " + runId;

			writer.println(resultLine);
			rank++;
		}
	}
	private static ArrayList<QueryNode> genTermNodes(String query, Index index, RetrievalModel model) {
		String [] terms = query.split("\\s+");
		ArrayList<QueryNode> children = new ArrayList<QueryNode>();
		for (String term : terms) {
			ProximityNode node = new TermNode(term, index, model);
			children.add(node);
		}
		return children;
	}
}
