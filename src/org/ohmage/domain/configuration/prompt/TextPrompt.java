package org.ohmage.domain.configuration.prompt;

import org.ohmage.domain.configuration.Prompt;
import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.TextPromptResponse;

public class TextPrompt extends Prompt {
	/**
	 */
	private final long min;
	/**
	 */
	private final long max;
	
	public TextPrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final long min, final long max, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.TEXT, index);
		
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean validateValue(Object value) {
		if(value == null) {
			return false;
		}
		
		if(value instanceof String) {
			long length = ((String) value).length();
			if((length < min) || (length > max)) {
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public TextPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new TextPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof String) {
			return new TextPromptResponse(this, null, repeatableSetIteration, (String) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or String value.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (max ^ (max >>> 32));
		result = prime * result + (int) (min ^ (min >>> 32));
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
		TextPrompt other = (TextPrompt) obj;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}
}