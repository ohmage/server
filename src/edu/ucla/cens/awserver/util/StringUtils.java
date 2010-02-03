package edu.ucla.cens.awserver.util;


/**
 * A collection of methods for manipulating or validating strings.
 * 
 * @author selsky
 */
public final class StringUtils {
	
	/**
	 * It is illegal and unncessary to instantiate this class as it is a collection of static methods.
	 */
	private StringUtils() {
		
	}
	
	/**
	 * Checks for a null or empty (zero-length or all whitespace) String.
	 * 
	 * A method with the same signature and behavior as this one exists in the MySQL JDBC code. That method is not used outside 
	 * of the data layer of this application in order to avoid unnecessary dependencies i.e., AW utility classes should not
	 * depend on a third-party data access lib.
	 * 
	 * @return true if the String is null, empty, or all whitespace
	 *         false otherwise
	 */
	public static boolean isEmptyOrWhitespaceOnly(String string) {
		
		return null == string || "".equals(string.trim()); 
		
	}
	
	/**
	 * Retrieves the subdomain from a URL String where the subdomain is defined as the text between the protocol (http://) and the
	 * first occurence of a dot (.) (so technically this method does not return the full subdomain). 
	 * 
	 * @throws IllegalArgumentException if a null or empty string is passed in
	 */
	public static String retrieveSubdomainFromUrlString(String url) {
		if(isEmptyOrWhitespaceOnly(url)) {
			throw new IllegalArgumentException("cannot retrieve subdomain from a null or empty URL String");
		}
		
		String urlStart = url.split("\\.")[0];
		String subdomain = null;
		
		if(urlStart.startsWith("http://")) {
			
			subdomain = urlStart.substring(7);

// enable https support when we enable it in the app server config			
//		} else if(urlStart.startsWith("https://")) {
//
//			subdomain = urlStart.substring(8);
			
		} else { // if this happens, the application server is configured to support an unknown protocol 
			     // and this method needs updating
			
			throw new IllegalArgumentException("unknown protocol: " + url);
		}
		
		return subdomain;
		
	}	
	
	/**
	 * Retrieves the "server name" from a URL String where the server name is defined as the text between the first dot following
	 * the protocol and the next dot e.g. for the URL http://pilot.andwellness-dev.cens.ucla.edu, the server name 
	 * that will be returned is andwellness-dev. 
	 * 
	 *  @throws IllegalArgumentException if a null or empty string is passed in
	 */
	public static String retrieveServerNameFromUrlString(String url) {
		if(isEmptyOrWhitespaceOnly(url)) {
			throw new IllegalArgumentException("cannot retrieve server name from a null or empty URL String");
		}
		
		return url.split("\\.")[1];
	}
	
	/**
	 * Converts an array of ints to a string of the form {n,n,n}. A null array will return the string null. 
	 * 
	 * TODO replace calls to this method with Arrays.toString()
	 */
	public static String intArrayToString(int[] array) {
		StringBuilder builder = new StringBuilder();
		if(null == array) {
			builder.append("null");
		} else {
			builder.append("{");
			for(int i = 0; i < array.length; i++) {
				builder.append(array[i]);
				if(i < array.length - 1) {
					builder.append(",");
				}
			}
			builder.append("}");
		}
		return builder.toString();
	}
}
