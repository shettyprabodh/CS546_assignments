package index;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import utilities.Compression;
import utilities.CompressionFactory;
import utilities.Compressors;


class LookUp {
	public LookUp(long offset2, long numBytes2, int dtf2, int ctf2) {
		this.offset = offset2;
		this.numBytes = numBytes2;
		this.dtf = dtf2;
		this.ctf = ctf2;
	}
	long offset;
	long numBytes;
	int dtf;
	int ctf;
};

public class InvertedIndex implements Index {
    private Map<Integer, String> sceneIdMap = new HashMap<Integer, String>();
    private Map<Integer, String> playIdMap = new HashMap<Integer, String>();
    private Map<Integer, Integer> docLengths = new HashMap<Integer, Integer>();
	private Compressors compression;
	private Map<String, LookUp> lookup = new HashMap<String, LookUp>(); // key = term

    private long collectionSize;
    private double aveDocLen;
    private int numOfDoc;
    private String termInvListFile;

    public InvertedIndex () {
     }

    public void load(boolean compress) {
    	this.compression = compress ? Compressors.VBYTE : Compressors.EMPTY;
    	termInvListFile = compress ? "invListCompressed" : "invList";

        loadStringMap("sceneId.txt", sceneIdMap);
        loadStringMap("playIds.txt", playIdMap);
        loadDocLengths("docLength.txt");
        loadLookUp("lookup.txt");

    }
    private void loadStringMap(String fileName, Map<Integer, String> map) {
        String line;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] data = line.split("\\s+");
                map.put(Integer.parseInt(data[0]), data[1]);
            }
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDocLengths(String fileName) {
        String line;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            long totalLength = 0;

            while((line = bufferedReader.readLine()) != null) {
                String[] data = line.split("\\s+");
                int docLen = Integer.parseInt(data[1]);
                docLengths.put(Integer.parseInt(data[0]), docLen);
                totalLength += docLen;
            }
            collectionSize = totalLength;
            numOfDoc = docLengths.keySet().size();
            aveDocLen = 1.0 * collectionSize / numOfDoc;
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLookUp(String fileName) {
        String line;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // termOffset bytesWritten docTermFreq collectionTermFreq);
            while((line = bufferedReader.readLine()) != null) {
                String[] data = line.split("\\s+");
                String term = data[0];
                long offset = Long.parseLong(data[1]);
                long numBytes = Long.parseLong(data[2]);
                int dtf = Integer.parseInt(data[3]);
                int ctf = Integer.parseInt(data[4]);
                LookUp look = new LookUp(offset, numBytes, dtf, ctf);
                
                lookup.put(term, look);
            }
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public PostingList getPostings(String word) {
        PostingList invertedList = new PostingList();
        try {
            RandomAccessFile reader = new RandomAccessFile(termInvListFile, "rw");
            LookUp look = lookup.get(word);
            reader.seek(look.offset);
            int buffLength =(int)(look.numBytes);
            byte[] buffer = new byte[buffLength];
            int numRead = reader.read(buffer, 0, buffLength);
            assert numRead == look.numBytes;
            Compression comp = CompressionFactory.getCompressor(compression);
            IntBuffer intBuffer = IntBuffer.allocate(buffer.length);
            comp.decode(buffer, intBuffer);   
            int[] data = new int[intBuffer.position()];
            intBuffer.rewind();
            intBuffer.get(data);
            invertedList.fromIntegerArray(data);
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return invertedList;
    }

    public Set<String> getVocabulary() {
    	// could return this as a sorted set...
        return lookup.keySet();
    }

 
    /**
     * Get the document frequency of a word
     * @return number of documents containing the word
     * @param term the word
     */
    public int getDocFreq(String term) {
    	int retval = 0;
    	if (lookup.containsKey(term)) {
    		retval = lookup.get(term).dtf;
    	}
    	return retval;
    }


    /**
     * @return the occurrences of a term in all documents
     */
    public int getTermFreq(String term) {
    	int retval = 0;
    	if (lookup.containsKey(term)) {
    		retval = lookup.get(term).ctf;
    	}
    	return retval;
    }

     /**
     * Get the total number of documents
     */
    public int getDocCount() {
        return numOfDoc;
    }

    public int getDocLength(int docId) {
        return docLengths.get(docId);
    }

    public double getAverageDocLength() {
        return aveDocLen;
    }

    /**
     * @return the size of the collection. I.e. total number of words in all documents
     */
    public long getCollectionSize() {
        return collectionSize;
    }
 
    public String getPlay(int docId) {
        return playIdMap.get(docId);
    }
 
    public String getScene(int docId) {
        return sceneIdMap.get(docId);
    }

    public String getDocName(int docId) {
        return getScene(docId);
    }

    /**
     * @return a list of the top k documents in descending order with respect to scores.
     * key = sceneId, value = score
     * Does document at a time retrieval using raw counts for the model
     */
    public List<Map.Entry<Integer, Double>> retrieveQuery(String query, int k) {
		PriorityQueue<Map.Entry<Integer, Double>> result = 
				new PriorityQueue<>(Map.Entry.<Integer, Double>comparingByValue());
		String [] queryTerms = query.split("\\s+");
		PostingList[] lists = new PostingList[queryTerms.length];
		for (int i = 0; i < queryTerms.length; i++) {
			lists[i] = getPostings(queryTerms[i]);
		}
		for (int doc = 1; doc <= getDocCount(); doc++) {
			Double curScore = 0.0;
			for (PostingList p : lists) {
				p.skipTo(doc);
				Posting post = p.getCurrentPosting();
				if (post!= null && post.getDocId() == doc) {
					// This is where our score function gets used later
					curScore += post.getTermFreq();
				}
			}
			result.add(new AbstractMap.SimpleEntry<Integer, Double>(doc, curScore));
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
}
