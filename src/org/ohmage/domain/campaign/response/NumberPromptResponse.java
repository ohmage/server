package org.ohmage.domain.campaign.response;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.NumberPrompt;

/**
 * A number prompt response.
 * 
 * @author John Jenkins
 */
public class NumberPromptResponse extends PromptResponse {
	private final Long number;
	
	/**
	 * Creates a number prompt response.
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
	 * @param number The response from the user.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public NumberPromptResponse(
			final NumberPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final Long number) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((number == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both number and no response cannot be null.");
		}
		else if((number != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both number and no response were given.");
		}
		
		this.number = number;
	}
	
	/**
	 * Returns the number response from the user.
	 * 
	 * @return The number response from the user.
	 */
	public Long getNumber() {
		return number;
	}
	
	/**
	 * Returns the number as a string.
	 * 
	 * @return A String representing the number.
	 */
	@Override
	public Object getResponseValue() {
		Object noResponseObject = super.getResponseValue();
		
		if(noResponseObject == null) {
			return number;
		}
		else {
			return noResponseObject;
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
		result = prime * result + ((number == null) ? 0 : number.hashCode());
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
		NumberPromptResponse other = (NumberPromptResponse) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}
}