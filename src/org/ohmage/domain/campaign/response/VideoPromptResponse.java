package org.ohmage.domain.campaign.response;

import java.util.UUID;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.VideoPrompt;
import org.ohmage.exception.DomainException;

public class VideoPromptResponse extends PromptResponse {
	/**
	 * Creates a video prompt response.
	 * 
	 * @param prompt The VideoPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made. Otherwise, null.
	 * 
	 * @param response The response from the user. See
	 * 				   {@link VideoPrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException The repeatable set iteration is null and this 
	 * 						   was part of a repeatable set, the repeatable set
	 * 						   iteration is not null and this was not part of a
	 * 						   repeatable set, or the response could not be 
	 * 						   properly decoded. 
	 * 
	 * @see VideoPrompt#validateValue(Object) Validation Rules
	 */
	public VideoPromptResponse(
			final VideoPrompt prompt,
			final Integer repeatableSetIteration,
			final Object response)
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the UUID.
	 * 
	 * @return The UUID.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public UUID getUuid() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (UUID) getResponse();
	}
}