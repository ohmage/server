package org.ohmage.domain.prompt.response;

import java.util.Collection;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * A multi-choice prompt response.
 * 
 * @author John Jenkins
 */
public class MultiChoicePromptResponse extends PromptResponse {
	private final Collection<String> choices;
	
	/**
	 * Creates a multi-choice prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param choices The choices. This must be null if a NoResponse is
	 * 				  given.
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
	public MultiChoicePromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final Collection<String> choices, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_MULTI_CHOICE, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((choices == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both hours and no response cannot be null.");
		}
		else if((choices != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both hours and no response were given.");
		}
		else {
			this.choices = choices;
		}
	}
	
	/**
	 * Returns the choices.
	 * 
	 * @return The choices. May be null if no response was given.
	 */
	public Collection<String> getChoices() {
		return choices;
	}

	/**
	 * Returns the choices as a string.
	 * 
	 * @return The choices as a String object.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return choices.toString();
		}
		else {
			return noResponseString;
		}		
	}
}