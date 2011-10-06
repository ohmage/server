package org.ohmage.domain.campaign;

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

	/**
	 * Creates a prompt response.
	 * 
	 * @param prompt The prompt that was presented to the user in order to
	 * 				 generate this prompt response.
	 * 
	 * @param noResponse A NoResponse object which represents the allowed 
	 * 					 values if the prompt does not have a response.
	 * 
	 * @param repeatableSetIteration If the 'prompt' was part of a repeatable
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt is null.
	 */
	public PromptResponse(final Prompt prompt, final NoResponse noResponse,
			final Integer repeatableSetIteration) {
		
		super(noResponse);
		
		if(prompt == null) {
			throw new IllegalArgumentException("The prompt is null.");
		}
		else if((repeatableSetIteration == null) && 
				(prompt.getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if(repeatableSetIteration < 0) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		this.prompt = prompt;
		this.repeatableSetIteration = repeatableSetIteration;
	}
	
	/**
	 * Returns the prompt on which this prompt response is based.
	 * 
	 * @return The prompt on which this prompt response is based.
	 */
	public Prompt getPrompt() {
		return prompt;
	}
	
	/**
	 * If the prompt used to generate this prompt response was part of a 
	 * repeatable set, then this will return the iteration of that repeatable
	 * set on which this response was generated. If it is not part of a 
	 * repeatable set, null will be returned.
	 * 
	 * @return The iteration of the repeatable set that is this prompt's parent
	 * 		   on which this response was generated. If the prompt was not part
	 * 		   of a repeatable set, null is returned.
	 */
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
	
	/**
	 * Return's the prompt's unique identifier.
	 */
	@Override
	public String getId() {
		// FIXME: This function should be renamed as prompt responses have
		// unique identifiers as well.
		return prompt.getId();
	}
	
	/**
	 * Generates a hash code for this prompt response.
	 * 
	 * @return This prompt's hash code.
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

	/**
	 * Determines if this prompt response is equal to another object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this prompt response is logically equal to the other
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