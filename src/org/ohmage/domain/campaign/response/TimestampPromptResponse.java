package org.ohmage.domain.campaign.response;

import java.util.Date;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.TimestampPrompt;
import org.ohmage.util.TimeUtils;

/**
 * A timestamp prompt response.
 * 
 * @author John Jenkins
 */
public class TimestampPromptResponse extends PromptResponse {
	private final Date timestamp;

	/**
	 * Creates a new timestamp prompt response.
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
	 * @param timestamp The timestamp from the user.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public TimestampPromptResponse(
			final TimestampPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final Date timestamp) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((timestamp == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both timestamp and no response cannot be null.");
		}
		else if((timestamp != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both timestamp and no response were given.");
		}
		
		this.timestamp = timestamp;
	}
	
	/**
	 * Returns the timestamp response from the user.
	 * 
	 * @return The timestamp response from the user.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the timestamp value as an ISO 8601 formatted date.
	 * 
	 * @return The timestamp value as an ISO 8601 formatted date.
	 */
	@Override
	public Object getResponseValue() {
		Object noResponseObject = super.getResponseValue();
		
		if(noResponseObject == null) {
			return TimeUtils.getIso8601DateTimeString(timestamp);
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
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
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
		TimestampPromptResponse other = (TimestampPromptResponse) obj;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}
}