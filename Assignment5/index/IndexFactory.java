package index;

/**
 * Creates various Index objects
 */
public class IndexFactory {
	
	/**
	 * Allows for access to an opened index without passing it around as an attribute.
	 * Suboptimal use of singleton pattern. Need to add index type enum here...
	 * @return a singleton Index instance
	 */
	static Index openIndex = null;
	public static Index getIndex() {
		if (openIndex == null) {
			openIndex = new InvertedIndex();
		}
		return openIndex;
	}
}
