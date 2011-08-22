package org.ohmage.validator;

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
}
