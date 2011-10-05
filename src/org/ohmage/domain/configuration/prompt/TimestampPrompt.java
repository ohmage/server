package org.ohmage.domain.configuration.prompt;

import java.util.Calendar;
import java.util.Date;

import org.ohmage.domain.configuration.Prompt;
import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.TimestampPromptResponse;
import org.ohmage.util.StringUtils;

public class TimestampPrompt extends Prompt {
	public TimestampPrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.TIMESTAMP, index);
	}
	
	@Override
	public boolean validateValue(Object value) {
		if(value instanceof String) {
			if(StringUtils.decodeDateTime((String) value) != null) {
				return true;
			}
			else if(StringUtils.decodeDate((String) value) != null) {
				return true;
			}
		}
		else if(value instanceof Date) {
			return true;
		}
		else if(value instanceof Calendar) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public TimestampPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new TimestampPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof Date) {
			return new TimestampPromptResponse(this, null, repeatableSetIteration, (Date) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or Date value.");
	}
}