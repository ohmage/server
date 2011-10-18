package org.ohmage.domain.campaign.prompt;

import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.TextPromptResponse;

/**
 * This class represents a text prompt.
 * 
 * @author John Jenkins
 */
public class TextPrompt extends BoundedPrompt {
	/**
	 * Creates a new text prompt.
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
	 * @param min The lower bound for the length of a response to this prompt.
	 * 
	 * @param max The upper bound for the length of a response to this prompt.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing or invalid. 
	 */
	public TextPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final long min, final long max, final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				min, max, null, Type.TEXT, index);
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
		String valueString;
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it is a string, parse it to check if it's a NoResponse value and,
		// if not, parse it as a string.
		else if(value instanceof String) {
			valueString = (String) value;
			
			try {
				return NoResponse.valueOf(valueString);
			}
			catch(IllegalArgumentException iae) {
				// It must be the response.
			}
		}
		// Finally, if its type is unknown, throw an exception.
		else {
			throw new IllegalArgumentException("The value is not decodable as a response value.");
		}
		
		// Ensure that the length falls within the bounds.
		long length = ((String) value).length();
		if(length < getMin()) {
			throw new IllegalArgumentException("The value is too short.");
		}
		else if(length > getMax()) {
			throw new IllegalArgumentException("The value is too long.");
		}
		
		return valueString;
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
	public TextPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		Object validatedResponse = validateValue(response);
		if(validatedResponse instanceof NoResponse) {
			return new TextPromptResponse(
					this, 
					(NoResponse) validatedResponse, 
					repeatableSetIteration, 
					null,
					false
				);
		}
		else if(validatedResponse instanceof String) {
			return new TextPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					(String) validatedResponse,
					false
				);
		}
			
		throw new IllegalArgumentException("The response was not a valid response.");
	}
}