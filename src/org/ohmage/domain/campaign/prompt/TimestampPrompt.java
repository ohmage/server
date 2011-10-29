package org.ohmage.domain.campaign.prompt;

import java.util.Calendar;
import java.util.Date;

import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.TimestampPromptResponse;
import org.ohmage.util.StringUtils;

/**
 * This class represents a timestamp prompt.
 * 
 * @author John Jenkins
 */
public class TimestampPrompt extends Prompt {
	/**
	 * Creates a timestamp prompt.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param unit The unit value for this prompt.
	 * 
	 * @param text The text to be displayed to the user for this prompt.
	 * 
	 * @param abbreviatedText An abbreviated version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayType This prompt's
	 * 					 {@link org.ohmage.domain.campaign.Prompt.DisplayType}.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing or invalid. 
	 */
	public TimestampPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.TIMESTAMP, index);
	}
	
	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A String value if it is a valid response or a {@link NoResponse}
	 * 		   object value if it is a valid {@link NoResponse} value.
	 * 
	 * @throws IllegalArgumentException Thrown if the value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) {
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already a date, return it.
		else if(value instanceof Date) {
			return value;
		}
		// If it's a Calendar, convert it to a Date and return it.
		else if(value instanceof Calendar) {
			return new Date(((Calendar) value).getTimeInMillis());
		}
		// If it's a String, attempt to convert it to a Date and return it.
		else if(value instanceof String) {
			Date result = null;
			
			try {
				return NoResponse.valueOf((String) value);
			}
			catch(IllegalArgumentException iae) {
				result = StringUtils.decodeDateTime((String) value);
				if(result != null) {
					return result;
				}
				
				result = StringUtils.decodeDate((String) value);
				if(result != null) {
					return result;
				}
			
				throw new IllegalArgumentException("The string value could not be converted to a date.");
			}
		}
		
		throw new IllegalArgumentException("The value could not be converted to a valid Date.");
	}
	
	/**
	 * Creates a response to this prompt based on a response value.
	 * 
	 * @param response The response from the user as an Object.
	 * 
	 * @param repeatableSetIteration If this prompt belongs to a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which the response to
	 * 								 this prompt was made.
	 * 
	 * @throws IllegalArgumentException Thrown if this prompt is part of a
	 * 									repeatable set but the repeatable set
	 * 									iteration value is null, if the
	 * 									repeatable set iteration value is 
	 * 									negative, or if the value is not a 
	 * 									valid response value for this prompt.
	 */
	@Override
	public TimestampPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		Object validatedResponse = validateValue(response);
		if(validatedResponse instanceof NoResponse) {
			return new TimestampPromptResponse(
					this, 
					(NoResponse) validatedResponse, 
					repeatableSetIteration, 
					null,
					false
				);
		}
		else if(validatedResponse instanceof Date) {
			return new TimestampPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					(Date) validatedResponse,
					false
				);
		}
			
		throw new IllegalArgumentException("The response was not a valid response.");
	}
}