package org.ohmage.domain.campaign.prompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.SingleChoiceCustomPromptResponse;

/**
 * This class represents a single choice prompt with custom choices from the
 * user.
 * 
 * @author John Jenkins
 */
public class SingleChoiceCustomPrompt extends CustomChoicePrompt {
	private static final String JSON_KEY_DEFAULT = "default";
	
	private final Integer defaultKey;
	
	/**
	 * Creates a new single-choice prompt with custom values.
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
	 * @param customChoices Custom choices created by the user.
	 * 
	 * @param defaultKey The key of the default label.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing or invalid. 
	 */
	public SingleChoiceCustomPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, LabelValuePair> choices,
			final Map<Integer, LabelValuePair> customChoices,
			final Integer defaultKey, final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, customChoices, Type.SINGLE_CHOICE_CUSTOM, index);
		
		if((defaultKey != null) &&
				(! getAllChoices().containsKey(defaultKey))) {
			throw new IllegalArgumentException("The default key does not exist.");
		}
		this.defaultKey = defaultKey;
	}
	
	/**
	 * Returns the default value if one was given; otherwise, null is returned.
	 * 
	 * @return The default value if one was given; otherwise, null is returned.
	 */
	public String getDefaultValue() {
		return getAllChoices().get(defaultKey).getLabel();
	}

	/**
	 * Validates that an Object is a valid response value. This must be one of
	 * the following:<br />
	 * <li>A {@link NoResponse} object.</li>
	 * <li>A String representing a {@link NoResponse} value.</li>
	 * <li>An Integer key value.</li>
	 * <li>A String representing a key value.</li>
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A {@link NoResponse} object or an Integer.
	 * 
	 * @throws IllegalArgumentException Thrown if the value is not valid.
	 */
	@Override
	public String validateValue(final Object value) throws NoResponseException {
		// If it's already a NoResponse value, then make sure that if it
		// was skipped that it is skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			throw new NoResponseException((NoResponse) value);
		}
		else if(value instanceof String) {
			try {
				throw new NoResponseException(NoResponse.valueOf((String) value));
			}
			catch(IllegalArgumentException notNoResponse) {
				Map<Integer, LabelValuePair> choices = getAllChoices();
				
				if(choices.isEmpty()) {
					addChoice(0, (String) value, null);
				} 
				else {
					if(! choices.values().contains(value)) {
						List<Integer> keys = new ArrayList<Integer>(choices.keySet());
						Collections.sort(keys);
						int key = keys.get(keys.size() - 1) + 1;
						addChoice(key, (String) value, null);
					}
				}
				
				return (String) value;
			}
		}
		else {
			throw new IllegalArgumentException("The value is not decodable as a reponse value.");
		}
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
	public SingleChoiceCustomPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		try {
			return new SingleChoiceCustomPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					validateValue(response)
				);
		}
		catch(NoResponseException e) {
			return new SingleChoiceCustomPromptResponse(
					this, 
					e.getNoResponse(), 
					repeatableSetIteration, 
					null
				);
		}
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
			
			result.put(JSON_KEY_DEFAULT, defaultKey);
			
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
				+ ((defaultKey == null) ? 0 : defaultKey.hashCode());
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
		SingleChoiceCustomPrompt other = (SingleChoiceCustomPrompt) obj;
		if (defaultKey == null) {
			if (other.defaultKey != null)
				return false;
		} else if (!defaultKey.equals(other.defaultKey))
			return false;
		return true;
	}
}