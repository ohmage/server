package org.ohmage.domain.configuration.prompt;

import java.util.Map;

import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.SingleChoicePromptResponse;

public class SingleChoicePrompt extends ChoicePrompt {
	private final String defaultValue;
	
	public SingleChoicePrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, ValueLabelPair> choices, 
			final String defaultValue, final int index) {

		super(condition, id, unit, text, abbreviatedText, explanationText,
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
	
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean validateValue(Object value) {
		if(value instanceof String) {
			if(getChoices().values().contains((String) value)) {
				return true;
			}
		}

		return false;
	}
	
	@Override
	public SingleChoicePromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new SingleChoicePromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof String) {
			return new SingleChoicePromptResponse(this, null, repeatableSetIteration, (String) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or String value.");
	}
}