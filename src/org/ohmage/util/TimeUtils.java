package org.ohmage.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class contains utility functions for date and time objects.
 * 
 * @author John Jenkins
 */
public final class TimeUtils {
	private static final String FORMAT_ISO_8601_DATE = "yyyy-MM-dd";
	private static final String FORMAT_ISO_8601_DATETIME = "yyyy-MM-dd HH:mm:ss";
	
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
	 */
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
	 */
	public static String getIso8601DateTimeString(Date date) {
		if(date == null) {
			return null;
		}
		
		return new SimpleDateFormat(FORMAT_ISO_8601_DATETIME).format(date);
	}
}