package org.ohmage.domain.campaign.response;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.SingleChoiceCustomPrompt;

/**
 * A response to a single choice prompt with custom choices.
 * 
 * @author John Jenkins
 */
public class SingleChoiceCustomPromptResponse extends PromptResponse {
	private final String choice;

	/**
	 * Creates a response for a single choice prompt with custom choices. 
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
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public SingleChoiceCustomPromptResponse(
			final SingleChoiceCustomPrompt prompt, final NoResponse noResponse,
			final Integer repeatableSetIteration, final String choice) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((choice == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both choice and no response cannot be null.");
		}
		else if((choice != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both choice and no response were given.");
		}
		
		this.choice = choice;
	}
	
	/**
	 * Returns the choice from the user.
	 * 
	 * @return The choice from the user.
	 */
	public String getText() {
		return choice;
	}

	/**
	 * Returns the choice as a string.
	 * 
	 * @return The choice as a string.
	 */
	@Override
	public Object getResponseValue() {
		Object noResponseObject = super.getResponseValue();
		
		if(noResponseObject == null) {
			//return choice;
			return ((SingleChoiceCustomPrompt) getPrompt()).getChoiceKey(choice);
		}
		else {
			return noResponseObject;
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
		SingleChoiceCustomPromptResponse other = (SingleChoiceCustomPromptResponse) obj;
		if (choice == null) {
			if (other.choice != null)
				return false;
		} else if (!choice.equals(other.choice))
			return false;
		return true;
	}
}
