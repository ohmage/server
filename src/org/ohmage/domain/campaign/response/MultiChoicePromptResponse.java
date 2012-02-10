package org.ohmage.domain.campaign.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ohmage.domain.campaign.Prompt.LabelValuePair;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.MultiChoicePrompt;
import org.ohmage.exception.DomainException;

/**
 * A multiple-choice prompt response.
 * 
 * @author John Jenkins
 */
public class MultiChoicePromptResponse extends PromptResponse {
	/**
	 * Creates a multiple-choice prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made. Otherwise, null.
	 * 
	 * @param response The response from the user. See
	 * 				   {@link MultiChoicePrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException The repeatable set iteration is null and this 
	 * 						   was part of a repeatable set, the repeatable set
	 * 						   iteration is not null and this was not part of a
	 * 						   repeatable set, or the response could not be 
	 * 						   properly decoded. 
	 * 
	 * @see MultiChoicePrompt#validateValue(Object) Validation Rules
	 */
	public MultiChoicePromptResponse(
			final MultiChoicePrompt prompt,
			final Integer repeatableSetIteration, 
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the choices response from the user.
	 * 
	 * @return The choices response from the user.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public List<String> getChoices() throws DomainException{
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		@SuppressWarnings("unchecked")
		Collection<Integer> responses = (Collection<Integer>) getResponse();
		List<String> result = new ArrayList<String>(responses.size());
		
		Map<Integer, LabelValuePair> choices = 
				((MultiChoicePrompt) getPrompt()).getChoices();
		for(Integer key : responses) {
			result.add(choices.get(key).getLabel());
		}
		
		return result;
	}
}