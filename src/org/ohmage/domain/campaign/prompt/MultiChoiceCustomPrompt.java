package org.ohmage.domain.campaign.prompt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.MultiChoiceCustomPromptResponse;
import org.ohmage.util.StringUtils;

/**
 * This class represents a multiple-choice prompt with custom choices provided
 * by the user.
 * 
 * @author John Jenkins
 */
public class MultiChoiceCustomPrompt extends CustomChoicePrompt {
	private static final String JSON_KEY_DEFAULT = "default";
	
	private final Collection<Integer> defaultValues;
	
	/**
	 * Creates a new multiple-choice prompt with custom choices.
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
	 * @param defaultValues The default value for this prompt. This is optional
	 * 						and may be null if one doesn't exist.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are missing or invalid. 
	 */
	public MultiChoiceCustomPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, LabelValuePair> choices,
			final Map<Integer, LabelValuePair> customChoices,
			final Collection<String> defaultValues, final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, customChoices, 
				Type.MULTI_CHOICE_CUSTOM, index);
		
		if(defaultValues == null) {
			this.defaultValues = new ArrayList<Integer>(0);
		}
		else {
			this.defaultValues = new HashSet<Integer>(defaultValues.size());
			Map<Integer, LabelValuePair> allChoices = getAllChoices();
			
			for(String defaultValue : defaultValues) {
				for(Integer currKey : allChoices.keySet()) {
					boolean found = false;
					
					if(allChoices.get(currKey).getLabel().equals(defaultValue)) {
						this.defaultValues.add(currKey);
						found = true;
						break;
					}
					
					if(! found) {
						throw new IllegalArgumentException("One of the default values is not a known choice.");
					}
				}
			}
		}
	}
	
	/**
	 * Returns the default values.
	 * 
	 * @return The default values, which may be empty.
	 */
	public final Collection<Integer> getDefaultValues() {
		return Collections.unmodifiableCollection(defaultValues);
	}

	/**
	 * Validates that some value is a valid response for this prompt.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A collection of label values.
	 * 
	 * @throws IllegalArgumentException Thrown if the value is not valid.
	 * 
	 * @throws NoResponseException Thrown if the value is or represents a
	 * 							   NoResponse object.
	 */
	@Override
	public Collection<String> validateValue(final Object value) throws NoResponseException {
		Collection<String> collectionValue = null;
		
		// If it's already a NoResponse value, then make sure that if it
		// was skipped that it is skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			throw new NoResponseException((NoResponse) value);
		}
		// If it's already a collection, first ensure that all of the elements
		// are strings.
		else if(value instanceof Collection<?>) {
			Collection<?> values = (Collection<?>) value;
			collectionValue = new HashSet<String>(values.size());
			
			for(Object currResponse : values) {
				if(currResponse instanceof String) {
					collectionValue.add((String) currResponse);
				}
				else {
					throw new IllegalArgumentException("An object in the list was not a valid label value.");
				}
			}
		}
		// If it's a JSONArray, parse it and get the items.
		else if(value instanceof JSONArray) {
			JSONArray responses = (JSONArray) value;
			int numResponses = responses.length();
			collectionValue = new HashSet<String>(numResponses);
			
			for(int i = 0; i < numResponses; i++) {
				try {
					collectionValue.add(responses.getString(i));
				}
				catch(JSONException notKey) {
					throw new IllegalArgumentException("The value was a JSONArray, but not all of the elements were strings.", notKey);
				}
			}
		}
		// If it's a sting, parse it to check if it's a NoResponse value and,
		// if not, parse it and generate a list of values.
		else if(value instanceof String) {
			String valueString = (String) value;
			
			try {
				throw new NoResponseException(NoResponse.valueOf(valueString));
			}
			catch(IllegalArgumentException notNoResponse) {
				try {
					JSONArray responses = new JSONArray(valueString);
					collectionValue = new HashSet<String>(responses.length());

					int numResponses = responses.length();
					for(int i = 0; i < numResponses; i++) {
						try {
							collectionValue.add(responses.getString(i));
						}
						catch(JSONException notString) {
							throw new IllegalArgumentException("One of the items in the list was not decodable as a string value.");
						}
					}
				}
				catch(JSONException notJsonArray) {
					String[] responses = valueString.split(",");
					collectionValue = new HashSet<String>(responses.length);
					
					for(int i = 0; i < responses.length; i++) {
						String currResponse = responses[i];
						
						if(! StringUtils.isEmptyOrWhitespaceOnly(currResponse)) {
							collectionValue.add(currResponse);
						}
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("The value is not decodable as a reponse value.");
		}
		
		Map<Integer, LabelValuePair> choices = getAllChoices();
		
		// Custom choice types are not required to 
		// have any pre-configured choices
		
		if(choices.isEmpty()) {
			
			int nextKey = 0;
			
			for(String labelValue : collectionValue) {
				addChoice(nextKey, labelValue, null);
				nextKey++;
			}
			
		}
		else {
			List<Integer> keys = new ArrayList<Integer>(choices.keySet());
			Collections.sort(keys);
			int nextKey = keys.get(keys.size() - 1) + 1;
			
			Collection<LabelValuePair> values = choices.values();
			
			for(String labelValue : collectionValue) {
				if(! values.contains(labelValue)) {
					addChoice(nextKey++, labelValue, null);
				}
			}
		}
		
		return collectionValue;
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
	public MultiChoiceCustomPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		try {
			return new MultiChoiceCustomPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					validateValue(response)
				);
		}
		catch(NoResponseException e) {
			return new MultiChoiceCustomPromptResponse(
					this, 
					e.getNoResponse(), 
					repeatableSetIteration, 
					null
				);
		}
	}
	
	/**
	 * Creates a JSONObject that represents this multi-choice custom prompt.
	 * 
	 * @return A JSONObject that represents this multi-choice custom prompt.
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
			
			result.put(JSON_KEY_DEFAULT, new JSONArray(defaultValues));
			
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
				+ ((defaultValues == null) ? 0 : defaultValues.hashCode());
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
		MultiChoiceCustomPrompt other = (MultiChoiceCustomPrompt) obj;
		if (defaultValues == null) {
			if (other.defaultValues != null)
				return false;
		} else if (!defaultValues.equals(other.defaultValues))
			return false;
		return true;
	}
}