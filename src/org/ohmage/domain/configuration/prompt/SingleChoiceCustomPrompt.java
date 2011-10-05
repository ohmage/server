package org.ohmage.domain.configuration.prompt;

import java.util.Map;

import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.SingleChoiceCustomPromptResponse;

public class SingleChoiceCustomPrompt extends CustomChoicePrompt {
	private final String defaultValue;
	
	public SingleChoiceCustomPrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, ValueLabelPair> choices,
			final Map<Integer, ValueLabelPair> customChoices,
			final String defaultValue, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, customChoices, Type.SINGLE_CHOICE_CUSTOM, index);
		
		if(defaultValue != null) {
			if(! getAllChoices().values().contains(defaultValue)) {
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
	public SingleChoiceCustomPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new SingleChoiceCustomPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof String) {
			return new SingleChoiceCustomPromptResponse(this, null, repeatableSetIteration, (String) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or String value.");
	}
}
