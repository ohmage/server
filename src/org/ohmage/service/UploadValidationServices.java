package org.ohmage.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.DateUtils;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;

/**
 * Helper services for working with uploaded data values that span multiple 
 * requests.
 * 
 * @author Joshua Selsky
 */
public final class UploadValidationServices {
	private static final String ISO_8601_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	private static final List<String> LOCATION_STATUSES = Arrays.asList(new String[] {
		JsonInputKeys.METADATA_LOCATION_STATUS_UNAVAILABLE,
		JsonInputKeys.METADATA_LOCATION_STATUS_INACCURATE,
		JsonInputKeys.METADATA_LOCATION_STATUS_VALID,
		JsonInputKeys.METADATA_LOCATION_STATUS_STALE
	});

	private static final String DATE_ERROR_MESSAGE_NULL = "The date in an upload message is empty or null";
	private static final String DATE_ERROR_MESSAGE_INVALID = "The date in an upload message is invalid: ";

	private static final String LOCATION_TIMESTAMP_ERROR_MESSAGE_NULL = "The location timestamp in a survey upload message is empty or null";
	private static final String LOCATION_TIMESTAMP_ERROR_MESSAGE_INVALID = "The location timestamp in a survey upload message is invalid: ";
	
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
	 * @param errorMessageEmptyOrNull The error message to push into the
	 * Request when validation fails because the timestamp is null or empty.
	 * @param errorMessageInvalid The error message to push into the Request when
	 * validation fails because the timestamp is malformed.
	 * 
	 * @see java.util.SimpleDateFormat
	 * @throws ServiceException if the timestamp is null or an unparseable ISO8601 timestamp
	 */
	public static void validateIso8601Timestamp(Request request, String timestamp, String errorMessageEmptyOrNull, String errorMessageInvalid) 
		throws ServiceException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(timestamp)) {
			request.setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, errorMessageEmptyOrNull);
			throw new ServiceException(errorMessageEmptyOrNull);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_TIMESTAMP_PATTERN);
		sdf.setLenient(false);
		
		try {
			
			sdf.parse(timestamp);
			
		} catch (ParseException pe) {
			
			String msg = errorMessageInvalid + timestamp;
			request.setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, msg);
			throw new ServiceException(msg, pe);
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
		if(StringUtils.isEmptyOrWhitespaceOnly(timezone)) {
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
	
	/**
	 * Validates the provided latitude.
	 * 
	 * @param request   The request to fail if the latitude is invalid.
	 * @param latitude  The latitude to validate
	 * @throws ServiceException if the latitude is null or not within the range
	 * of a correct latitude (-90 < latitude < 90)
	 */
	public static void validateLatitude(Request request, Double latitude) throws ServiceException {
		if(latitude == null) {
			failRequestAndThrowServiceException(
				request, 
				ErrorCodes.SERVER_INVALID_LOCATION,
				"latitude in upload message is null"
			);
		}
		
		if(latitude.doubleValue() < -90d || latitude.doubleValue() > 90d) {
			failRequestAndThrowServiceException(
				request, 
				ErrorCodes.SERVER_INVALID_LOCATION,
                "latitude in upload message is invalid: " + latitude
            );
		}
	}
	
	/**
	 * Validates the provided longitude.
	 * 
	 * @param request   The request to fail if the latitude is invalid.
	 * @param longitude The longitude to validate
	 * @throws ServiceException if the latitude is null or not within the range
	 * of a correct longitude (-180 < latitude < 180)
	 */
    public static void validateLongitude(Request request, Double longitude) throws ServiceException {
    	if(longitude == null) {
    		failRequestAndThrowServiceException(
    			request, 
    			ErrorCodes.SERVER_INVALID_LOCATION,
    			"longitude in upload message is null"
    		);
		}
		
		if(longitude.doubleValue() < -180d || longitude.doubleValue() > 180d) {
			failRequestAndThrowServiceException(
				request, 
				ErrorCodes.SERVER_INVALID_LOCATION,
                "longitude in upload message is invalid: " + longitude
            );
		}
	}
    
    /**
	 * Validates the provided accuracy (the accuracy of a GPS or Network derived latlong).
	 * 
	 * @param request   The request to fail if the accuracy is invalid.
	 * @param accuracy  The accuracy to validate
	 * @throws ServiceException If the accuracy is null or an unparseable float
	 */
    public static void validateAccuracy(Request request, String accuracy) throws ServiceException {
    	if(accuracy == null) {
    		failRequestAndThrowServiceException(
    			request, 
    			ErrorCodes.SERVER_INVALID_LOCATION,
    			"accuracy in upload message is null"
    		);
		}
		
    	try {
    		
			Float.parseFloat(accuracy);
			
		} catch (NumberFormatException nfe) {
			
			failRequestAndThrowServiceException(
    			request, 
    			ErrorCodes.SERVER_INVALID_LOCATION,
    			"accuracy in upload message is an unparseable float: " + accuracy
    		);
		}
	}
    
    /**
	 * Validates the provider (the source of a latlong e.g., network, GPS, WifiGPSLocationService).
	 * 
	 * @param request   The request to fail if the provider is invalid.
	 * @param provider  The provider to validate
	 * @throws ServiceException If the provider is empty or null
	 */
    public static void validateProvider(Request request, String provider) throws ServiceException {
    	if(StringUtils.isEmptyOrWhitespaceOnly(provider)) {
    		failRequestAndThrowServiceException(
    			request, 
    			ErrorCodes.SERVER_INVALID_LOCATION,
    			"provider in upload message is null"
    		);
		}
	}
    
	/**
	 * Validates that the provided location status is not null and an accepted
	 * location status value.
	 * 
	 * @param request  The request to fail should the location status be invalid.
	 * @param locationStatus  The location status to validate.
	 * @throws ServiceException  If the location status is null or invalid.
	 */
	public static void validateLocationStatus(Request request, String locationStatus) throws ServiceException {
		if(locationStatus == null) {
			String msg = "location_status in upload message is null";
			request.setFailed(ErrorCodes.SERVER_INVALID_LOCATION_STATUS, msg);
			throw new ServiceException(msg);
		}
		
		if(! LOCATION_STATUSES.contains(locationStatus)) {
			String msg = "location_status in upload message is invalid: " + locationStatus;
			request.setFailed(ErrorCodes.SERVER_INVALID_LOCATION_STATUS, msg);
			throw new ServiceException(msg);
		}
	}
	
	/**
	 * Validates the provided location object. If the location status is 
	 * unavailable, it is an error to send a location object. Otherwise,
	 * the location object must contain values for longitude, latitude,
	 * accuracy, provider, and timestamp.
	 *  
	 * @param request  The request to fail should the location object be
	 * invalid.
	 * @param location  A JSON object containing location properties to be
	 * validated.
	 * @throws ServiceException  If the location is null and the location 
	 * status is unavailable or if the location status is not unavailable
	 * and the location is invalid in structure.
	 */
	public static void validateLocation(Request request, JSONObject location, String locationStatus) throws ServiceException {
		if(locationStatus.equals(JsonInputKeys.METADATA_LOCATION_STATUS_UNAVAILABLE) && location != null) {
			String msg = "location object in upload message exists, but location status is unavailable";
			request.setFailed(ErrorCodes.SERVER_INVALID_LOCATION, msg);
			throw new ServiceException(msg);
		}
		
		if(! locationStatus.equals(JsonInputKeys.METADATA_LOCATION_STATUS_UNAVAILABLE) && location == null) {
			String msg = "missing location object in upload message";
			request.setFailed(ErrorCodes.SERVER_INVALID_LOCATION, msg);
			throw new ServiceException(msg);
		}
		
		if(location != null) {
			UploadValidationServices.validateLatitude(request, JsonUtils.getDoubleFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_LATITUDE));
			UploadValidationServices.validateLongitude(request, JsonUtils.getDoubleFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_LONGITUDE));
			UploadValidationServices.validateAccuracy(request, JsonUtils.getStringFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_ACCURACY));
			UploadValidationServices.validateProvider(request, JsonUtils.getStringFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_PROVIDER));
			
			UploadValidationServices.validateIso8601Timestamp(
				request,
				JsonUtils.getStringFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_TIMESTAMP),
				LOCATION_TIMESTAMP_ERROR_MESSAGE_NULL,
				LOCATION_TIMESTAMP_ERROR_MESSAGE_INVALID
			);
		}
	}
	
	/**
	 * Validates the metadata in the provided JSON object:
	 * {@value org.ohmage.request.JsonInputKeys#METADATA_DATE},
	 * {@value org.ohmage.request.JsonInputKeys#JSON_METADATA_TIME},
	 * {@value org.ohmage.request.JsonInputKeys#METADATA_TIMEZONE},
	 * {@value org.ohmage.request.JsonInputKeys#METADATA_LOCATION_STATUS},
	 * {@value org.ohmage.request.JsonInputKeys#METADATA_DATE},
	 * 
	 *  
	 * @param request  The request to fail should the location object be
	 * invalid.
	 * @param location  A JSON object containing location properties to be
	 * validated.
	 * @throws ServiceException  If the location is null and the location 
	 * status is unavailable or if the location status is not unavailable
	 * and the location is invalid in structure.
	 */
	public static void validateUploadMetadata(Request request, JSONObject uploadObject) throws ServiceException {
		// FIXME this is actually a timestamp -- it should be renamed in the HTTP API calls that use it
		String metadataDate = JsonUtils.getStringFromJsonObject(uploadObject, JsonInputKeys.METADATA_DATE);
		Long epochTime = JsonUtils.getLongFromJsonObject(uploadObject, JsonInputKeys.METADATA_TIME);
		String timezone = JsonUtils.getStringFromJsonObject(uploadObject, JsonInputKeys.METADATA_TIMEZONE);
		String locationStatus = JsonUtils.getStringFromJsonObject(uploadObject, JsonInputKeys.METADATA_LOCATION_STATUS);
		JSONObject location = JsonUtils.getJsonObjectFromJsonObject(uploadObject, JsonInputKeys.METADATA_LOCATION);
		
		validateIso8601Timestamp(request, metadataDate, DATE_ERROR_MESSAGE_NULL, DATE_ERROR_MESSAGE_INVALID);
		validateEpochTime(request, epochTime);
		validateTimezone(request, timezone);
		validateLocationStatus(request, locationStatus);
		validateLocation(request, location, locationStatus);
	}
	
    private static void failRequestAndThrowServiceException(Request request, String errorCode, String msg) throws ServiceException {
		request.setFailed(errorCode, msg);
		throw new ServiceException(msg);
	}
}
