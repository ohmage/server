package org.ohmage.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
import org.ohmage.util.DateUtils;

/**
 * Helper services for working with uploaded data values that span multiple 
 * requests.
 * 
 * @author Joshua Selsky
 */
public final class UploadValidationServices {
	private static final String ISO_8601_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Private to enforce non-instantiation.
	 */
	private UploadValidationServices() { }
	
	
	/**
	 * Validates the provided timestamp. The timestamp must be of the form 
	 * yyyy-MM-dd HH:mm:ss in SimpleDateFormat pattern parlance. 
	 * 
	 * @param request The request to fail should the timestamp be invalid.
	 * @param date The date to validate.
	 * @see java.util.SimpleDateFormat
	 * @throws ServiceException if the timestamp is null or an unparseable ISO8601 timestamp
	 */
	public static void validateIso8601Timestamp(Request request, String timestamp) throws ServiceException {
		if(timestamp == null) {
			String msg = "timestamp ('date') in upload message is null";
			request.setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, msg);
			throw new ServiceException(msg);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_TIMESTAMP_PATTERN);
		sdf.setLenient(false);
		
		try {
			
			sdf.parse(timestamp);
			
		} catch (ParseException pe) {
			
			String msg = "timestamp ('date') in upload message is invalid: " + timestamp;
			request.setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, msg);
			throw new ServiceException(msg);
		}
	}
	
	/**
	 * Validates the provided (UNIX epoch-style) time. 
	 * 
	 * @param request The request to fail should the time be invalid.
	 * @param date The date to validate.
	 * @throws ServiceException if the time is null or negative
	 */
	public static void validateEpochTime(Request request, Long time) throws ServiceException {
		if(time == null) {
			String msg = "time in upload message is null";
			request.setFailed(ErrorCodes.SERVER_INVALID_TIME, msg);
			throw new ServiceException(msg);
		}
		
		if(time < 0) {	
			String msg = "time in upload message is invalid: " + time;
			request.setFailed(ErrorCodes.SERVER_INVALID_TIME, msg);
			throw new ServiceException(msg);
		}
	}
	
	/**
	 * Validates the provided timezone against the timezone abbreviations
	 * bundled with the JVM. WARNING: In some rare cases, client applications
	 * may be bundled with a superset of timezones (certain Android versions do
	 * this). This method will throw a ServiceException if the server JVM 
	 * does not understand the provided timezone.  
	 *  
	 * @param request The request to fail should the timezone be invalid.
	 * @param timezone The timezone to validate.
	 * @throws ServiceException if the timezone is null or unknown
	 */
	public static void validateTimezone(Request request, String timezone) throws ServiceException {
		if(timezone == null) {
			String msg = "timezone in upload message is null";
			request.setFailed(ErrorCodes.SERVER_INVALID_TIMEZONE, msg);
			throw new ServiceException(msg);
		}
		
		if(! DateUtils.isValidTimezone(timezone)) {
			String msg = "timezone in upload message is invalid/unknown to the server: " + timezone;
			request.setFailed(ErrorCodes.SERVER_INVALID_TIMEZONE, msg);
			throw new ServiceException(msg);
		}
	}
}
