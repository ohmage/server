package org.ohmage.domain.campaign.prompt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private final Collection<String> defaultValues;
	
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
		
		if(defaultValues != null) {
			Collection<LabelValuePair> allValues = getAllChoices().values();
			
			Set<String> tempDefaultValues = new HashSet<String>(defaultValues);
			tempDefaultValues.removeAll(allValues);
			if(tempDefaultValues.size() != 0) {
				throw new IllegalArgumentException(
						"The following default values are not valid choices: " + 
						tempDefaultValues);
			}
		}
		this.defaultValues = defaultValues;
	}
	
	/**
	 * Returns the default values if they exist or null if they do not.
	 * 
	 * @return The default values if they exist or null if they do not.
	 */
	public final Collection<String> getDefaultValues() {
		return Collections.unmodifiableCollection(defaultValues);
	}

	/**
	 * Validates that an Object is a valid response value for this prompt. This
	 * includes a {@link NoResponse} value as either a {@link NoResponse} 
	 * object or as a string representing one, a Collection object of String
	 * objects, or a comma-separated String object with or without braces.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A {@link NoResponse} object or a Collection of Integer.
	 * 
	 * @throws IllegalArgumentException Thrown if the value is not valid.
	 */
	@Override
	public Object validateValue(final Object value) {
		Collection<Integer> collectionValue = null;
		Map<Integer, LabelValuePair> choices = getAllChoices();
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already a collection, first ensure that all of the elements
		// are integers.
		else if(value instanceof Collection<?>) {
			Collection<?> values = (Collection<?>) value;
			collectionValue = new HashSet<Integer>(values.size());
			
			for(Object currResponse : values) {
				if(currResponse instanceof Integer) {
					collectionValue.add((Integer) currResponse);
				}
				else if(currResponse instanceof String) {
					for(Integer key : choices.keySet()) {
						if(choices.get(key).getLabel().equals(currResponse)) {
							collectionValue.add(key);
							break;
						}
					}
				}
				else {
					throw new IllegalArgumentException("One of the values in the collection was not a String value.");
				}
			}
		}
		// If it's a sting, parse it to check if it's a NoResponse value and,
		// if not, parse it and generate a list of values.
		else if(value instanceof String) {
			String valueString = (String) value;
			
			try {
				return NoResponse.valueOf(valueString);
			}
			catch(IllegalArgumentException notNoResponse) {
				collectionValue = new HashSet<Integer>();

				try {
					JSONArray responses = new JSONArray(valueString);
					
					int numResponses = responses.length();
					for(int i = 0; i < numResponses; i++) {
						String responseLabel = responses.getString(i);
						
						try {
							collectionValue.add(getChoiceKey(responseLabel));
						}
						catch(IllegalArgumentException e) {
							List<Integer> keyset = new ArrayList<Integer>(getAllChoices().keySet());
							Collections.sort(keyset);
							
							addChoice(keyset.get(keyset.size() - 1) + 1, responseLabel, null);
						}
					}
				}
				catch(JSONException notJsonArray) {
					String[] responses = valueString.split(",");
					
					collectionValue = new HashSet<Integer>(responses.length);
					for(int i = 0; i < responses.length; i++) {
						String currResponse = responses[i];
						
						if(StringUtils.isEmptyOrWhitespaceOnly(currResponse)) {
							try {
								collectionValue.add(Integer.decode(currResponse));
							}
							catch(NumberFormatException notKey) {
								boolean found = false;
								
								for(Integer key : choices.keySet()) {
									if(choices.get(key).getLabel().equals(currResponse)) {
										collectionValue.add(key);
										found = true;
										break;
									}
								}
								
								if(! found) {
									throw new IllegalArgumentException("The response value could not be decoded as a response value.");
								}
							}
						}
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("The value is not decodable as a reponse value.");
		}
		
		for(Integer key : collectionValue) {
			if(! choices.containsKey(key)) {
				throw new IllegalArgumentException("A key was given that isn't a known choice.");
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
	@SuppressWarnings("unchecked")
	@Override
	public MultiChoiceCustomPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		Object validatedResponse = validateValue(response);
		if(validatedResponse instanceof NoResponse) {
			return new MultiChoiceCustomPromptResponse(
					this, 
					(NoResponse) validatedResponse, 
					repeatableSetIteration, 
					null,
					false
				);
		}
		else if(validatedResponse instanceof Collection<?>) {
			return new MultiChoiceCustomPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					(Collection<String>) validatedResponse,
					false
				);
		}
			
		throw new IllegalArgumentException("The response was not a valid response.");
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