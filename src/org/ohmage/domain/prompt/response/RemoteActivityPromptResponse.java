package org.ohmage.domain.prompt.response;

import org.json.JSONObject;
import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * A remote activity prompt response.
 * 
 * @author John Jenkins
 */
public class RemoteActivityPromptResponse extends PromptResponse {
	private final JSONObject result;
	
	/**
	 * Creates a remote activity prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param result The remote activity's result. This must be null if a 
	 * 				 NoResponse is given.
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
	public RemoteActivityPromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final JSONObject result, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_REMOTE_ACTIVITY, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
		if((result == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both result and no response cannot be null.");
		}
		else if((result != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both result and no response were given.");
		}
		else {
			this.result = result;
		}
	}
	
	/**
	 * Returns the remote activity's result.
	 * 
	 * @return The remote activity's result. This may be null if no response 
	 * 		   was given.
	 */
	public JSONObject getResult() {
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
}