package org.ohmage.domain.campaign.response;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.HoursBeforeNowPrompt;
import org.ohmage.exception.DomainException;

/**
 * A hours-before-now prompt response.
 * 
 * @author John Jenkins
 * 
 * @deprecated The HoursBeforeNowPrompt is deprecated, so its response must be
 * deprecated as well.
 */
public class HoursBeforeNowPromptResponse extends PromptResponse {
	/**
	 * Creates a hours-before-now prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made. Otherwise, null.
	 * 
	 * @param response The response from the user. See
	 * 				   {@link HoursBeforeNowPrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException The repeatable set iteration is null and this 
	 * 						   was part of a repeatable set, the repeatable set
	 * 						   iteration is not null and this was not part of a
	 * 						   repeatable set, or the response could not be 
	 * 						   properly decoded. 
	 * 
	 * @see HoursBeforeNowPrompt#validateValue(Object) Validation Rules
	 */
	public HoursBeforeNowPromptResponse(
			final HoursBeforeNowPrompt prompt, 
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the hours response from the user.
	 * 
	 * @return The hours response from the user. 
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public Long getHours() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (Long) getResponse();
	}
}