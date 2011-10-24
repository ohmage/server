package org.ohmage.validator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.util.StringUtils;

public class AuditValidators {
	/**
	 * The maximum length of a client value.
	 */
	public static final int MAX_CLIENT_LENGTH = 255;
	
	/**
	 * The possible response types for a request.
	 * 
	 * @author John Jenkins
	 */
	public static enum ResponseType { 
		SUCCESS, 
		FAILURE;
		
		/**
		 * Returns the ResponseType object that represents the value.
		 *  
		 * @param value The value to use to get a ResponseType object.
		 * 
		 * @return The ResponseType object.
		 * 
		 * @throws IllegalArgumentException Thrown if the value is not 
		 * 									decodable as a response type value.
		 */
		public static ResponseType getValue(final String value) {
			return valueOf(value.toUpperCase());
		}
		
		/**
		 * This response type as a human-readable string.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private AuditValidators() {}
	
	/**
	 * Validates that the given request type is a valid RequestType and returns
	 * a RequestType object representing it or is null or whitespace only and
	 * returns null. If it is not null or whitespace only and not a valid 
	 * RequestType, a ValidationException is thrown.
	 * 
	 * @param requestType The RequestType as a String.
	 * 
	 * @return Null if 'requestType' is null or whitespace only; otherwise, a
	 * 		   RequestType that the 'requestType' represented.
	 * 
	 * @throws ValidationException Thrown if the request type is not null, not
	 * 							   whitespace only, and not a valid 
	 * 							   RequestType.
	 */
	public static RequestType validateRequestType(final String requestType) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(requestType)) {
			return null;
		}
		
		try {
			return RequestType.valueOf(requestType.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.AUDIT_INVALID_RESPONSE_TYPE, 
					"The request type must be one of '" + 
						RequestType.DELETE.toString().toLowerCase() + "', '" +
						RequestType.GET.toString().toLowerCase() + "', '" +
						RequestType.HEAD.toString().toLowerCase() + "', '" +
						RequestType.OPTIONS.toString().toLowerCase() + "', '" +
						RequestType.POST.toString().toLowerCase() + "', '" +
						RequestType.PUT.toString().toLowerCase() + "', '" +
						RequestType.TRACE.toString().toLowerCase() + "', or '" +
						RequestType.UNKNOWN.toString().toLowerCase() + "'." +
						" Given: " + requestType,
					"Invalid request type given: " + 
						requestType, 
					e
				);
		}
	}
	
	/**
	 * Validates that the URI string is a valid URI per Java's implementation 
	 * URI.<br />
	 * <br />
	 * Note: The definition of a URI is very liberal and great care should
	 * be taken when using the value that is returned.<br />
	 * <br />
	 * Note: An empty string is considered a valid URI, however our approach 
	 * for handling empty string across all validators is to return null for an
	 * empty string.
	 * 
	 * @param uri The URI as a string.
	 * 
	 * @return The URI as a Java URI object.
	 * 
	 * @throws ValidationException If the URI string cannot be converted into a
	 * 							   URI object.
	 */
	public static URI validateUri(final String uri) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(uri)) {
			return null;
		}
		
		try {
			return new URI(uri);
		}
		catch(URISyntaxException e) {
			throw new ValidationException(
					ErrorCode.AUDIT_INVALID_URI, 
					"The URI was invalid: " + uri, 
					e
				);
		}
	}
	
	/**
	 * Checks if the client string is null or whitespace only. We put no other
	 * limitations on the client string because a user may want to query about
	 * client values that the server doesn't know about.
	 * 
	 * @param client The client string value.
	 * 
	 * @return The client string value unless it was null or whitespace only in
	 * 		   which case null is returned. 
	 * 
	 * @throws ValidationException Thrown if the client value is greater than
	 * 							   {@value #MAX_CLIENT_LENGTH} characters.
	 */
	public static String validateClient(final String client) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			return null;
		}
		
		if(client.trim().length() > MAX_CLIENT_LENGTH) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_CLIENT, 
					"The client value cannot be longer than " + 
						MAX_CLIENT_LENGTH + 
						" characters."
				);
		}
		else {
			return client.trim();
		}
	}
	
	/**
	 * Checks if the device ID is null or whitespace only. We put no other
	 * limitations on the device ID because a user may want to query about 
	 * device IDs that the server doesn't know about.
	 * 
	 * @param deviceId The device ID.
	 * 
	 * @return The device ID string unless it was null or whitespace only in
	 * 		   which case null is returned.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static String validateDeviceId(final String deviceId) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(deviceId)) {
			return null;
		}
		
		return deviceId.trim();
	}
	
	/**
	 * Validates that the response time is a valid response time and returns a
	 * ResponseType object based on its value, unless its null or whitespace
	 * only in which case null is returned.
	 * 
	 * @param responseType The response type as a string.
	 * 
	 * @return Returns null if the response type was null or whitespace only;
	 * 		   otherwise, a ResponseType object is returned based on the 
	 * 		   response type.
	 * 
	 * @throws ValidationException Thrown if the response type is not null, not
	 * 							   whitespace only, and not a valid response
	 * 							   type.
	 */
	public static ResponseType validateResponseType(final String responseType) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(responseType)) {
			return null;
		}
		
		try {
			return ResponseType.valueOf(responseType.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					"Invalid request type given: " + responseType, 
					e
				);
		}
	}
	
	/**
	 * Checks if the error is null or whitespace only in which case it returns
	 * null; otherwise it returns the error code which must be one of
	 * {@link org.ohmage.annotator.Annotator.ErrorCode ErrorCode}.
	 * 
	 * @param errorCode The error code to be validated.
	 * 
	 * @return Null if the error code was null or whitespace only; otherwise,
	 * 		   the error code is returned.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static ErrorCode validateErrorCode(final String errorCode) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(errorCode)) {
			return null;
		}
		
		try {
			return ErrorCode.getValue(errorCode);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.AUDIT_INVALID_ERROR_CODE, 
					"The error code is unknown: " + errorCode, 
					e);
		}
	}
	
	/**
	 * Validates that the start date is a valid date.
	 *  
	 * @param startDate The start date as a string to be validated.
	 * 
	 * @return Returns null if the start date is null or whitespace only; 
	 * 		   otherwise, it returns a Date representing the start date string.
	 * 
	 * @throws ValidationException Thrown if the start date is not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateStartDate(final String startDate) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			return null;
		}
		
		Date date = StringUtils.decodeDateTime(startDate);
		if(date == null) {
			date = StringUtils.decodeDate(startDate);
			
			if(date == null) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_DATE, 
						"The start date is invalid: " + startDate
					);
			}
		}
		
		return date;
	}
	
	/**
	 * Validates that the end date is a valid date.
	 *  
	 * @param startDate The end date as a string to be validated.
	 * 
	 * @return Returns null if the end date is null or whitespace only; 
	 * 		   otherwise, it returns a Date representing the end date string.
	 * 
	 * @throws ValidationException Thrown if the end date is not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateEndDate(final String endDate) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			return null;
		}
		
		Date date = StringUtils.decodeDateTime(endDate);
		if(date == null) {
			date = StringUtils.decodeDate(endDate);
			
			if(date == null) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_DATE, 
						"The end date is invalid: " + endDate);
			}
		}
		
		return date;
	}
}