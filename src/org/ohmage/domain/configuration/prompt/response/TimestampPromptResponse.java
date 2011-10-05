package org.ohmage.domain.configuration.prompt.response;

import java.util.Date;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.TimestampPrompt;
import org.ohmage.util.TimeUtils;

/**
 * A timestamp prompt response.
 * 
 * @author John Jenkins
 */
public class TimestampPromptResponse extends PromptResponse {
	/**
	 */
	private final Date timestamp;

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
		else if((timestamp != null) && (! prompt.validateValue(timestamp))) {
			throw new IllegalArgumentException("The timestamp is invalid.");
		}
		else {
			this.timestamp = timestamp;
		}
	}
	
	/**
	 * Returns the timestamp.
	 * @return  The timestamp. This may be null if no response was given.
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
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return TimeUtils.getIso8601DateTimeString(timestamp);
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
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
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
		TimestampPromptResponse other = (TimestampPromptResponse) obj;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}
}