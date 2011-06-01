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
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * A collection of static methods for working with dates.
 * 
 * @author selsky
 */
public class DateUtils {
	private static Logger _logger = Logger.getLogger(DateUtils.class);
	
	/**
	 * Private constructor as this class is a collection of static methods.
	 */
	private DateUtils() { }
	
//	/**
//     * Assumes dates are of the form yyyy-MM-dd hh:mm:ss.
//     * 
//     * @throws IllegalArgumentException if the timezone is unknown
//     * @throws IllegalArgumentException if the date is null or unparseable
//	 */
//	public static String convertDateToUtc(String date, String timezone) {
//		if(null == date) {
//			throw new IllegalArgumentException("a non-null date is required");
//		}
//		if(! isValidTimezone(timezone)) {
//			throw new IllegalArgumentException("a valid timezone is required");
//		}
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		sdf.setTimeZone(TimeZone.getTimeZone(timezone)); // create the date using the original timezone
//		
//		Date parsedDate = null;
//		
//		try {
//			
//			parsedDate = sdf.parse(date);
//			
//		} catch (ParseException pe) {
//			
//			throw new IllegalArgumentException("unparseable date " + date);
//			
//		}
//		
//		sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // this converts the date to UTC, updating the value in the process
//		return sdf.format(parsedDate);
//		 
//	}
//	
//	/**
//	 *  Converts the long value in the provided timezone to a value in UTC.
//	 */
//	public static long convertTimeToUtc(Long time, String timezone) {
//		if(! isValidTimezone(timezone)) {
//			throw new IllegalArgumentException("a valid timezone is required");
//		}
//
//		Calendar providedTime = Calendar.getInstance(TimeZone.getTimeZone(timezone));
//		providedTime.setTimeInMillis(time);
//		
////		int hour = providedTime.get(Calendar.HOUR);
////		int minute = providedTime.get(Calendar.MINUTE);
////		int second = providedTime.get(Calendar.SECOND);
////		
////		System.out.printf("Provided time  : %02d:%02d:%02d %s \n", hour, minute, second, providedTime.get(Calendar.AM_PM));
//		
//		Calendar utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//		utcTime.setTimeInMillis(providedTime.getTimeInMillis());
//		
////		hour = utcTime.get(Calendar.HOUR);
////		minute = utcTime.get(Calendar.MINUTE);
////		second = utcTime.get(Calendar.SECOND);
////		
////		System.out.printf("UTC time  : %02d:%02d:%02d %s \n", hour, minute, second, utcTime.get(Calendar.AM_PM));
//		
//		return utcTime.getTimeInMillis();
//	}
	
	/**
	 * Returns the millisecond offset between the system timezone and the provided timezone.   
	 */
	public static long systemTimezoneOffset(String timezone) {
		if(! isValidTimezone(timezone)) {
			throw new IllegalArgumentException("a valid timezone is required");
		}
		
		TimeZone systemTimeZone = TimeZone.getDefault();
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("system tz: " + systemTimeZone.getID());
		}
		
		TimeZone dataTimeZone = TimeZone.getTimeZone(timezone);
		
		long now = System.currentTimeMillis();
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("returning " + (systemTimeZone.getOffset(now) - dataTimeZone.getOffset(now))  + " for tz " + timezone);
		}
		
		return systemTimeZone.getOffset(now) - dataTimeZone.getOffset(now);
	}
	
	public static boolean isValidTimezone(String tz) {
		if(null != tz) {
			String[] ids = TimeZone.getAvailableIDs();
			for(String id : ids) {
				if(id.equals(tz)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Converts the provided timestamp to a UTC value using the provided timezoneId. The timestamp must be in the format 
	 * yyyy-MM-dd HH:mm:ss. If the provided timezoneId is not found by the server JVM, GMT is assumed. 
	 */
	public static String timestampStringToUtc(String timestamp, String timezoneId) {
		final String dateFormatString = "yyyy-MM-dd HH:mm:ss";
		
		DateFormat inputDateFormat = new SimpleDateFormat(dateFormatString);
		inputDateFormat.setLenient(false);
		// if timezoneId is not understood, GMT will be returned by TimeZone.getTimeZone(timezoneId)
		inputDateFormat.setTimeZone(TimeZone.getTimeZone(timezoneId)); 
		Date inputDate = null;
		
		try {
			
			inputDate = inputDateFormat.parse(timestamp);
			
		} catch (ParseException pe) { // data that does not match the format
			
			_logger.warn("unparseable date");
			throw new IllegalStateException("could not parse timestamp " + timestamp + " using format " + dateFormatString, pe);
			
		}
		
		// ok, now switch timezones to UTC
		
		DateFormat outputDateFormat = new SimpleDateFormat(dateFormatString);
		outputDateFormat.setLenient(false);
		outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return outputDateFormat.format(inputDate);
	}
}
