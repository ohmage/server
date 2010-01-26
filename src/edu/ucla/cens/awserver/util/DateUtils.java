package edu.ucla.cens.awserver.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
     * @throws IllegalArgumentException if the date is null or unparseable
	 */
	public static String convertDateToUtc(String date, String timezone) {
		if(null == date) {
			throw new IllegalArgumentException("a non-null date is required");
		}
		if(! isValidTimezone(timezone)) {
			throw new IllegalArgumentException("a valid timezone is required");
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(timezone)); // create the date using the original timezone
		
		Date parsedDate = null;
		
		try {
			
			parsedDate = sdf.parse(date);
			
		} catch (ParseException pe) {
			
			throw new IllegalArgumentException("unparseable date " + date);
			
		}
		
		sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // this converts the date to UTC, updating the value in the process
		return sdf.format(parsedDate);
		 
	}
	
	/**
	 *  Converts the long value in the provided timezone to a value in UTC.
	 */
	public static long convertTimeToUtc(Long time, String timezone) {
		if(! isValidTimezone(timezone)) {
			throw new IllegalArgumentException("a valid timezone is required");
		}

		Calendar providedTime = Calendar.getInstance(TimeZone.getTimeZone(timezone));
		providedTime.setTimeInMillis(time);
		
//		int hour = providedTime.get(Calendar.HOUR);
//		int minute = providedTime.get(Calendar.MINUTE);
//		int second = providedTime.get(Calendar.SECOND);
//		
//		System.out.printf("Provided time  : %02d:%02d:%02d %s \n", hour, minute, second, providedTime.get(Calendar.AM_PM));
		
		Calendar utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		utcTime.setTimeInMillis(providedTime.getTimeInMillis());
		
//		hour = utcTime.get(Calendar.HOUR);
//		minute = utcTime.get(Calendar.MINUTE);
//		second = utcTime.get(Calendar.SECOND);
//		
//		System.out.printf("UTC time  : %02d:%02d:%02d %s \n", hour, minute, second, utcTime.get(Calendar.AM_PM));
		
		return utcTime.getTimeInMillis();
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
