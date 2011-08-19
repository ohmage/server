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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * A collection of static methods for working with dates.
 * 
 * FIXME -- Javadoc
 * 
 * @author Joshua Selsky
 */
public final class DateUtils {
	private static final Logger LOGGER = Logger.getLogger(DateUtils.class);
	
	/**
	 * Private constructor as this class is a collection of static methods.
	 */
	private DateUtils() { }
	
	/**
	 * Returns the millisecond offset between the system timezone and the provided timezone.   
	 */
	public static long systemTimezoneOffset(String timezone) {
		if(! isValidTimezone(timezone)) {
			throw new IllegalArgumentException("a valid timezone is required");
		}
		
		TimeZone systemTimeZone = TimeZone.getDefault();
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("system tz: " + systemTimeZone.getID());
		}
		
		TimeZone dataTimeZone = TimeZone.getTimeZone(timezone);
		
		long now = System.currentTimeMillis();
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("returning " + (systemTimeZone.getOffset(now) - dataTimeZone.getOffset(now))  + " for tz " + timezone);
		}
		
		return systemTimeZone.getOffset(now) - dataTimeZone.getOffset(now);
	}
	
	/**
	 * 
	 * @param tz
	 * @return
	 */
	public static boolean isValidTimezone(String tz) {
		if(null != tz) {
			// TODO store a static sorted copy of this list?
			return Arrays.asList(TimeZone.getAvailableIDs()).contains(tz);
		}
		return false;
	}
	
	/**
	 * Converts the provided timestamp to a UTC value using the provided timezoneId. The timestamp must be in the format 
	 * yyyy-MM-dd HH:mm:ss. If the provided timezoneId is not found by the server JVM, GMT is assumed. 
	 * 
	 * @param timestamp  The timestamp to convert.
	 * @param timezoneId The id of the timezone the timestamp will be converted to.
	 * @return A String containing the converted timezone.
	 */
	public static String timestampStringToUtc(String timestamp, String timezoneId) {
		final String dateFormatString = "yyyy-MM-dd HH:mm:ss";
		
		DateFormat inputDateFormat = new SimpleDateFormat(dateFormatString);
		inputDateFormat.setLenient(false);
		// if timezoneId is not understood, GMT will be returned by TimeZone.getTimeZone(timezoneId), which is unfortunate
		inputDateFormat.setTimeZone(TimeZone.getTimeZone(timezoneId)); 
		Date inputDate = null;
		
		try {
			
			inputDate = inputDateFormat.parse(timestamp);
			
		} catch (ParseException pe) { // data that does not match the format
			
			LOGGER.warn("unparseable date");
			throw new IllegalStateException("could not parse timestamp " + timestamp + " using format " + dateFormatString, pe);
			
		}
		
		// ok, now switch timezones to UTC
		
		DateFormat outputDateFormat = new SimpleDateFormat(dateFormatString);
		outputDateFormat.setLenient(false);
		outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return outputDateFormat.format(inputDate);
	}
}
