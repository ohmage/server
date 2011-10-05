package org.ohmage.domain.configuration;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class for all prompt responses.
 * 
 * @author John Jenkins
 */
public abstract class PromptResponse extends Response {
	private static final String JSON_KEY_PROMPT_ID = "prompt_id";
	private static final String JSON_KEY_RESPONSE = "value";
	
	/**
	 * The prompt that was presented to the user to create this response.
	 */
	private final Prompt prompt;
	
	/**
	 * The iteration of this prompt that generated this prompt.
	 */
	private final Integer repeatableSetIteration;

	public PromptResponse(final Prompt prompt, final NoResponse noResponse,
			final Integer repeatableSetIteration) {
		super(noResponse);
		
		this.prompt = prompt;
		this.repeatableSetIteration = repeatableSetIteration;
	}
	
	public Prompt getPrompt() {
		return prompt;
	}
	
	public Integer getRepeatableSetIteration() {
		return repeatableSetIteration;
	}
	
	/**
	 * Creates a JSONObject that represents this object.
	 * 
	 * @return A JSONObject that represents this object.
	 */
	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_PROMPT_ID, prompt.getId());
			result.put(JSON_KEY_RESPONSE, getResponseValue());
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
	
	@Override
	public String getId() {
		return prompt.getId();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((prompt == null) ? 0 : prompt.hashCode());
		result = prime
				* result
				+ ((repeatableSetIteration == null) ? 0
						: repeatableSetIteration.hashCode());
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
		PromptResponse other = (PromptResponse) obj;
		if (prompt == null) {
			if (other.prompt != null)
				return false;
		} else if (!prompt.equals(other.prompt))
			return false;
		if (repeatableSetIteration == null) {
			if (other.repeatableSetIteration != null)
				return false;
		} else if (!repeatableSetIteration.equals(other.repeatableSetIteration))
			return false;
		return true;
	}
}