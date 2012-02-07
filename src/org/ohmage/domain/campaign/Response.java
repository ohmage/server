package org.ohmage.domain.campaign;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the generic class for responses.
 * 
 * @author John Jenkins
 */
public abstract class Response {
	/**
	 * These values represent when a prompt or repeatable set does not have a
	 * response value because it was skipped, not displayed, etc.
	 * 
	 * @author  John Jenkins
	 */
	public static enum NoResponse { 
		/**
		 * The user skipped the prompt.
		 */
		SKIPPED, 
		/**
		 * The prompt was not displayed to the user.
		 */
		NOT_DISPLAYED;
		
		@Override
		public String toString() {
			return name();
		}
	}
	
	/**
	 * The Object representing this response. This could be a type-specific 
	 * response based on the survey or a NoResponse object if no response was
	 * given.
	 */
	private final Object response;
	
	/**
	 * Creates a new Response object.
	 * 
	 * @param noResponse The reason there is no response value or null if there
	 * 					 was a response value.
	 */
	public Response(final Object response) {
		this.response = response;
	}
	
	/**
	 * Returns whether or not this prompt was skipped.
	 * 
	 * @return Whether or not this prompt was skipped.
	 */
	public boolean wasSkipped() {
		return NoResponse.SKIPPED.equals(response);
	}
	
	/**
	 * Returns whether or not this prompt was not displayed.
	 * 
	 * @return Whether or not this prompt was not displayed.
	 */
	public boolean wasNotDisplayed() {
		return NoResponse.NOT_DISPLAYED.equals(response);
	}
	
	/**
	 * Returns an object representing the user's response.
	 * 
	 * @return An object representing the user's response.
	 */
	public Object getResponse() {
		return response;
	}

	/**
	 * Converts this response into a JSONObject.
	 * 
	 * @return A JSONObject representing this response.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	public abstract JSONObject toJson(
			final boolean withId) 
			throws JSONException;
	
	/**
	 * Retrieves the ID of the survey item to which this response pertains.
	 * 
	 * @return The unique identifier for the survey item to which this response
	 * 		   pertains.
	 */
	public abstract String getId();

	/**
	 * Generates a hash code for this response.
	 * 
	 * @return A hash code for this response.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((response == null) ? 0 : response.hashCode());
		return result;
	}

	/**
	 * Determines if this response is logically equivalent to another object.
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response other = (Response) obj;
		if (response != other.response)
			return false;
		return true;
	}
}