package org.ohmage.validator;

import java.util.Date;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ValidationException;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

public class AuditValidators {
	private static final Logger LOGGER = Logger.getLogger(AuditValidators.class);
	
	private static final int MAX_CLIENT_LENGTH = 255;
	
	public static enum ResponseType { SUCCESS, FAILURE };
	
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
	 * @param request The Request that is performing this validation.
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
	public static RequestType validateRequestType(Request request, String requestType) throws ValidationException {
		LOGGER.info("Validating a HTTP request type.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(requestType)) {
			return null;
		}
		
		try {
			return RequestType.valueOf(requestType.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.AUDIT_INVALID_RESPONSE_TYPE, "The request type must be one of '" + 
					RequestType.DELETE.toString().toLowerCase() + "', '" +
					RequestType.GET.toString().toLowerCase() + "', '" +
					RequestType.HEAD.toString().toLowerCase() + "', '" +
					RequestType.OPTIONS.toString().toLowerCase() + "', '" +
					RequestType.POST.toString().toLowerCase() + "', '" +
					RequestType.PUT.toString().toLowerCase() + "', '" +
					RequestType.TRACE.toString().toLowerCase() + "', or '" +
					RequestType.UNKNOWN.toString().toLowerCase() + "'." +
					" Given: " + requestType);
			throw new ValidationException("Invalid request type given: " + requestType, e);
		}
	}
	
	/**
	 * Checks if the URI is null or whitespace only. We put no other 
	 * limitations on the URI because a user may want to read about URIs that
	 * the server doesn't know about but someone is attempting to call anyway.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param uri The URI as a string.
	 * 
	 * @return The URI as a string unless it was null or whitespace only in
	 * 		   which case null is returned.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static String validateUri(Request request, String uri) throws ValidationException {
		LOGGER.info("Vaidating an URI.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(uri)) {
			return null;
		}
		
		return uri.trim();
	}
	
	/**
	 * Checks if the client string is null or whitespace only. We put no other
	 * limitations on the client string because a user may want to query about
	 * client values that the server doesn't know about.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param client The client string value.
	 * 
	 * @return The client string value unless it was null or whitespace only in
	 * 		   which case null is returned. 
	 * 
	 * @throws ValidationException Thrown if the client value is greater than
	 * 							   {@value #MAX_CLIENT_LENGTH} characters.
	 */
	public static String validateClient(Request request, String client) throws ValidationException {
		LOGGER.info("Validating a client value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			return null;
		}
		
		if(client.trim().length() > MAX_CLIENT_LENGTH) {
			request.setFailed(ErrorCodes.SERVER_INVALID_CLIENT, "The client value cannot be longer than " + MAX_CLIENT_LENGTH + " characters.");
			throw new ValidationException("The client value cannot be longer than " + MAX_CLIENT_LENGTH + " characters.");
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
	 * @param request The Request that is performing this validation.
	 * 
	 * @param deviceId The device ID.
	 * 
	 * @return The device ID string unless it was null or whitespace only in
	 * 		   which case null is returned.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static String validateDeviceId(Request request, String deviceId) throws ValidationException {
		LOGGER.info("Validating a device ID.");
		
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
	 * @param request The Request that is performing this validation.
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
	public static ResponseType validateResponseType(Request request, String responseType) throws ValidationException {
		LOGGER.info("Validating a response type.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(responseType)) {
			return null;
		}
		
		try {
			return ResponseType.valueOf(responseType.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.AUDIT_INVALID_RESPONSE_TYPE, "The response type must be one of '" + 
					ResponseType.SUCCESS.toString().toLowerCase() + "' or '" +
					ResponseType.FAILURE.toString().toLowerCase() + "'." +
					" Given: " + responseType);
			throw new ValidationException("Invalid request type given: " + responseType, e);
		}
	}
	
	/**
	 * Checks if the error is null or whitespace only in which case it returns
	 * null; otherwise it returns the error code. There are no restrictions on
	 * what an error code value is allowed to be because they may change over
	 * time and someone might want to query about an old one.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param errorCode The error code to be validated.
	 * 
	 * @return Null if the error code was null or whitespace only; otherwise,
	 * 		   the error code is returned.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static String validateErrorCode(Request request, String errorCode) throws ValidationException {
		LOGGER.info("Validating an error code.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(errorCode)) {
			return null;
		}
		
		return errorCode.trim();
	}
	
	/**
	 * Validates that the start date is a valid date.
	 *  
	 * @param request The Request that is performing this validation.
	 * 
	 * @param startDate The start date as a string to be validated.
	 * 
	 * @return Returns null if the start date is null or whitespace only; 
	 * 		   otherwise, it returns a Date representing the start date string.
	 * 
	 * @throws ValidationException Thrown if the start date is not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateStartDate(Request request, String startDate) throws ValidationException {
		LOGGER.info("Validating a start date.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			return null;
		}
		
		Date date = StringUtils.decodeDateTime(startDate);
		if(date == null) {
			date = StringUtils.decodeDate(startDate);
			
			if(date == null) {
				request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date is invalid: " + startDate);
				throw new ValidationException("The start date is invalid: " + startDate);
			}
		}
		
		return date;
	}
	
	/**
	 * Validates that the end date is a valid date.
	 *  
	 * @param request The Request that is performing this validation.
	 * 
	 * @param startDate The end date as a string to be validated.
	 * 
	 * @return Returns null if the end date is null or whitespace only; 
	 * 		   otherwise, it returns a Date representing the end date string.
	 * 
	 * @throws ValidationException Thrown if the end date is not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateEndDate(Request request, String endDate) throws ValidationException {
		LOGGER.info("Validating an end date.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			return null;
		}
		
		Date date = StringUtils.decodeDateTime(endDate);
		if(date == null) {
			date = StringUtils.decodeDate(endDate);
			
			if(date == null) {
				request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The end date is invalid: " + endDate);
				throw new ValidationException("The end date is invalid: " + endDate);
			}
		}
		
		return date;
	}
}