package org.ohmage.domain.configuration.prompt.response;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.NumberPrompt;
import org.ohmage.exception.ErrorCodeException;

/**
 * A number prompt response.
 * 
 * @author John Jenkins
 */
public class NumberPromptResponse extends PromptResponse {
	/**
	 */
	private final Integer number;
	
	/**
	 * Creates a number prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
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
	public NumberPromptResponse(
			final NumberPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final Integer number) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((number == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both number and no response cannot be null.");
		}
		else if((number != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both number and no response were given.");
		}
		else if((number != null) && (! prompt.validateValue(number))) {
			throw new IllegalArgumentException("The number is not valid.");
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
			return number.toString();
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
		result = prime * result + ((number == null) ? 0 : number.hashCode());
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
		NumberPromptResponse other = (NumberPromptResponse) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}
}