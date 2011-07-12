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
package org.ohmage.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.ohmage.util.StringUtils;


/**
 * A Singleton collection of static methods for validating dates.
 * 
 * @author Joshua Selsky
 */
public final class DateValidators {
	private static final Logger LOGGER = Logger.getLogger(DateValidators.class);
	private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd"; 
	private static final String ISO8601_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Default constructor. Private to enforce non-instantiability. 
	 */
	private DateValidators() {}
	
	/**
	 * Most of the ohmage APIs require dates to be in ISO-8601 format. In
	 * layman's terms, this means that dates have increasing precision as
	 * read from left to right. In Java <tt>DateFormat</tt> parlance, this means 
	 * that the allowed dates must be of the form yyyy-MM-dd.
	 * 
	 *  @param date The date to check for ISO8601 conformance
	 *  @param required A boolean indicating whether the date is a required 
	 *  part of an API call.
	 *  @return Returns true if the date is conformant, false otherwise. If 
	 *  required is false and the date is empty, returns true.
	 */
	public static boolean validateISO8601Date(String date, boolean required) {
		return validate(date, required, ISO8601_DATE_FORMAT);
	}
	
	/**
	 * The ohmage APIs require date-times to be in ISO-8601 format. In
	 * layman's terms, this means that dates have increasing precision as
	 * read from left to right. In Java <tt>DateFormat</tt> parlance, this means 
	 * that the allowed dates must be of the form <tt>yyyy-MM-dd HH:mm:ss</tt>.
	 * 
	 *  @param date The date to check for ISO8601 conformance
	 *  @param required A boolean indicating whether the date is a required 
	 *  part of an API call.
	 *  @return Returns true if the date is conformant, false otherwise. If 
	 *  required is false and the date is empty, returns true.
	 */
	public static boolean validateISO8601DateTime(String dateTime, boolean required) {
		return validate(dateTime, required, ISO8601_DATETIME_FORMAT);
	}
	
	/**
	 * @param date The date string to validate
	 * @param required Whether the date parameter is required by a particular
	 * API call.
	 * @param format A string representing a valid SimpleDateFormat. 
	 * @return Returns true if the date is conformant with the provided format,
	 * false otherwise. If required is false and the date is empty, returns true.  
	 */
	private static boolean validate(String date, boolean required, String format) {
		if(StringUtils.isEmptyOrWhitespaceOnly(date)) {
			return (! required);
		}
		
		try {
			// SimpleDateFormat is not thread-safe so is has to be created on 
			// every invocation of this method
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			formatter.setLenient(false);
			formatter.parse(date);
			
		} catch (ParseException pe) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Invalid datetime: " + date + ". It does not match: " + format);
			}
			
			return false;
		}
		
		return true;
	}
}
