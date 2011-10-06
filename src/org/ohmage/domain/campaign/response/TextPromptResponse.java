package org.ohmage.domain.campaign.response;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.TextPrompt;

/**
 * A text prompt response.
 * 
 * @author John Jenkins
 */
public class TextPromptResponse extends PromptResponse {
	private final String text;

	/**
	 * Creates a new text prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param noResponse A 
	 * 					 {@link org.ohmage.domain.campaign.Response.NoResponse}
	 * 					 value if the user didn't supply an answer to this 
	 * 					 prompt.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made.
	 * 
	 * @param text The response from the user.
	 * 
	 * @param validate Whether or not to validate the response.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public TextPromptResponse(
			final TextPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final String text,
			final boolean validate) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((text == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both text and no response cannot be null.");
		}
		else if((text != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both text and no response were given.");
		}
		
		if(validate) {
			prompt.validateValue(text);
		}
		this.text = text;
	}
	
	/**
	 * Returns the text response from the user.
	 * 
	 * @return The text response from the user.
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

	/**
	 * Generates a hash code for this response.
	 * 
	 * @return A hash code for this prompt response.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	/**
	 * Determines if this prompt response is logically equivalent to another
	 * object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this response is logically equivalent to the other 
	 * 		   object; false, otherwise.
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