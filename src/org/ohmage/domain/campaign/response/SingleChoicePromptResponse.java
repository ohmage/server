package org.ohmage.domain.campaign.response;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.SingleChoicePrompt;

/**
 * Creates a single choice prompt response.
 * 
 * @author John Jenkins
 */
public class SingleChoicePromptResponse extends PromptResponse {
	private final Integer choice;

	/**
	 * Creates a single choice prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param noResponse A 
	 * 					 {@link org.ohmage.domain.campaign.Response.NoResponse}
	 * 					 value if the user didn't supply an answer to this 
	 * 					 prompt.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made.
	 * 
	 * @param choice The key for the choice from the user.
	 * 
	 * @param validate Whether or not to validate the response.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public SingleChoicePromptResponse(
			final SingleChoicePrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final Integer choiceKey,
			final boolean validate) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((choiceKey == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both choice and no response cannot be null.");
		}
		else if((choiceKey != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both choice and no response were given.");
		}
		
		if(validate) {
			prompt.validateValue(choiceKey);
		}
		this.choice = choiceKey;
	}
	
	/**
	 * Returns the choice response from the user.
	 * 
	 * @return The choice response from the user.
	 */
	public String getText() {
		return ((SingleChoicePrompt) getPrompt()).getChoices().get(choice).getLabel();
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
			return getText();
		}
		else {
			return noResponseString;
		}
	}

	/**
	 * Generates a hash code for this response.
	 * 
	 * @return A hash code for this prompt response.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((choice == null) ? 0 : choice.hashCode());
		return result;
	}

	/**
	 * Determines if this prompt response is logically equivalent to another
	 * object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this response is logically equivalent to the other 
	 * 		   object; false, otherwise.
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