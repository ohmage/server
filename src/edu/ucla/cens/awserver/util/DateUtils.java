package edu.ucla.cens.awserver.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A collection of static methods for working with dates.
 * 
 * @author selsky
 */
public class DateUtils {
//	private static Logger _logger = Logger.getLogger(DateUtils.class);
	
	/**
	 * Instatiation disallowed and unnecessary.
	 */
	private DateUtils() { }
	
	/**
     * Assumes dates are of the form yyyy-MM-dd hh:mm:ss.
     * 
     * @throws IllegalArgumentException if the timezone is unknown
     * @throws IllegalArgumentException if the date is null 
	 */
	public static String convertDateToUtc(Date date, String timezone) {
		if(null == date) {
			throw new IllegalArgumentException("a non-null date is required");
		}
		if(! isValidTimezone(timezone)) {
			throw new IllegalArgumentException("a valid timezone is required");
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(timezone)); // create the date using the original timezone
		sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // this converts the date to UTC, updating the value in the process
		
		return sdf.format(date);
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
}
