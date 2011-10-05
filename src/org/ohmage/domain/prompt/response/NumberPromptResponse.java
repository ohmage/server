package org.ohmage.domain.prompt.response;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * A number prompt response.
 * 
 * @author John Jenkins
 */
public class NumberPromptResponse extends PromptResponse {
	private final Integer number;
	
	/**
	 * Creates a number prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param number The number prompt response. This must be null if a 
	 * 				 NoResponse is given.
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
	public NumberPromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final Integer number, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_NUMBER, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((number == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both number and no response cannot be null.");
		}
		else if((number != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both number and no response were given.");
		}
		else {
			this.number = number;
		}
	}
	
	/**
	 * Returns the number.
	 * 
	 * @return The number. May be null if no response was given.
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Returns the number as a string.
	 * 
	 * @return A String representing the number.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return Integer.toString(number);
		}
		else {
			return noResponseString;
		}
	}
}