package retrieval;

import java.util.ArrayList;

import index.Index;

public class BandNode extends UnorderedWindow {

	public BandNode(ArrayList<QueryNode> termNodes, 
			Index ind, RetrievalModel mod) {
		super(0, termNodes, ind, mod);
	}
	
	@Override
	public Integer getWindowSize(Integer d){
		return index.getDocLength(d);
	}
}
