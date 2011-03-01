package edu.ucla.cens.awserver.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


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
	 * @return true if the String is the value "true" or "false"
	 *         false otherwise -- this method is more restrictive than Boolean.valueOf(String s)
	 */
	public static boolean isBooleanString(String string) {
		
		return "true".equals(string) || "false".equals(string);
		
	}
	
	/**
	 * @return the URL decoded version of the provided string.
	 */
	public static String urlDecode(String string) {
		if(null == string) {
			return null;
		}
		
		try {
			
			return URLDecoder.decode(string, "UTF-8");
			
		} catch (UnsupportedEncodingException uee) { // bad!! is the Java install corrupted?
			
			throw new IllegalStateException("cannot decode UTF-8 string: " + string);
		}
	}
}
