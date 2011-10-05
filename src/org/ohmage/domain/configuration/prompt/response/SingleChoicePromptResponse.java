package org.ohmage.domain.configuration.prompt.response;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.SingleChoicePrompt;

/**
 * Creates a single choice prompt response.
 * 
 * @author John Jenkins
 */
public class SingleChoicePromptResponse extends PromptResponse {
	/**
	 */
	private final String choice;

	public SingleChoicePromptResponse(
			final SingleChoicePrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final String choice) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((choice == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both choice and no response cannot be null.");
		}
		else if((choice != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both choice and no response were given.");
		}
		else if((choice != null) && (! prompt.validateValue(choice))) {
			throw new IllegalArgumentException("The choice is not valid.");
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((choice == null) ? 0 : choice.hashCode());
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
		SingleChoicePromptResponse other = (SingleChoicePromptResponse) obj;
		if (choice == null) {
			if (other.choice != null)
				return false;
		} else if (!choice.equals(other.choice))
			return false;
		return true;
	}
}