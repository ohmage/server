package org.ohmage.domain.campaign.prompt;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.SingleChoicePromptResponse;

/**
 * This class represents a single choice prompt.
 * 
 * @author John Jenkins
 */
public class SingleChoicePrompt extends ChoicePrompt {
	private static final String JSON_KEY_DEFAULT = "default";
	
	private final String defaultValue;
	
	/**
	 * Creates a new single-choice prompt.
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
	 * @param choices The static choices as defined in the XML.
	 * 
	 * @param defaultValue The default value for this prompt. This is optional
	 * 					   and may be null if one doesn't exist.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing or invalid. 
	 */
	public SingleChoicePrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, LabelValuePair> choices, 
			final String defaultValue, final int index) {

		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, Type.SINGLE_CHOICE, index);
		
		if(defaultValue != null) {
			if(! getChoices().values().contains(defaultValue)) {
				throw new IllegalArgumentException(
						"The default value is not a valid choice.");
			}
		}
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Returns the default value if one was given; otherwise, null is returned.
	 * 
	 * @return The default value if one was given; otherwise, null is returned.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Validates that an Object is a valid response value for this prompt. This
	 * includes a {@link NoResponse} value as either a {@link NoResponse} 
	 * object or as a string representing one or a String object representing a
	 * response from the user.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A {@link NoResponse} object or a String.
	 * 
	 * @throws IllegalArgumentException Thrown if the value is not valid.
	 */
	@Override
	public Object validateValue(final Object value) {
		String choiceValue;
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		else if(value instanceof String) {
			choiceValue = (String) value;
			
			try {
				return NoResponse.valueOf(choiceValue);
			}
			catch(IllegalArgumentException e) {
				// Then, the user must be attempting to respond with a value.
			}
		}
		else {
			throw new IllegalArgumentException("The value is not decodable as a reponse value.");
		}

		if(! getChoices().values().contains(choiceValue)) {
			throw new IllegalArgumentException("The value is not a value choice: " + choiceValue);
		}
		
		return choiceValue;
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
	public SingleChoicePromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		Object validatedResponse = validateValue(response);
		if(validatedResponse instanceof NoResponse) {
			return new SingleChoicePromptResponse(
					this, 
					(NoResponse) validatedResponse, 
					repeatableSetIteration, 
					null,
					false
				);
		}
		else if(validatedResponse instanceof String) {
			return new SingleChoicePromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					(String) validatedResponse,
					false
				);
		}
			
		throw new IllegalArgumentException("The response was not a valid response.");
	}
	
	/**
	 * Creates a JSONObject that represents this single-choice custom prompt.
	 * 
	 * @return A JSONObject that represents this single-choice custom prompt.
	 */
	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = super.toJson();
			
			if(result == null) {
				// FIXME: Ignore the exception thrown, allowing it to 
				// propagate.
				return null;
			}
			
			result.put(JSON_KEY_DEFAULT, defaultValue);
			
			return result;
		}
		catch(JSONException e) {
			// FIXME: Throw an exception.
			return null;
		}
	}

	/**
	 * Generates a hash code for this prompt.
	 * 
	 * @return A hash code for this prompt.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
		return result;
	}

	/**
	 * Determines if this prompt is logically equivalent to another prompt.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the other object is logically equivalent to this
	 * 		   prompt.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleChoicePrompt other = (SingleChoicePrompt) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		return true;
	}
}