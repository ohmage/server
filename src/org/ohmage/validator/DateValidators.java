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
import java.util.Date;

import org.apache.log4j.Logger;
import org.ohmage.exception.ValidationException;


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
	 *  @return Returns a Date representation of the provided String if the
	 *  provided String is parseable and non-null.
	 */
	public static Date validateISO8601Date(String date) throws ValidationException {
		return validate(date, ISO8601_DATE_FORMAT);
	}
	
	/**
	 * The ohmage APIs require date-times to be in ISO-8601 format. In
	 * layman's terms, this means that dates have increasing precision as
	 * read from left to right. In Java <tt>DateFormat</tt> parlance, this means 
	 * that the allowed dates must be of the form <tt>yyyy-MM-dd HH:mm:ss</tt>.
	 * 
	 *  @param date The date to check for ISO8601 conformance
	 *  @return Returns a Date representation of the provided String if the
	 *  provided String is parseable and non-null.
	 */
	public static Date validateISO8601DateTime(String dateTime) throws ValidationException {
		return validate(dateTime, ISO8601_DATETIME_FORMAT);
	}
	
	/**
	 * @param date The date string to validate
	 * @param format A string representing a valid SimpleDateFormat. 
	 * @return Returns a Date representation of the provided String if the
	 * provided String is parseable and non-null.
	 */
	private static Date validate(String date, String format) throws ValidationException {
		if(date == null) {
			return null;
		}
		
		try {
			// SimpleDateFormat is not thread-safe so is has to be created on 
			// every invocation of this method
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			formatter.setLenient(false);
			return formatter.parse(date);
			
		} catch (ParseException pe) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Invalid datetime: " + date + ". It does not match: " + format);
			}
			
			throw new ValidationException("Invalid date: " + date, pe);
		}
	}
}
