package org.ohmage.domain.campaign.response;

// import java.util.UUID;
// import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.AudioPrompt;
import org.ohmage.exception.DomainException;

/**
 * <p>
 * An audio prompt response.
 * </p>
 *
 * @author John Jenkins
 */
public class AudioPromptResponse extends MediaPromptResponse {
	/**
	 * Creates an audio prompt response.
	 * 
	 * @param prompt
	 *        The PhotoPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration
	 *        If the prompt was part of a repeatable set, this is the iteration
	 *        of that repeatable set on which this response was made.
	 *        Otherwise, null.
	 * 
	 * @param response
	 *        The response from the user. See
	 *        {@link AudioPrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException
	 *         The repeatable set iteration is null and this was part of a
	 *         repeatable set, the repeatable set iteration is not null and
	 *         this was not part of a repeatable set, or the response could not
	 *         be properly decoded.
	 * 
	 * @see AudioPrompt#validateValue(Object) Validation Rules
	 */
	public AudioPromptResponse(
		final AudioPrompt prompt,
		final Integer repeatableSetIteration,
		final Object response)
		throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	

}