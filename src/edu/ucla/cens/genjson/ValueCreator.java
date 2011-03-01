package edu.ucla.cens.genjson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Utility class for creating various types of values for AW JSON messages. 
 * 
 * @author selsky
 */
public class ValueCreator {
	private static Random _random = new Random(); 
	private static String[] _tzs = {"EST", "CST", "MST", "PST"}; // American timezone hegemony ;)
	// Favor still for testing
	private static String[] _modes = {"still", "still", "still", "walk", "run", "bike", "drive"};
	private static String[] _providers = {"null", "GPS", "network"};
	private static char[] chars = "abcdefghijklmnopqrstuvwxyz 0123456789".toCharArray();
	private static String[] _locationStatuses = {"valid", "unavailable","inaccurate", "stale"};
	private static char[] _hexChars = "0123456789ABCDEF".toCharArray(); 
	
	private ValueCreator() { };
	
	public static double latitude() {
		return _random.nextInt() % 90 + _random.nextDouble();
	}
	
	public static double longitude() {
		return _random.nextInt() % 180 + _random.nextDouble();
	}
	
	public static long epoch() {
		return System.currentTimeMillis();
	}

	public static long epoch(int num_days_ago) {
		return System.currentTimeMillis() - 86400000l * num_days_ago; 
	}
	
	public static String tz() {
		return _tzs[Math.abs(_random.nextInt()) % (_tzs.length)];
	}
	
	public static String date() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
// use the local timezone for now
// if specific timezones are added in the future, the epoch method will have to change so the value returned has the same tz
//		sdf.setTimeZone(TimeZone.getTimeZone(tz));
		return sdf.format(new Date());
	}

	public static String date(int num_days_ago) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
// use the local timezone for now
// if specific timezones are added in the future, the epoch method will have to change so the value returned has the same tz
//		sdf.setTimeZone(TimeZone.getTimeZone(tz));
		return sdf.format(new Date(epoch(num_days_ago)));
	}
	
	public static String mode() {
		return _modes[Math.abs(_random.nextInt()) % (_modes.length)];
	}
	
	public static double randomPositiveDouble() {
		return Math.abs(_random.nextDouble());
	}
	
	public static double randomDouble() {
		return _random.nextDouble();
	}
	
	public static boolean randomBoolean() {
		return _random.nextBoolean(); 
	}
	
	public static String randomTime() {
		String hours =  String.valueOf(Math.abs(_random.nextInt() % 24));
		
		int m = Math.abs(_random.nextInt() % 60);
		String minutes = m < 10 ? "0" + m : String.valueOf(m);
		
		return hours + ":" + minutes; 
	}
	
	// Can pass an hour around which the returned times will be centered
	// The data will be distributed uniformly over the range
	public static String randomTime(int average_hour, int hour_range) {
	    int h = Math.abs((average_hour + _random.nextInt(hour_range+1) - hour_range / 2) % 24);
	    String hours = String.valueOf(h);
	    
	    int m = Math.abs(_random.nextInt() % 60);
        String minutes = m < 10 ? "0" + m : String.valueOf(m);
	    
	    return hours + ":" + minutes;
	}
	
	public static int randomPositiveIntModulus(int modulus) {
		return Math.abs(_random.nextInt() % modulus);
	}
	
	public static float randomPositiveFloat() {
		return Math.abs(_random.nextFloat());
	}
	
	public static String randomProvider() {
		return _providers[randomPositiveIntModulus(2)];
	}
	
	public static String randomString(int length) {
    	StringBuilder builder = new StringBuilder();
    	
    	for(int i = 0; i < length; i++) {
    		builder.append(chars[randomPositiveIntModulus(chars.length - 1)]);
    	}
    	
    	return builder.toString();
    }
	
	public static String randomLocationStatus() {
		return _locationStatuses[randomPositiveIntModulus(3)];
	}
	
	public static String randomMacAddress() {
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < 6; i++) {
			builder.append(_hexChars[randomPositiveIntModulus(_hexChars.length)])
			   .append( _hexChars[randomPositiveIntModulus(_hexChars.length)]);
			
			if(i < 5) {
				builder.append(":");
			}
		}
		
		return builder.toString();
	}
}
