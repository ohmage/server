package org.ohmage.domain.prompt.response;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * Creates a single choice prompt response.
 * 
 * @author John Jenkins
 */
public class SingleChoicePromptResponse extends PromptResponse {
	private final String choice;
	
	/**
	 * Creates a single choice prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param choice The choice. This must be null if a NoResponse is given.
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
	public SingleChoicePromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final String choice, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_SINGLE_CHOICE, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((choice == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both choice and no response cannot be null.");
		}
		else if((choice != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both choice and no response were given.");
		}
		else {
			this.choice = choice;
		}
	}
	
	/**
	 * Returns the choice.
	 * 
	 * @return The choice. This may be null if no response was given.
	 */
	public String getText() {
		return choice;
	}

	/**
	 * Returns the choice as a string.
	 * 
	 * @return The choice.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return choice;
		}
		else {
			return noResponseString;
		}
	}
}