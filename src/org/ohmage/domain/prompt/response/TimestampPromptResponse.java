package org.ohmage.domain.prompt.response;

import java.util.Date;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.util.TimeUtils;

/**
 * A timestamp prompt response.
 * 
 * @author John Jenkins
 */
public class TimestampPromptResponse extends PromptResponse {
	private final Date timestamp;
	
	/**
	 * Creates a timestamp prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param timestamp The timestamp prompt response. This must be null if a 
	 * 					NoResponse is given.
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
	public TimestampPromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final Date timestamp, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_TIMESTAMP, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((timestamp == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both timestamp and no response cannot be null.");
		}
		else if((timestamp != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both timestamp and no response were given.");
		}
		else {
			this.timestamp = timestamp;
		}
	}
	
	/**
	 * Returns the timestamp.
	 * 
	 * @return The timestamp. This may be null if no response was given.
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
}