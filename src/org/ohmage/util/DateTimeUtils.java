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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class contains utility functions for date and time objects.
 * 
 * @author John Jenkins
 */
public final class DateTimeUtils {
	/**
	 * The date-time formatter for the W3C's specifications for date-only
	 * values.
	 */
	private static final DateTimeFormatter ISO_W3C_DATE_FORMATTER;
	static {
		// The W3C define 6 formats as valid ISO date/time values.
		DateTimeParser[] parsers = new DateTimeParser[3];
		
		// Just the year.
		parsers[0] = ISODateTimeFormat.year().getParser();
		
		// The year and month.
		parsers[1] = ISODateTimeFormat.yearMonth().getParser();
		
		// The year, month, and day.
		parsers[2] = 
			ISODateTimeFormat.yearMonthDay().getParser();
		
		// Build the formatter with the 6 parsers and the complete ISO 
		// date-time printer.
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
		builder.append(ISODateTimeFormat.dateTime().getPrinter(), parsers);
		ISO_W3C_DATE_FORMATTER = builder.toFormatter().withZoneUTC();
	}
	
	/**
	 * The date-time formatter for the W3C's specifications for date-time 
	 * values.
	 */
	private static final DateTimeFormatter ISO_W3C_DATE_TIME_FORMATTER;
	static {
		// The W3C define 6 formats as valid ISO date/time values.
		DateTimeParser[] parsers = new DateTimeParser[3];
		
		// The year, month, day, hour, minute, and time zone.
		// Build the parser from the existing ISODateTimeParser for the year,
		// month, day, hour, and minute and add the time zone.
		DateTimeFormatterBuilder dateHourMinuteTimezone =
			new DateTimeFormatterBuilder();
		dateHourMinuteTimezone.append(ISODateTimeFormat.dateHourMinute());
		dateHourMinuteTimezone.append(DateTimeFormat.forPattern("ZZ"));
		parsers[0] = dateHourMinuteTimezone.toFormatter().getParser();
		
		// The year, month, day, hour, minute, second, and time zone.
		parsers[1] = ISODateTimeFormat.dateTimeNoMillis().getParser();
		
		// The year, month, day, hour, minute, and time zone.
		// Build the parser from the existing ISODateTimeParser for the year,
		// month, day, hour, and minute and add the time zone.
		parsers[2] = ISODateTimeFormat.dateTime().getParser();
		
		// Build the formatter with the 6 parsers and the complete ISO 
		// date-time printer.
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
		builder.append(ISODateTimeFormat.dateTime().getPrinter(), parsers);
		ISO_W3C_DATE_TIME_FORMATTER = builder.toFormatter();
	}
	
	private static final String FORMAT_ISO_8601_DATE = "yyyy-MM-dd";
	private static final String FORMAT_ISO_8601_DATETIME = "yyyy-MM-dd HH:mm:ss";
	
	private static final DateTimeFormatter DATE_FORMATTER = 
			DateTimeFormat.forPattern(FORMAT_ISO_8601_DATE);
	private static final DateTimeFormatter DATE_TIME_FORMATTER = 
			DateTimeFormat.forPattern(FORMAT_ISO_8601_DATETIME);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private DateTimeUtils() {}
	
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
	 * Converts a date to an W3C-ISO8601 string.
	 * 
	 * @param date The date, time, and time zone to be converted to a string.
	 * 
	 * @param withTime Whether or not to include the time component as well as
	 * 				   the date component.
	 * 
	 * @return A string representing the date and time in the time zone.
	 */
	public static String getW3cIso8601DateString(
			final DateTime date, 
			final boolean withTime) {
		
		if(withTime) {
			return ISO_W3C_DATE_TIME_FORMATTER.print(date);
		}
		else {
			return ISO_W3C_DATE_FORMATTER.print(date);
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
			try {
				return DATE_FORMATTER.parseDateTime(date);
			}
			catch(IllegalArgumentException notBadFormat) {
				return parseIsoW3CDateTime(date); 
			}
		}
	}
	
	/**
	 * Sanitizes and decodes a time zone string.
	 * 
	 * @param timeZone The time zone to be decoded.
	 * 
	 * @return The DateTimeZone that represents this time zone.
	 * 
	 * @throws IllegalArgumentException The date time zone is invalid.
	 */
	public static DateTimeZone getDateTimeZoneFromString(
			final String timeZone) {
		
		if(timeZone == null) {
			return null;
		}
		
		try {
			return DateTimeZone.forID(timeZone);
		}
		catch(IllegalArgumentException e) {
			// This is acceptable if it starts with "GMT" or "UTC".
			if(timeZone.startsWith("GMT") || timeZone.startsWith("UTC")) {
				return DateTimeZone.forID(timeZone.substring(3));
			}
			else {
				throw e;
			}
		}
	}
	
	/**
	 * Parses a string into a DateTime object that represents the time as long
	 * as it conforms to one of the W3C's specifications of the ISO8601
	 * standard.
	 * 
	 * @param value The string value to be parsed.
	 * 
	 * @return A DateTime object that represents the parsed value.
	 * 
	 * @throws IllegalArgumentException The value does not conform to the W3C
	 * 									specification of the ISO8601 standard.
	 */
	public static DateTime parseIsoW3CDateTime(String value) {
		try {
			return
				ISO_W3C_DATE_FORMATTER.withOffsetParsed().parseDateTime(value);
		}
		catch(IllegalArgumentException e) {
			return
				ISO_W3C_DATE_TIME_FORMATTER
					.withOffsetParsed().parseDateTime(value);
		}
	}
	
	/**
	 * Validates that some timezone string is a valid timezone string.
	 * 
	 * @param tz The timezone string.
	 * 
	 * @return True if the timezone string is a valid timezone; false, 
	 * 		   otherwise.
	 */
	public static boolean isValidTimezone(final String tz) {
		if(null != tz) {
			// TODO store a static sorted copy of this list?
			return Arrays.asList(TimeZone.getAvailableIDs()).contains(tz);
		}
		return false;
	}
}