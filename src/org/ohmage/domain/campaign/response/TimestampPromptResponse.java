package org.ohmage.domain.campaign.response;

import java.util.Date;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.TimestampPrompt;
import org.ohmage.exception.DomainException;

/**
 * A timestamp prompt response.
 * 
 * @author John Jenkins
 */
public class TimestampPromptResponse extends PromptResponse {
	/**
	 * Creates a new timestamp prompt response.
	 * 
	 * @param prompt The TimestampPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made. Otherwise, null.
	 * 
	 * @param response The response from the user. See
	 * 				   {@link TimestampPrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException The repeatable set iteration is null and this 
	 * 						   was part of a repeatable set, the repeatable set
	 * 						   iteration is not null and this was not part of a
	 * 						   repeatable set, or the response could not be 
	 * 						   properly decoded. 
	 * 
	 * @see TimestampPrompt#validateValue(Object) Validation Rules
	 */
	public TimestampPromptResponse(
			final TimestampPrompt prompt,
			final Integer repeatableSetIteration, 
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the timestamp response from the user.
	 * 
	 * @return The timestamp response from the user.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public Date getTimestamp() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (Date) getResponse();
	}
}