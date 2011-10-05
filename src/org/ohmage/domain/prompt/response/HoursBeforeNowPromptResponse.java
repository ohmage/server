package org.ohmage.domain.prompt.response;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * A hours-before-now prompt.
 * 
 * @author John Jenkins
 */
public class HoursBeforeNowPromptResponse extends PromptResponse {
	private final int hours;
	
	/**
	 * Creates a hours-before-now prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param hours The number of hours. This must be null if a NoResponse is
	 * 				given.
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
	public HoursBeforeNowPromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final Integer hours, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_HOURS_BEFORE_NOW, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((hours == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both hours and no response cannot be null.");
		}
		else if((hours != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both hours and no response were given.");
		}
		else {
			this.hours = hours;
		}
	}
	
	/**
	 * Returns the hours.
	 * 
	 * @return The hours or null if no response was given.
	 */
	public int getHours() {
		return hours;
	}
	
	/**
	 * Returns the hours as a string.
	 * 
	 * @return A String representing the hours.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return Integer.toString(hours);
		}
		else {
			return noResponseString;
		}
	}
}