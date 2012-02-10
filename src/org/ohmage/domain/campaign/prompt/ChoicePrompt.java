package org.ohmage.domain.campaign.prompt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.exception.DomainException;

/**
 * This class represents prompts that have a set of choices.
 * 
 * @author John Jenkins
 */
public abstract class ChoicePrompt extends Prompt {
	/**
	 * The key to use when exporting this to JSON to indicate the list of 
	 * choices.
	 */
	public static final String JSON_KEY_CHOICE_GLOSSARY = "choice_glossary";
	
	private final Map<Integer, LabelValuePair> choices;
	private final boolean hasValues;

	/**
	 * Creates a new choice prompt.
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
	 * @param choices A map of choices from their integer key value to their
	 * 				  label-value pairs.
	 * 
	 * @param type This prompt's 
	 * 			   {@link org.ohmage.domain.campaign.Prompt.Type}.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public ChoicePrompt(
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
			final Type type,
			final int index) 
			throws DomainException {

		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				type, index);
		
		this.choices = new HashMap<Integer, LabelValuePair>(choices);
		
		boolean tHasValues = false;
		for(LabelValuePair lvp : choices.values()) {
			if(lvp.getValue() != null) {
				tHasValues = true;
				break;
			}
		}
		hasValues = tHasValues;
	}
	
	/**
	 * Returns an unmodifiable map of the choices.
	 * 
	 * @return An unmodifiable map of the choices where the key is the integer
	 * 		   key for the choice and the value is a label-value pair.
	 */
	public Map<Integer, LabelValuePair> getChoices() {
		return Collections.unmodifiableMap(choices);
	}
	
	/**
	 * Returns the key for some label.
	 * 
	 * @param label The label.
	 * 
	 * @return The key value.
	 * 
	 * @throws DomainException If no such key for the given label exists.
	 */
	public Integer getChoiceKey(final String label) throws DomainException {
		Map<Integer, LabelValuePair> choices = getChoices();
		for(Integer key : choices.keySet()) {
			if(choices.get(key).getLabel().equals(label)) {
				return key;
			}
		}
		
		throw new DomainException("No such key for label: " + label);
	}
	
	/**
	 * Returns whether any of the choices have values associated with them.
	 * 
	 * @return Whether any of the choices have values associated with them.
	 */
	public boolean hasValues() {
		return hasValues;
	}
	
	/**
	 * Creates a JSONObject that represents this choice prompt.
	 * 
	 * @return A JSONObject that represents this choice prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		JSONObject choiceGlossary = new JSONObject();
		for(Integer key : choices.keySet()) {
			choiceGlossary.put(key.toString(), choices.get(key).toJson());
		}
		result.put(JSON_KEY_CHOICE_GLOSSARY, choiceGlossary);
		
		return result;
	}

	/**
	 * Generates a hash code for this choice prompt.
	 * 
	 * @return A hash code for this choice prompt.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((choices == null) ? 0 : choices.hashCode());
		return result;
	}

	/**
	 * Determines if this choice prompt is logically equivalent to some object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the other object and this choice prompt are logically
	 * 		   equivalent; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChoicePrompt other = (ChoicePrompt) obj;
		if (choices == null) {
			if (other.choices != null)
				return false;
		} else if (!choices.equals(other.choices))
			return false;
		return true;
	}
}