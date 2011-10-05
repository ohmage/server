package org.ohmage.domain.configuration.prompt;

import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.HoursBeforeNowPromptResponse;


public class HoursBeforeNowPrompt extends BoundedPrompt {
	public HoursBeforeNowPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final long min, final long max, final Long defaultValue, 
			final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				min, max, defaultValue, Type.HOURS_BEFORE_NOW, index);
	}
	
	@Override
	public HoursBeforeNowPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if(response instanceof NoResponse) {
			return new HoursBeforeNowPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof Long) {
			return new HoursBeforeNowPromptResponse(this, null, repeatableSetIteration, (Long) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or Long value.");
	}
}