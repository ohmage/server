package org.ohmage.domain.campaign.prompt;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.Response.NoResponse;

/**
 * This class represents prompts with numeric bounds. This includes but is not
 * limited to numeric prompts where the numeric response must be between two
 * numbers as well as text prompts where the number of characters that make up
 * the result must be between two numbers.
 * 
 * @author John Jenkins
 */
public abstract class BoundedPrompt extends Prompt {
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
	
	private final Long defaultValue;
	
	/**
	 * Creates a new bounded prompt.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
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
	 * @param min The lower bound for a response to this prompt.
	 * 
	 * @param max The upper bound for a response to this prompt.
	 * 
	 * @param defaultValue The default value for this prompt. This is optional
	 * 					   and may be null if one doesn't exist.
	 * 
	 * @param type This prompt's 
	 * 			   {@link org.ohmage.domain.campaign.Prompt.Type}.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing or invalid. 
	 */
	public BoundedPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final long min, final long max, final Long defaultValue,
			final Type type, final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				type, index);
		
		this.min = min;
		this.max = max;
		
		if(defaultValue != null) {
			if(defaultValue < min) {
				throw new IllegalArgumentException(
						"The default value is less than the minimum value.");
			}
			else if(defaultValue > max) {
				throw new IllegalArgumentException(
						"The default value is greater than hte maximum value.");
			}
		}
		this.defaultValue = defaultValue;
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
	 * Returns the default value.
	 * 
	 * @return The default value. This may be null if none was given.
	 */
	public Long getDefault() {
		return defaultValue;
	}

	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A Long value if it is a valid response or a {@link NoResponse}
	 * 		   object value if it is a valid {@link NoResponse} value.
	 * 
	 * @throws IllegalArgumentException Thrown if the value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) {
		long longValue;
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already a number, first ensure that it is an integer and not
		// a floating point number.
		else if(value instanceof Number) {
			if((value instanceof AtomicInteger) ||
					(value instanceof AtomicLong) ||
					(value instanceof BigInteger) ||
					(value instanceof Integer) ||
					(value instanceof Long) ||
					(value instanceof Short)) {
				
				longValue = ((Number) value).longValue();
			}
			else {
				throw new IllegalArgumentException("Only whole number are allowed.");
			}
		}
		// If it is a string, parse it to check if it's a NoResponse value and,
		// if not, parse it as a long. If that does not work either, throw an
		// exception.
		else if(value instanceof String) {
			String stringValue = (String) value;
			
			try {
				return NoResponse.valueOf(stringValue);
			}
			catch(IllegalArgumentException iae) {
				try {
					longValue = Long.decode(stringValue);
				}
				catch(NumberFormatException nfe) {
					throw new IllegalArgumentException("The value is not a valid number.");
				}
			}
		}
		// Finally, if its type is unknown, throw an exception.
		else {
			throw new IllegalArgumentException("The value is not decodable as a response value.");
		}
		
		// Now that we have a Long value, verify that it is within bounds.
		if(longValue < min) {
			throw new IllegalArgumentException("The value is less than the lower bound.");
		}
		else if(longValue > max) {
			throw new IllegalArgumentException("The value is greater than the lower bound.");
		}
		
		return longValue;
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
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + (int) (max ^ (max >>> 32));
		result = prime * result + (int) (min ^ (min >>> 32));
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
		BoundedPrompt other = (BoundedPrompt) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}
}