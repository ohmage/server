package org.ohmage.domain.campaign.response;

import org.json.JSONArray;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.RemoteActivityPrompt;
import org.ohmage.exception.DomainException;

/**
 * A remote activity prompt response.
 * 
 * @author John Jenkins
 */
public class RemoteActivityPromptResponse extends PromptResponse {
	/**
	 * Creates a remote Activity prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made. Otherwise, null.
	 * 
	 * @param response The response from the user. See
	 * 				   {@link RemoteActivityPrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException The repeatable set iteration is null and this 
	 * 						   was part of a repeatable set, the repeatable set
	 * 						   iteration is not null and this was not part of a
	 * 						   repeatable set, or the response could not be 
	 * 						   properly decoded. 
	 * 
	 * @see RemoteActivityPrompt#validateValue(Object) Validation Rules
	 */
	public RemoteActivityPromptResponse(
			final RemoteActivityPrompt prompt,
			final Integer repeatableSetIteration, 
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the remote Activity's result.
	 * 
	 * @return The remote Activity's result.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public JSONArray getResult() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (JSONArray) getResponse();
	}
}