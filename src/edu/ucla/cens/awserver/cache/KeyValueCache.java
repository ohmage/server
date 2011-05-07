package edu.ucla.cens.awserver.cache;

/**
 * A key value relat
 * @author jojenki
 *
 */
public class KeyValueCache extends Cache {
	
	/**
	 * Inner class for handling the results of a query for the Strings and
	 * and their respective String values.
	 *  
	 * @author John Jenkins
	 */
	protected static class KeyAndValue {
		public String _key;
		public String _value;
		
		public KeyAndValue(String key, String value) {
			_key = key;
			_value = value;
		}
	}
}
