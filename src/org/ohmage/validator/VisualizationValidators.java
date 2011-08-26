package org.ohmage.validator;

import java.util.Date;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating all values pertaining to 
 * visualization requests.
 * 
 * @author John Jenkins
 */
public class VisualizationValidators {
	// The width for 1080p.
	private static final int MAX_IMAGE_DIMENSION = 1920;
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private VisualizationValidators() {}
	
	/**
	 * Validates that the desired image width is not less than 0 and not  
	 * greater than {@value #MAX_IMAGE_DIMENSION}.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param width The width to be validated.
	 * 
	 * @return Returns null if the width is null or whitespace only. Otherwise,
	 * 		   it returns a decoded value for the width.
	 * 
	 * @throws ValidationException Thrown if the width is not null, not 
	 * 							   whitespace only, and not a valid number or
	 * 							   a number that is out of bounds.
	 */
	public static Integer validateWidth(Request request, String width) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(width)) {
			return null;
		}
		
		try {
			Integer result =  Integer.decode(width);
			
			if(result < 0) {
				request.setFailed(ErrorCodes.VISUALIZATION_INVALID_WIDTH_VALUE, "The image's width cannot be less than 0: " + result);
				throw new ValidationException("The image's width cannot be less than 0: " + result);
			}
			else if(result > MAX_IMAGE_DIMENSION) {
				request.setFailed(ErrorCodes.VISUALIZATION_INVALID_WIDTH_VALUE, "The image's width cannot be greater than " + MAX_IMAGE_DIMENSION + ": " + result);
				throw new ValidationException("The image's width cannot be greater than " + MAX_IMAGE_DIMENSION + ": " + result);
			}
			else {
				return result;
			}
		}
		catch(NumberFormatException e) {
			request.setFailed(ErrorCodes.VISUALIZATION_INVALID_WIDTH_VALUE, "The image's width is not a number: " + width);
			throw new ValidationException("The image's width is not a valid number: " + width, e);
		}
	}
	
	/**
	 * Validates that the desired image height is not less than 0 and not  
	 * greater than {@value #MAX_IMAGE_DIMENSION}.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param height The height to be validated.
	 * 
	 * @return Returns null if the height is null or whitespace only. 
	 * 		   Otherwise, it returns a decoded value for the height.
	 * 
	 * @throws ValidationException Thrown if the height is not null, not 
	 * 							   whitespace only, and not a valid number or
	 * 							   a number that is out of bounds.
	 */
	public static Integer validateHeight(Request request, String height) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(height)) {
			return null;
		}
		
		try {
			Integer result =  Integer.decode(height);
			
			if(result < 0) {
				request.setFailed(ErrorCodes.VISUALIZATION_INVALID_WIDTH_VALUE, "The image's width cannot be less than 0: " + result);
				throw new ValidationException("The image's width cannot be less than 0: " + result);
			}
			else if(result > MAX_IMAGE_DIMENSION) {
				request.setFailed(ErrorCodes.VISUALIZATION_INVALID_WIDTH_VALUE, "The image's width cannot be greater than " + MAX_IMAGE_DIMENSION + ": " + result);
				throw new ValidationException("The image's width cannot be greater than " + MAX_IMAGE_DIMENSION + ": " + result);
			}
			else {
				return result;
			}
		}
		catch(NumberFormatException e) {
			request.setFailed(ErrorCodes.VISUALIZATION_INVALID_WIDTH_VALUE, "The image's width is not a number: " + height);
			throw new ValidationException("The image's width is not a valid number: " + height, e);
		}
	}
	
	/**
	 * Validates that a start date string is a valid date and returns it. If it
	 * is not a valid date, the request is failed and an exception is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param startDate The start date string to be validated.
	 * 
	 * @return Returns null the start date is null or whitespace only. 
	 * 		   Otherwise, the start date as a Date object is returned.
	 * 
	 * @throws ValidationException Thrown if the start date is not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateStartDate(Request request, String startDate) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(startDate);
		if(result == null) {
			result = StringUtils.decodeDate(startDate);
			
			if(result == null) {
				request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date is not a valid date: " + startDate);
				throw new ValidationException("The start date is not a valid date: " + startDate);
			}
			else {
				return result;
			}
		}
		else {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "Only a date is allowed, not time: " + startDate);
			throw new ValidationException("Only a date is allowed, not time: " + startDate);
		}
	}
	
	/**
	 * Validates that a end date string is a valid date and returns it. If it
	 * is not a valid date, the request is failed and an exception is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param endDate The end date string to be validated.
	 * 
	 * @return Returns null the end date is null or whitespace only. Otherwise, 
	 * 		   the end date as a Date object is returned.
	 * 
	 * @throws ValidationException Thrown if the end date is not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateEndDate(Request request, String endDate) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(endDate);
		if(result == null) {
			result = StringUtils.decodeDate(endDate);
			
			if(result == null) {
				request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The end date is not a valid date: " + endDate);
				throw new ValidationException("The end date is not a valid date: " + endDate);
			}
			else {
				return result;
			}
		}
		else {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "Only a date is allowed, not time: " + endDate);
			throw new ValidationException("Only a date is allowed, not time: " + endDate);
		}
	}
}