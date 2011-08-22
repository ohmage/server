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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A collection of methods for manipulating or validating Strings.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public final class StringUtils {
	private static final int NUM_URN_SEGMENTS = 3;
	private static final Pattern URN_PATTERN = Pattern.compile("[a-z0-9_]+");

	private static final String FORMAT_AMERICAN_DATE = "MM/dd/yyyy";
	private static final String FORMAT_ISO_8601_DATE = "yyyy-MM-dd";
	
	private static final String FORMAT_AMERICAN_DATE_TIME = "M/d/yyyy h:m:s a";
	private static final String FORMAT_ISO_8601_DATE_TIME = "yyyy-M-d H:m:s";
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^([_A-Za-z0-9-]+)(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	
	private static final String DEFAULT_DELIMITER = ",";
	
	private static final int LATITUDE_LIMIT = 90;
	private static final int LONGITUDE_LIMIT = 180;
	
	/**
	 * It is unnecessary to instantiate this class as it is a collection of 
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
	 * Checks if a String's length is greater than or equal to some 'min' 
	 * value, and less than or equal to some 'max' value. The String should not
	 * be null, but if it is, false is returned.
	 * 
	 * @param string The String value whose length is being checked.
	 * 
	 * @param min The minimum allowed value for the String.
	 * 
	 * @param max The maximum allowed value for the String.
	 * 
	 * @return Returns false if the String is null, 'min' is greater than 
	 * 		   'max', or if the String's length is not within the bounds; 
	 * 		   otherwise, true is returned.
	 */
	public static boolean lengthWithinLimits(String string, int min, int max) {
		if(string == null) {
			return false;
		}
		
		if(min > max) {
			return false;
		}
		
		int stringLength = string.length();
		return((stringLength >= min) && (stringLength <= max));
	}
	
	/**
	 * Check is a String contains any profanity or not. Note: This is not yet
	 * implemented and will always return true!
	 *  
	 * @param string The String that is being checked for profanity.
	 * 
	 * @return Returns false if the String is null or contains no profanity;
	 * 		   otherwise, it returns false.
	 */
	public static boolean isProfane(String string) {
		if(string == null) {
			return false;
		}
		
		// TODO: Add a profanity filter.
		
		return false;
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
	 * Validates that a UUID String is a valid UUID.
	 * 
	 * @param uuid The UUID as a String to validate.
	 * 
	 * @return Returns true if the String is not null, not whitespace only, and
	 * 		   is a valid UUID.
	 */
	public static boolean isValidUuid(String uuid) {
		if(StringUtils.isEmptyOrWhitespaceOnly(uuid)) {
			return false;
		}
		
		try {
			UUID.fromString(uuid);
			
			return true;
		}
		catch(IllegalArgumentException e) {
			return false;
		}
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
	 * <p>Validates that some String, 'value', is a valid boolean value. The 
	 * String must be in all lower case and English.</p>
	 * <p>There is no special case for null. See {@link #decodeBoolean(String)}
	 * to determine what is a valid boolean value.</p>
	 * 
	 * @param value A String representation of a boolean value. This must be 
	 * 				in English and all lower case.
	 * 
	 * @return Returns true if the value is a valid boolean value; returns 
	 * 		   false if it is not a valid boolean value.
	 * 
	 * @see org.ohmage.util.StringUtils#decodeBoolean(String)
	 */
	public static boolean isValidBoolean(String value) {
		return (StringUtils.decodeBoolean(value) != null);
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
	 * Strips off the millseconds value from a JDBC timestamp String returned from the MySQL JDBC connector.
	 * 
	 * @param timestamp The timestamp to strip the nanos from.
	 * 
	 * @return A newly formatted String in the format 'yyyy-MM-dd hh:mm:ss' or null if the provided String is null.
	 */
	public static String stripMillisFromJdbcTimestampString(String timestamp) {
		if(null != timestamp) {
			if(timestamp.contains(".")) {
				return timestamp.substring(0, timestamp.lastIndexOf('.'));
			} 
			else {
				return timestamp;
			}
		}	
		return null;
	}
	
	/**
	 * Decodes a String representing a date and returns a resulting Date
	 * object. The date may follow any of these formats:
	 * <ul>
	 *   <li>{@value #FORMAT_AMERICAN_DATE}</li>
	 *   <li>{@value #FORMAT_ISO_8601_DATE}</li>
	 * </ul>
	 * 
	 * @param date The date as a String that is to be decoded.
	 * 
	 * @return Returns null if the date is null, whitespace only, or doesn't
	 * 		   follow one of the given format patterns. Otherwise, a new Date
	 * 		   object that represents the date is returned.
	 */
	public static Date decodeDate(String date) {
		if(isEmptyOrWhitespaceOnly(date)) {
			return null;
		}
		
		try {
			return new SimpleDateFormat(FORMAT_AMERICAN_DATE).parse(date);
		}
		catch(ParseException americanException) {
			try {
				return new SimpleDateFormat(FORMAT_ISO_8601_DATE).parse(date);
			}
			catch(ParseException iso8601Exception) {
				return null;
			}
		}
	}
	
	/**
	 * Decodes a String representing a date-time and returns a resulting Date
	 * object. The date my follow any of these formats:
	 * <ul>
	 *   <li>{@value #FORMAT_AMERICAN_DATE_TIME}</li>
	 *   <li>{@value #FORMAT_ISO_8601_DATE_TIME}</li>
	 * </ul>
	 * 
	 * @param dateTime The date-time as a String that is to be decoded.
	 * 
	 * @return Returns null if the date-time is null, whitespace only, or 
	 * 		   doesn't follow one of the given format patterns. Otherwise, a 
	 * 		   new Date object that represents the date is returned.
	 */
	public static Date decodeDateTime(String dateTime) {
		if(isEmptyOrWhitespaceOnly(dateTime)) {
			return null;
		}
		
		try {
			return new SimpleDateFormat(FORMAT_AMERICAN_DATE_TIME).parse(dateTime);
		}
		catch(ParseException americanException) {
			try {
				return new SimpleDateFormat(FORMAT_ISO_8601_DATE_TIME).parse(dateTime);
			}
			catch(ParseException iso8601Exception) {
				return null;
			}
		}
	}
	
	/**
	 * Decodes a String into a Double and then verifies that it is a plausible
	 * latitude value. If the String is null, whitespace only, or not a
	 * plausible latitude value, null is returned; otherwise a Double 
	 * representing the latitude value is returned.
	 * 
	 * @param latitude The String to be decoded.
	 * 
	 * @return Null if the String is null, whitespace only, not a number, or
	 * 		   not a valid latitude value.
	 */
	public static Double decodeLatitude(String latitude) {
		if(StringUtils.isEmptyOrWhitespaceOnly(latitude)) {
			return null;
		}
		
		try {
			Double latitudeDouble = Double.parseDouble(latitude);
			if((latitudeDouble < -LATITUDE_LIMIT) || (latitudeDouble > LATITUDE_LIMIT)) {
				return null;
			}
			else {
				return latitudeDouble;
			}
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Decodes a String into a Double and then verifies that it is a plausible
	 * longitude value. If the String is null, whitespace only, or not a
	 * plausible longitude value, null is returned; otherwise a Double 
	 * representing the longitude value is returned.
	 * 
	 * @param longitude The String to be decoded.
	 * 
	 * @return Null if the String is null, whitespace only, not a number, or
	 * 		   not a valid longitude value.
	 */
	public static Double decodeLongitude(String longitude) {
		if(StringUtils.isEmptyOrWhitespaceOnly(longitude)) {
			return null;
		}
		
		try {
			Double longitudeDouble = Double.parseDouble(longitude);
			if((longitudeDouble < -LONGITUDE_LIMIT) || (longitudeDouble > LONGITUDE_LIMIT)) {
				return null;
			}
			else {
				return longitudeDouble;
			}
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Splits the provided string using the provided delimiter. If no delimiter
	 * is provided, a comma is used.
	 * 
	 * @param string  The string to split
	 * @param delimiter The delimiter to use for the split operation.
	 * @return A List of Strings from the provided String or an empty string if
	 * the provided list is null or all whitespace. 
	 */
	public static List<String> splitString(String string, String delimiter) {
		if(isEmptyOrWhitespaceOnly(string)) {
			return Collections.emptyList();
		} 
		return Arrays.asList(string.split(isEmptyOrWhitespaceOnly(delimiter) ? DEFAULT_DELIMITER : delimiter));
	}
	
	/**
	 * Checks the incoming string to see if it contains only the delimiter or
	 * multiple delimiters in a row. If the delimiter is not provided, 
	 * a comma is used.
	 * 
	 * @param string  A string containing a delimited list.
	 * @param delimiter The string delimiter.
	 * @return  true if the string is malformed, false otherwise.
	 */
	public static boolean isMalformedDelimitedList(String string, String delimiter) {
		if(StringUtils.isEmptyOrWhitespaceOnly(string)) {
			return false;
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(delimiter)) {
			delimiter = DEFAULT_DELIMITER;
		}
		
		int strlen = string.length();
		int currentIndex = -1;
		for(int i = 0; i < strlen; i++) {
			if(currentIndex == -1) {
				currentIndex = string.indexOf(delimiter);
				if(currentIndex == -1) { // no delimiter found
					return false;
				}
			} else {
				int lastOccurrence = currentIndex;
				currentIndex = string.indexOf(delimiter, lastOccurrence + 1);
				
				if(currentIndex == -1) {
					return false;
				}
				else {
					if(currentIndex == lastOccurrence + 1) { // two delimiters in a row
						return true;
					}
				}
			}
		}
		
		return ! string.equals(delimiter);
	}
	
	/**
	 * @return a parameter list of the form (?,...?) depending on the numberOfParameters  
	 */
	public static String generateStatementPList(int numberOfParameters) {
		if(numberOfParameters < 1) {
			throw new IllegalArgumentException("cannot generate a parameter list for less than one parameter");
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

}
