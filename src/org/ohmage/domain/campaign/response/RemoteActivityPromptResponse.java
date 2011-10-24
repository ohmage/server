package org.ohmage.domain.campaign.response;

import org.json.JSONArray;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.RemoteActivityPrompt;

/**
 * A remote activity prompt response.
 * 
 * @author John Jenkins
 */
public class RemoteActivityPromptResponse extends PromptResponse {
	private final JSONArray result;
	
	/**
	 * Creates a remote Activity prompt response.
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
	 * @param result The response from the user that should be a JSONArray of
	 * 				 JSONObjects.
	 * 
	 * @param validate Whether or not to validate the response.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are 
	 * 									invalid or if 'validate' is "true" and
	 * 									the response value is invalid.
	 */
	public RemoteActivityPromptResponse(
			final RemoteActivityPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final JSONArray result,
			final boolean validate) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((result == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both result and no response cannot be null.");
		}
		else if((result != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both result and no response were given.");
		}

		if(validate) {
			prompt.validateValue(result);
		}
		this.result = result;
	}
	
	/**
	 * Returns the remote Activity's result.
	 * 
	 * @return The remote Activity's result.
	 */
	public JSONArray getResult() {
		return result;
	}

	/**
	 * Returns the remote activity's result as a string.
	 * 
	 * @return The remote activity's result as a String.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return result.toString();
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
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
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
		RemoteActivityPromptResponse other = (RemoteActivityPromptResponse) obj;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}
}