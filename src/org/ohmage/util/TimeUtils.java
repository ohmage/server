/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This class contains utility functions for date and time objects.
 * 
 * @author John Jenkins
 */
public final class TimeUtils {
	private static final String FORMAT_ISO_8601_DATE = "yyyy-MM-dd";
	private static final String FORMAT_ISO_8601_DATETIME = "yyyy-MM-dd HH:mm:ss";
	
	private static final DateTimeFormatter DATE_FORMATTER = 
			DateTimeFormat.forPattern(FORMAT_ISO_8601_DATE);
	private static final DateTimeFormatter DATE_TIME_FORMATTER = 
			DateTimeFormat.forPattern(FORMAT_ISO_8601_DATETIME);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private TimeUtils() {}
	
	/**
	 * Converts a Date object into a Calendar object.
	 * 
	 * @param date The Date object to be converted into a Calendar object.
	 * 
	 * @return A Calendar object that represents the same date and time as the
	 * 		   Date object.
	 */
	public static Calendar convertDateToCalendar(Date date) {
		if(date == null) {
			return null;
		}
		
		Calendar result = Calendar.getInstance();
		result.setTime(date);
		return result;
	}
	
	/**
	 * Returns an ISO 8601 date formatted string from the parameterized 'date'.
	 * 
	 * @param date The date.
	 * 
	 * @return A string representing the date with the following format
	 * 		   {@value #FORMAT_ISO_8601_DATE} or null if the date is null.
	 * 
	 * @see #getIso8601DateTimeString(Date)
	 *
	public static String getIso8601DateString(Date date) {
		if(date == null) {
			return null;
		}

		return new SimpleDateFormat(FORMAT_ISO_8601_DATE).format(date);
	}
	
	/**
	 * Returns an ISO 8601 date and time formatted string from the 
	 * parameterized 'date'.
	 * 
	 * @param date The date
	 * 
	 * @return A string representing the date with the following format
	 * 		   {@value #FORMAT_ISO_8601_DATETIME} or null if the date is
	 * 		   null.
	 * 
	 * @see #getIso8601DateString(Date)
	 *
	public static String getIso8601DateTimeString(Date date) {
		if(date == null) {
			return null;
		}
		
		return new SimpleDateFormat(FORMAT_ISO_8601_DATETIME).format(date);
	}
	
	/**
	 * Converts a date to an ISO8601 string.
	 * 
	 * @param date The date, time, and time zone to be converted to a string.
	 * 
	 * @param withTime Whether or not to include the time component as well as
	 * 				   the date component.
	 * 
	 * @return A string representing the date and time in the time zone.
	 */
	public static String getIso8601DateString(
			final DateTime date, 
			final boolean withTime) {
		
		if(withTime) {
			return DATE_TIME_FORMATTER.print(date);
		}
		else {
			return DATE_FORMATTER.print(date);
		}
	}
	
	/**
	 * Converts a date string into a DateTime object. The date string may be
	 * a date and time or just a date.
	 * 
	 * @param date The string to be parsed.
	 * 
	 * @return The DateTime representing that string.
	 * 
	 * @throws IllegalArgumentException The string could not be parsed.
	 */
	public static DateTime getDateTimeFromString(
			final String date) {
		
		try {
			return DATE_TIME_FORMATTER.parseDateTime(date);
		}
		catch(IllegalArgumentException e) {
			// If it's not a date-time, maybe it is just a date.
			return DATE_FORMATTER.parseDateTime(date);
		}
	}
}