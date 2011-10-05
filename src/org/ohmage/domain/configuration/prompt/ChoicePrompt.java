package org.ohmage.domain.configuration.prompt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;

public abstract class ChoicePrompt extends Prompt {
	public static final String JSON_KEY_CHOICE_GLOSSARY = "choice_glossary";
	
	/**
	 */
	private final Map<Integer, ValueLabelPair> choices;

	public ChoicePrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, ValueLabelPair> choices, final Type type,
			final int index) {

		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				type, index);
		
		this.choices = new HashMap<Integer, ValueLabelPair>(choices);
	}
	
	public Map<Integer, ValueLabelPair> getChoices() {
		return Collections.unmodifiableMap(choices);
	}
	
	@Override
	public boolean validateValue(Object value) {
		if(value instanceof String) {
			if(choices.values().contains((String) value)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = super.toJson();
			
			result.put(JSON_KEY_CHOICE_GLOSSARY, choices);
			
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
		result = prime * result + ((choices == null) ? 0 : choices.hashCode());
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
		ChoicePrompt other = (ChoicePrompt) obj;
		if (choices == null) {
			if (other.choices != null)
				return false;
		} else if (!choices.equals(other.choices))
			return false;
		return true;
	}
}