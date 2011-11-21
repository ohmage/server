package org.ohmage.domain.campaign.prompt;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.TextPromptResponse;

/**
 * This class represents a text prompt.
 * 
 * @author John Jenkins
 */
public class TextPrompt extends Prompt {
	private static final String JSON_KEY_LOWER_BOUND = "min";
	private static final String JSON_KEY_UPPER_BOUND = "max";
	private static final String JSON_KEY_DEFAULT = "default";
	
	/**
	 * The campaign configuration property key for the lower bound.
	 */
	public static final String XML_KEY_MIN = "min";
	/**
	 * The campaign configuration property key for the upper bound.
	 */
	public static final String XML_KEY_MAX = "max";
	
	private final long min;
	private final long max;
	
	private final String defaultValue;
	
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
			final long min, final long max, final String defaultValue,
			final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.TEXT, index);
		
		this.min = min;
		this.max = max;
		this.defaultValue = defaultValue;
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
	 * Returns the lower bound for a response to this prompt.
	 * 
	 * @return The lower bound for a response to this prompt.
	 */
	public long getMin() {
		return min;
	}
	
	/**
	 * Returns the upper bound for a response to this prompt.
	 * 
	 * @return The upper bound for a response to this prompt.
	 */
	public long getMax() {
		return max;
	}
	
	/**
	 * Returns the default value for a response to this prompt.
	 * 
	 * @return The default value for a response to this prompt.
	 */
	public String getDefault() {
		return defaultValue;
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
		
		Object responseObject = validateValue(response);
		if(responseObject instanceof NoResponse) {
			return new TextPromptResponse(
					this, 
					(NoResponse) responseObject, 
					repeatableSetIteration, 
					null
				);
		}
		else if(responseObject instanceof String) {
			return new TextPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					(String) responseObject
				);
		}
		else {
			throw new IllegalStateException("The validation no longer returns the expected object type.");
		}
	}
	
	/**
	 * Creates a JSONObject that represents this bounded prompt.
	 * 
	 * @return A JSONObject that represents this bounded prompt.
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
			
			result.put(JSON_KEY_LOWER_BOUND, min);
			result.put(JSON_KEY_UPPER_BOUND, max);
			result.put(JSON_KEY_DEFAULT, defaultValue);
			
			return result;
		}
		catch(JSONException e) {
			// FIXME: Throw an exception.
			return null;
		}
	}

	/**
	 * Returns a hash code representing this prompt.
	 * 
	 * @return A hash code representing this prompt.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (max ^ (max >>> 32));
		result = prime * result + (int) (min ^ (min >>> 32));
		result = prime * result + defaultValue.hashCode();
		return result;
	}

	/**
	 * Determines if this bounded prompt and another object are logically 
	 * equal.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the object is logically equivalent to this bounded 
	 * 		   prompt; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextPrompt other = (TextPrompt) obj;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		if (! defaultValue.equals(other.defaultValue))
			return false;
		return true;
	}
}