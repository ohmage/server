package org.ohmage.domain.configuration.prompt.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.MultiChoicePrompt;
import org.ohmage.exception.ErrorCodeException;

/**
 * A multi-choice prompt response.
 * 
 * @author John Jenkins
 */
public class MultiChoicePromptResponse extends PromptResponse {
	/**
	 */
	private final List<String> choices;
	
	/**
	 * Creates a multi-choice prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
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
	public MultiChoicePromptResponse(
			final MultiChoicePrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, 
			final Collection<String> choices) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((choices == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both hours and no response are null.");
		}
		else if((choices != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both hours and no response were given.");
		}
		else if((choices != null) && (! prompt.validateValue(choices))) {
			throw new IllegalArgumentException("The chosen choices were invalid.");
		}
		else {
			this.choices = new ArrayList<String>(choices);
		}
	}
	
	/**
	 * Returns the choices.
	 * 
	 * @return The choices. May be null if no response was given.
	 */
	public List<String> getChoices() {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((choices == null) ? 0 : choices.hashCode());
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
		MultiChoicePromptResponse other = (MultiChoicePromptResponse) obj;
		if (choices == null) {
			if (other.choices != null)
				return false;
		} else if (!choices.equals(other.choices))
			return false;
		return true;
	}
}