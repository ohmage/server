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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A collection of methods for manipulating or validating Strings.
 * 
 * @author selsky
 */
public final class StringUtils {
	private static final int NUM_URN_SEGMENTS = 3;
	private static final Pattern URN_PATTERN = Pattern.compile("[a-z0-9_]+");
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^([_A-Za-z0-9-]+)(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	
	/**
	 * It is unncessary to instantiate this class as it is a collection of 
	 * static methods.
	 */
	private StringUtils() {}
	
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
	 * 
	 * @deprecated We are moving away from dynamically generated SQL.
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
	 * 
	 * @deprecated Use the respective X.parseX() functions to get exactly what
	 * 			   you want, and, if it fails, add the exception to the one you
	 * 			   will generate and throw.
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
	 * Validates that a String value is a valid URN value.
	 * 
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
		
		String[] a = s.split(":");
		
		if(a.length < NUM_URN_SEGMENTS) { // require at least three colon-delimited sections
			return false;
		}
		
		for(int i = 1; i < a.length; i++) { // each section after the initial urn must match URN_PATTERN
			if(! URN_PATTERN.matcher(a[i]).matches()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Validates that an email address is a valid email address.
	 * 
	 * @param emailAddress The email address to be validated.
	 * 
	 * @return Returns false if the email address is null, whitespace only, or
	 * 		   not a valid email address; otherwise, true is returned.
	 */
	public static boolean isValidEmailAddress(String emailAddress) {
		if(isEmptyOrWhitespaceOnly(emailAddress)) {
			return false;
		}
		
		return EMAIL_PATTERN.matcher(emailAddress).matches();
	}
	
	/**
	 * Decodes a String value into its boolean representation. If it is not a 
	 * valid boolean value, null is returned. This should be used anywhere 
	 * boolean values are being decoded from Strings.
	 * 
	 * @param value The String value to be decoded.
	 * 
	 * @return Returns true if the value is a valid boolean value and 
	 * 		   represents true. Returns false if the value is a valid boolean
	 * 		   value and represents false. Returns null if the value is not a
	 * 		   valid boolean value.
	 */
	public static Boolean decodeBoolean(String value) {
		// If we are going to allow different values for true and false, this
		// is where they should be included.
		if("true".equals(value)) {
			return true;
		}
		else if("false".equals(value)) {
			return false;
		}
		else {
			return null;
		}
	}
	
	/**
	 * @return an empty list, or if the provided string contained comma-separated values, a list containing each element
	 * 
	 * @deprecated Perform this operation in specific Validators. Be sure to
	 * 			   check for empty Strings which will be returned as a List of
	 * 			   size 1 where the only element is also an empty String.
	 */
	public static List<String> splitCommaSeparatedString(String string) {
		if(null == string) {
			return Collections.emptyList();
		} 
		return Arrays.asList(string.split(","));
	}
}