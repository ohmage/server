package org.ohmage.domain.prompt.response;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * A text prompt response.
 * 
 * @author John Jenkins
 */
public class TextPromptResponse extends PromptResponse {
	private final String text;
	
	/**
	 * Creates a text prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param text The text. This must be null if a NoResponse is given.
	 * 
	 * @param noResponse An indication of why there is no response for this
	 * 					 prompt. This should be null if there was a response. 
	 * 
	 * @throws IllegalArgumentException Thrown if a required parameter is null,
	 * 									or if both or neither of a response and
	 * 									a NoResponse were given.
	 * 
	 * @throws ErrorCodeException Thrown if the prompt ID is null or whitespace
	 * 							  only.
	 */
	public TextPromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final String text, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_TIMESTAMP, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((text == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both text and no response cannot be null.");
		}
		else if((text != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both text and no response were given.");
		}
		else {
			this.text = text;
		}
	}
	
	/**
	 * Returns the text.
	 * 
	 * @return The text. This may be null if no response was given.
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
}