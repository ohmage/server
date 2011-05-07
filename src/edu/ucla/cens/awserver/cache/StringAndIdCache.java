package edu.ucla.cens.awserver.cache;

/**
 * A cache designed for String-ID relationships.
 * 
 * @author John Jenkins
 */
public abstract class StringAndIdCache extends Cache {
	
	/**
	 * Inner class for handling the results of a query for the Strings and
	 * their respective IDs.
	 *  
	 * @author John Jenkins
	 */
	protected static class StringAndId {
		public int _id;
		public String _string;
		
		public StringAndId(int id, String string) {
			_id = id;
			_string = string;
		}
	}
}