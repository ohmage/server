package org.ohmage.domain.configuration.prompt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CustomChoicePrompt extends ChoicePrompt {
	/**
	 */
	private final Map<Integer, ValueLabelPair> customChoices;
	
	public CustomChoicePrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, ValueLabelPair> choices,
			final Map<Integer, ValueLabelPair> customChoices, 
			final Type type, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, type, index);
		
		this.customChoices = new HashMap<Integer, ValueLabelPair>(customChoices);
	}
	
	public Map<Integer, ValueLabelPair> getCustomChoices() {
		return Collections.unmodifiableMap(customChoices);
	}
	
	public Map<Integer, ValueLabelPair> getAllChoices() {
		Map<Integer, ValueLabelPair> combinedMap = new HashMap<Integer, ValueLabelPair>(customChoices);
		combinedMap.putAll(getChoices());
		
		return Collections.unmodifiableMap(combinedMap);
	}
	
	public void addChoice(Integer key, Number value, String label) {
		if(key == null) {
			throw new IllegalArgumentException("The key cannot be null.");
		}
		else if(key < 0) {
			throw new IllegalArgumentException("The key cannot be negative.");
		}
		else if(label == null) {
			throw new IllegalArgumentException("The value cannot be null.");
		}
		
		customChoices.put(key, new ValueLabelPair(label, value));
	}
	
	public void removeChoice(Integer key) {
		if(key == null) {
			throw new IllegalArgumentException("The key cannot be null.");
		}
		else if(key < 0) {
			throw new IllegalArgumentException("The key cannot be negative.");
		}
		
		customChoices.remove(key);
	}

	@Override
	public boolean validateValue(Object value) {
		if(super.validateValue(value)) {
			return true;
		}
		else if(value instanceof String) {
			if(customChoices.values().contains((String) value)) {
				return true;
			}
		}

		return false;
	}
	
	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = super.toJson();
			
			JSONObject choiceGlossary = result.getJSONObject(JSON_KEY_CHOICE_GLOSSARY);
			for(Integer key : customChoices.keySet()) {
				choiceGlossary.put(key.toString(), customChoices.get(key));
			}
			result.put(JSON_KEY_CHOICE_GLOSSARY, choiceGlossary);
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((customChoices == null) ? 0 : customChoices.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomChoicePrompt other = (CustomChoicePrompt) obj;
		if (customChoices == null) {
			if (other.customChoices != null)
				return false;
		} else if (!customChoices.equals(other.customChoices))
			return false;
		return true;
	}
}