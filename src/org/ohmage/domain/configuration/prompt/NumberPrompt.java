package org.ohmage.domain.configuration.prompt;

import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.NumberPromptResponse;

public class NumberPrompt extends BoundedPrompt {
	public NumberPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final long min, final long max, final Long defaultValue,
			final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel,
				min, max, defaultValue, Type.NUMBER, index);
	}
	
	@Override
	public NumberPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new NumberPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof Integer) {
			return new NumberPromptResponse(this, null, repeatableSetIteration, (Integer) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or Integer value.");
	}
}