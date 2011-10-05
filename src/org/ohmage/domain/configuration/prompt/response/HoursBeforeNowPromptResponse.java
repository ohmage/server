package org.ohmage.domain.configuration.prompt.response;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.HoursBeforeNowPrompt;

/**
 * A hours-before-now prompt.
 * 
 * @author John Jenkins
 */
public class HoursBeforeNowPromptResponse extends PromptResponse {
	/**
	 */
	private final Long hours;
	
	public HoursBeforeNowPromptResponse(
			final HoursBeforeNowPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final Long hours) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((hours == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both hours and no response cannot be null.");
		}
		else if((hours != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both hours and no response were given.");
		}
		else if((prompt != null) && (! prompt.validateValue(hours))) {
			throw new IllegalArgumentException("The number of hours is invalid.");
		}
		else {
			this.hours = hours;
		}
	}
	
	/**
	 * Returns the hours.
	 * @return  The hours or null if no response was given.
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
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return Long.toString(hours);
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
		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
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
		HoursBeforeNowPromptResponse other = (HoursBeforeNowPromptResponse) obj;
		if (hours == null) {
			if (other.hours != null)
				return false;
		} else if (!hours.equals(other.hours))
			return false;
		return true;
	}
}