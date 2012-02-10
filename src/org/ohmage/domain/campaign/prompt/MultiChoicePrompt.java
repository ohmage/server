package org.ohmage.domain.campaign.prompt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.MultiChoicePromptResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

/**
 * This class represents a multiple-choice prompt where the choices are all
 * static choices from the XML.
 * 
 * @author John Jenkins
 */
public class MultiChoicePrompt extends ChoicePrompt {
	private static final String JSON_KEY_DEFAULT = "default";
	
	private final Collection<Integer> defaultKeys;
	
	/**
	 * Creates a new multiple-choice prompt.
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
	 * @param defaultKeys The default keys for this prompt.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public MultiChoicePrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String abbreviatedText, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final DisplayType displayType, 
			final String displayLabel,
			final Map<Integer, LabelValuePair> choices, 
			final Collection<Integer> defaultKeys, 
			final int index) 
			throws DomainException {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, Type.MULTI_CHOICE, index);
		
		if(defaultKeys != null) {
			Collection<Integer> availableKeys = getChoices().keySet();
			for(Integer defaultKey : defaultKeys) {
				if(! availableKeys.contains(defaultKey)) {
					throw new DomainException(
							"The default key does not exist.");
				}
			}
		}
		this.defaultKeys = defaultKeys;
	}
	
	/**
	 * Returns the default values.
	 * 
	 * @return The default values, which may be empty.
	 */
	public final Collection<String> getDefaultValues() {
		Map<Integer, LabelValuePair> choices = getChoices();
		Collection<String> result = 
				new ArrayList<String>(defaultKeys.size());
		
		for(Integer key : defaultKeys) {
			result.add(choices.get(key).getLabel());
		}
		
		return result;
	}

	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated. This must be one of the  
	 * 				following:<br />
	 * 				<ul>
	 * 				<li>{@link NoResponse}</li>
	 * 				<li>{@link Integer}. This will be converted into a list of
	 * 				  one items that contains this integer. The {@link Integer}
	 * 				  should be the key of the item that the user chose.</li>
	 * 				<li>A {@link Collection} of {@link Integer}s where each
	 * 				  {@link Integer} is a key to a unique item the user chose.
	 * 				  </li>
	 * 				<li>A {@link JSONArray} of {@link Integer}s where each
	 * 				  {@link Integer} is a key to a unique item the user chose.
	 * 				  </li>
	 * 				<li>{@link String} that represents:</li>
	 * 				  <ul>
	 * 				    <li>{@link NoResponse}</li>
	 * 				    <li>A {@link JSONArray} of {@link Integer}s where each
	 * 				      {@link Integer} is a key to a unique item the user 
	 * 				      chose.</li>
	 * 				    <li>A comma-separated list of {@link Integer}s where 
	 * 				      each {@link Integer} is a key to a unique item the 
	 * 				      user chose.</li>
	 * 				  <ul>
	 * 				</ul>
	 * 
	 * @return A {@link Collection} of {@link Integer}s or a {@link NoResponse}
	 * 		   object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		Collection<Integer> collectionValue = null;
		Map<Integer, LabelValuePair> choices = getChoices();
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already an integer, add it as the only result item.
		else if(value instanceof Integer) {
			collectionValue = new ArrayList<Integer>(1);
			collectionValue.add((Integer) value);
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
				else {
					throw new DomainException(
							"The value was a collection, but not all of the items were integers.");
				}
			}
		}
		// If it's a JSONArray, parse it and get the items.
		else if(value instanceof JSONArray) {
			JSONArray responses = (JSONArray) value;
			int numResponses = responses.length();
			collectionValue = new HashSet<Integer>(numResponses);
			
			for(int i = 0; i < numResponses; i++) {
				try {
					collectionValue.add(responses.getInt(i));
				}
				catch(JSONException notKey) {
					throw new DomainException(
							"The value was a JSONArray, but not all of the elements were integers.", 
							notKey);
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
						try {
							collectionValue.add(responses.getInt(i));
						}
						catch(JSONException notKey) {
							throw new DomainException(
									"The value was a JSONArray, but not all fo the elements were integers.", 
									notKey);
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
								throw new DomainException(
										"The value was a comma-separated list, but not all of the elemtns were integers.", 
										notKey);
							}
						}
					}
				}
			}
		}
		else {
			throw new DomainException(
					"The value is not decodable as a reponse value: " + 
						value.toString());
		}
		
		for(Integer key : collectionValue) {
			if(! choices.containsKey(key)) {
				throw new DomainException(
						"A key was given that isn't a known choice.");
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
	 * @throws DomainException Thrown if this prompt is part of a repeatable 
	 * 						   set but the repeatable set iteration value is 
	 * 						   null, if the repeatable set iteration value is 
	 * 						   negative, or if the value is not a valid 
	 * 						   response value for this prompt.
	 */
	@Override
	public MultiChoicePromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new MultiChoicePromptResponse(
				this,
				repeatableSetIteration,
				response);
	}
	
	/**
	 * Creates a JSONObject that represents this multi-choice prompt.
	 * 
	 * @return A JSONObject that represents this multi-choice prompt.
	 * 
	 * @throws JSONException There was a problem creating a JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_DEFAULT, new JSONArray(defaultKeys));
		
		return result;
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
				+ ((defaultKeys == null) ? 0 : defaultKeys.hashCode());
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
		MultiChoicePrompt other = (MultiChoicePrompt) obj;
		if (defaultKeys == null) {
			if (other.defaultKeys != null)
				return false;
		} else if (!defaultKeys.equals(other.defaultKeys))
			return false;
		return true;
	}
}