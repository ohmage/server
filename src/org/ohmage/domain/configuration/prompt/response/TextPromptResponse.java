package org.ohmage.domain.configuration.prompt.response;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.TextPrompt;

/**
 * A text prompt response.
 * 
 * @author John Jenkins
 */
public class TextPromptResponse extends PromptResponse {
	/**
	 */
	private final String text;

	public TextPromptResponse(
			final TextPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final String text) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((text == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both text and no response cannot be null.");
		}
		else if((text != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both text and no response were given.");
		}
		else if((text != null) && (! prompt.validateValue(text))) {
			throw new IllegalArgumentException("The text response is invalid.");
		}
		else {
			this.text = text;
		}
	}
	
	/**
	 * Returns the text.
	 * @return  The text. This may be null if no response was given.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the text.
	 * 
	 * @return The text.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return text;
		}
		else {
			return noResponseString;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		TextPromptResponse other = (TextPromptResponse) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}