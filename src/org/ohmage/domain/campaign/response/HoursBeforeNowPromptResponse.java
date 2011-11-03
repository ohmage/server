package org.ohmage.domain.campaign.response;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.HoursBeforeNowPrompt;

/**
 * A hours-before-now prompt response.
 * 
 * @author John Jenkins
 */
public class HoursBeforeNowPromptResponse extends PromptResponse {
	private final Long hours;
	
	/**
	 * Creates a hours-before-now prompt response.
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
	 * @param hours The response from the user.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public HoursBeforeNowPromptResponse(
			final HoursBeforeNowPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final Long hours) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((hours == null) && (noResponse == null)) {
			throw new IllegalArgumentException(
					"Both hours and no response were null.");
		}
		else if((hours != null) && (noResponse != null)) {
			throw new IllegalArgumentException(
					"Both hours and no response were given.");
		}
		
		this.hours = hours;
	}
	
	/**
	 * Returns the hours response from the user.
	 * 
	 * @return The hours response from the user. 
	 */
	public Long getHours() {
		return hours;
	}
	
	/**
	 * Returns the hours as a string.
	 * 
	 * @return A String representing the hours.
	 */
	@Override
	public Object getResponseValue() {
		Object noResponseObject = super.getResponseValue();
		
		if(noResponseObject == null) {
			return hours;
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
		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
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
		HoursBeforeNowPromptResponse other = (HoursBeforeNowPromptResponse) obj;
		if (hours == null) {
			if (other.hours != null)
				return false;
		} else if (!hours.equals(other.hours))
			return false;
		return true;
	}
}