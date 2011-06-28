/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A collection of methods for manipulating or validating strings.
 * 
 * @author selsky
 */
public final class StringUtils {
	// private static Logger _logger = Logger.getLogger(StringUtils.class);
	private static Pattern _urnPattern = Pattern.compile("[a-z0-9_]+");
	
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
	
	/**
	 * @return a parameter list of the form (?,...?) depending on the numberOfParameters  
	 */
	public static String generateStatementPList(int numberOfParameters) {
		if(numberOfParameters < 1) {
			throw new IllegalArgumentException("cannot generate a parameter list for less than one parameters");
		}
		
		StringBuilder builder = new StringBuilder("(");
		for(int i = 0; i < numberOfParameters; i++) {
			builder.append("?");
			if(i < numberOfParameters - 1) {
				builder.append(",");
			}	
		}
		builder.append(")");
		return builder.toString();
	}
	
	/**
	 * @return an Integer or a Double if the provided String is parseable to either
	 */
	public static Object stringToNumber(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException a) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException b) {}  
		}
		return value;
	}
	
	/**
	 * @return true if the provided value is a valid URN, false otherwise
	 */
	public static boolean isValidUrn(String value) {
		if(isEmptyOrWhitespaceOnly(value)) {
			return false;
		}
		
		String s = value.toLowerCase();
		
		if(! s.startsWith("urn:")) {
			return false;
		}
		
		if(s.length() < 7) { // disallow anything shorter than urn:a:a 
			return false;
		}
		
		String[] a = s.split(":");
		
		if(a.length < 3) { // require at least three colon-delimited sections
			return false;
		}
		
		for(int i = 1; i < a.length; i++) { // each section after the initial urn must match _urnPattern
			if(! _urnPattern.matcher(a[i]).matches()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @return an empty list, or if the provided string contained comma-separated values, a list containing each element
	 */
	public static List<String> splitCommaSeparatedString(String string) {
		if(null == string) {
			return Collections.emptyList();
		} 
		return Arrays.asList(string.split(","));
	}
	
	/**
	 * Creates a single String representation of the Collection where each item
	 * is converted to a String via its toString() method and each item is
	 * separated by the 'delimiter'.
	 * 
	 * @param collection The Collection of items to be aggregated into a single
	 * 					 String.
	 * 
	 * @param delimiter The String to place between each item in the resulting
	 * 					String.
	 * 
	 * @return A String representation of the Collection.
	 */
	public static String collectionToDelimitedString(Collection<?> collection, String delimiter) {
		boolean firstPass = true;
		StringBuilder builder = new StringBuilder();
		for(Object item : collection) {
			if(firstPass) {
				firstPass = false;
			}
			else {
				builder.append(delimiter);
			}
			
			builder.append(item.toString());
		}
		
		return builder.toString();
	}
	
	/**
	 * Strips off the millseconds value from a JDBC timestamp String returned from the MySQL JDBC connector.
	 * 
	 * @param timestamp The timestamp to strip the nanos from.
	 * 
	 * @return A newly formatted String in the format 'yyyy-MM-dd hh:mm:ss' or null if the provided String is null.
	 */
	public static String stripMillisFromJdbcTimestampString(String timestamp) {
		if(null != timestamp) {
			if(timestamp.contains(".")) {
				return timestamp.substring(0, timestamp.lastIndexOf("."));
			} 
			else {
				return timestamp;
			}
		}	
		return null;
	}
	
}
