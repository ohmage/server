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
package org.ohmage.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.util.DateTimeUtils;

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
	 * @param date The date to validate.
	 * @param errorMessageEmptyOrNull The error message to push into the
	 * Request when validation fails because the timestamp is null or empty.
	 * @param errorMessageInvalid The error message to push into the Request when
	 * validation fails because the timestamp is malformed.
	 * 
	 * @see java.util.SimpleDateFormat
	 * @throws ServiceException if the timestamp is null or an unparseable ISO8601 timestamp
	 */
	public static void validateIso8601Timestamp(final String timestamp, 
			final String errorMessageEmptyOrNull, 
			final String errorMessageInvalid) throws ServiceException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(timestamp)) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_TIMESTAMP, 
					errorMessageEmptyOrNull);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_TIMESTAMP_PATTERN);
		sdf.setLenient(false);
		
		try {
			
			sdf.parse(timestamp);
			
		} catch (ParseException pe) {
			
			String msg = errorMessageInvalid + timestamp;
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_TIMESTAMP, 
					msg, 
					pe);
		}
	}
	
	/**
	 * Validates the provided (UNIX epoch-style) time. 
	 * 
	 * @param date The date to validate.
	 * @throws ServiceException if the time is null or negative
	 */
	public static void validateEpochTime(final Long time) 
			throws ServiceException {
		
		if(time == null) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_TIME, 
					"time in upload message is null");
		}
		
		if(time < 0) {	
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_TIME, 
					"time in upload message is invalid: " + time);
		}
	}
	
	/**
	 * Validates the provided timezone against the timezone abbreviations
	 * bundled with the JVM. WARNING: In some rare cases, client applications
	 * may be bundled with a superset of timezones (certain Android versions do
	 * this). This method will throw a ServiceException if the server JVM 
	 * does not understand the provided timezone.  
	 *  
	 * @param timezone The timezone to validate.
	 * @throws ServiceException if the timezone is null or unknown
	 */
	public static void validateTimezone(final String timezone) 
			throws ServiceException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(timezone)) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_TIMEZONE, 
					"timezone in upload message is null");
		}
		
		if(! DateTimeUtils.isValidTimezone(timezone)) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_TIMEZONE, 
					"timezone in upload message is invalid/unknown to the server: " + 
						timezone);
		}
	}
	
	/**
	 * Validates the provided latitude.
	 * 
	 * @param latitude  The latitude to validate
	 * @throws ServiceException if the latitude is null or not within the range
	 * of a correct latitude (-90 < latitude < 90)
	 */
	public static void validateLatitude(final Double latitude) 
			throws ServiceException {
		
		if(latitude == null) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_LOCATION,
					"latitude in upload message is null");
		}
		
		if(latitude.doubleValue() < -90d || latitude.doubleValue() > 90d) {
			throw new ServiceException(
				ErrorCode.SERVER_INVALID_LOCATION,
                "latitude in upload message is invalid: " + latitude);
		}
	}
	
	/**
	 * Validates the provided longitude.
	 * 
	 * @param longitude The longitude to validate
	 * @throws ServiceException if the latitude is null or not within the range
	 * of a correct longitude (-180 < latitude < 180)
	 */
    public static void validateLongitude(final Double longitude) 
    		throws ServiceException {
    	
    	if(longitude == null) {
    		throw new ServiceException(
    			ErrorCode.SERVER_INVALID_LOCATION,
    			"longitude in upload message is null");
		}
		
		if(longitude.doubleValue() < -180d || longitude.doubleValue() > 180d) {
			throw new ServiceException(
				ErrorCode.SERVER_INVALID_LOCATION,
                "longitude in upload message is invalid: " + longitude);
		}
	}
    
    /**
	 * Validates the provided accuracy (the accuracy of a GPS or Network derived latlong).
	 * 
	 * @param accuracy  The accuracy to validate
	 * @throws ServiceException If the accuracy is null or an unparseable float
	 */
    public static void validateAccuracy(final String accuracy) 
    		throws ServiceException {
    	
    	if(accuracy == null) {
    		throw new ServiceException(
    			ErrorCode.SERVER_INVALID_LOCATION,
    			"accuracy in upload message is null");
		}
		
    	try {
    		
			Float.parseFloat(accuracy);
			
		} catch (NumberFormatException nfe) {
			throw new ServiceException(
    			ErrorCode.SERVER_INVALID_LOCATION,
    			"accuracy in upload message is an unparseable float: " + accuracy,
    			nfe);
		}
	}
    
    /**
	 * Validates the provider (the source of a latlong e.g., network, GPS, WifiGPSLocationService).
	 * 
	 * @param request   The request to fail if the provider is invalid.
	 * @param provider  The provider to validate
	 * @throws ServiceException If the provider is empty or null
	 */
    public static void validateProvider(final String provider) 
    		throws ServiceException {
    	
    	if(StringUtils.isEmptyOrWhitespaceOnly(provider)) {
    		throw new ServiceException(
    			ErrorCode.SERVER_INVALID_LOCATION,
    			"provider in upload message is null");
		}
	}
    
	/**
	 * Validates that the provided location status is not null and an accepted
	 * location status value.
	 * 
	 * @param locationStatus  The location status to validate.
	 * @throws ServiceException  If the location status is null or invalid.
	 */
	public static void validateLocationStatus(final String locationStatus) 
			throws ServiceException {
		
		if(locationStatus == null) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
					"location_status in upload message is null");
		}
		
		if(! LOCATION_STATUSES.contains(locationStatus)) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
					"location_status in upload message is invalid: " + 
						locationStatus);
		}
	}
	
	/**
	 * Validates the provided location object. If the location status is 
	 * unavailable, it is an error to send a location object. Otherwise,
	 * the location object must contain values for longitude, latitude,
	 * accuracy, provider, and timestamp.
	 *  
	 * @param location  A JSON object containing location properties to be
	 * validated.
	 * @throws ServiceException  If the location is null and the location 
	 * status is unavailable or if the location status is not unavailable
	 * and the location is invalid in structure.
	 */
	public static void validateLocation(final JSONObject location, 
			final String locationStatus) throws ServiceException {
		
		if(locationStatus.equals(JsonInputKeys.METADATA_LOCATION_STATUS_UNAVAILABLE) && location != null) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_LOCATION, 
					"location object in upload message exists, but location status is unavailable");
		}
		
		if(! locationStatus.equals(JsonInputKeys.METADATA_LOCATION_STATUS_UNAVAILABLE) && location == null) {
			throw new ServiceException(
					ErrorCode.SERVER_INVALID_LOCATION, 
					"missing location object in upload message");
		}
		
		if(location != null) {
			UploadValidationServices.validateLatitude(JsonUtils.getDoubleFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_LATITUDE));
			UploadValidationServices.validateLongitude(JsonUtils.getDoubleFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_LONGITUDE));
			UploadValidationServices.validateAccuracy(JsonUtils.getStringFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_ACCURACY));
			UploadValidationServices.validateProvider(JsonUtils.getStringFromJsonObject(location, JsonInputKeys.METADATA_LOCATION_PROVIDER));
			
			UploadValidationServices.validateIso8601Timestamp(
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
	 * @param location  A JSON object containing location properties to be
	 * validated.
	 * @throws ServiceException  If the location is null and the location 
	 * status is unavailable or if the location status is not unavailable
	 * and the location is invalid in structure.
	 */
	public static void validateUploadMetadata(final JSONObject uploadObject) 
			throws ServiceException {
		
		// FIXME this is actually a timestamp -- it should be renamed in the HTTP API calls that use it
		String metadataDate = JsonUtils.getStringFromJsonObject(uploadObject, JsonInputKeys.METADATA_DATE);
		Long epochTime = JsonUtils.getLongFromJsonObject(uploadObject, JsonInputKeys.METADATA_TIME);
		String timezone = JsonUtils.getStringFromJsonObject(uploadObject, JsonInputKeys.METADATA_TIMEZONE);
		String locationStatus = JsonUtils.getStringFromJsonObject(uploadObject, JsonInputKeys.METADATA_LOCATION_STATUS);
		JSONObject location = JsonUtils.getJsonObjectFromJsonObject(uploadObject, JsonInputKeys.METADATA_LOCATION);
		
		validateIso8601Timestamp(metadataDate, DATE_ERROR_MESSAGE_NULL, DATE_ERROR_MESSAGE_INVALID);
		validateEpochTime(epochTime);
		validateTimezone(timezone);
		validateLocationStatus(locationStatus);
		validateLocation(location, locationStatus);
	}
}
