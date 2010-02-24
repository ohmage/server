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
	private static String[] _modes = {"still", "walk", "run", "bike", "drive"};
	
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

	public static long epoch(int days) {
		return System.currentTimeMillis() + (21600000l * days); 
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

	public static String date(int days) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
// use the local timezone for now
// if specific timezones are added in the future, the epoch method will have to change so the value returned has the same tz
//		sdf.setTimeZone(TimeZone.getTimeZone(tz));
		return sdf.format(new Date(epoch(days)));
	}
	
	public static String mode() {
		return _modes[Math.abs(_random.nextInt()) % (_modes.length)];
	}
	
	public static double randomPositiveDouble() {
		return Math.abs(_random.nextDouble());
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
	
	public static int randomPositiveIntModulus(int modulus) {
		return Math.abs(_random.nextInt() % modulus);
	}
}
