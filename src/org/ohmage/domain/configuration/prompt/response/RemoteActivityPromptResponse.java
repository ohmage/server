package org.ohmage.domain.configuration.prompt.response;

import org.json.JSONObject;
import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.RemoteActivityPrompt;

/**
 * A remote activity prompt response.
 * 
 * @author John Jenkins
 */
public class RemoteActivityPromptResponse extends PromptResponse {
	/**
	 */
	private final JSONObject result;
	
	public RemoteActivityPromptResponse(
			final RemoteActivityPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, final JSONObject result) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((result == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both result and no response cannot be null.");
		}
		else if((result != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both result and no response were given.");
		}
		else if((result != null) && (! prompt.validateValue(result))) {
			throw new IllegalArgumentException("The value is not valid.");
		}
		else {
			this.result = result;
		}
	}
	
	/**
	 * Returns the remote activity's result.
	 * @return  The remote activity's result. This may be null if no response   was given.
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