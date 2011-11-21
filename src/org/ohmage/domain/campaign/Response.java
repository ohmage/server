package org.ohmage.domain.campaign;

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
		
		/**
		 * Checks if a value is a NoResponse value.
		 * 
		 * @param value The value to check.
		 * 
		 * @return True if it is a NoResponse value; false, otherwise.
		 */
		public static boolean isNoResponse(final String value) {
			try {
				NoResponse.valueOf(value);
				return true;
			}
			catch(IllegalArgumentException e) {
				return false;
			}
		}
		
		@Override
		public String toString() {
			return name();
		}
	}
	private final NoResponse noResponse;
	
	/**
	 * Creates a new Response object.
	 * 
	 * @param noResponse The reason there is no response value or null if there
	 * 					 was a response value.
	 */
	public Response(final NoResponse noResponse) {
		this.noResponse = noResponse;
	}
	
	/**
	 * Returns whether or not this prompt was skipped.
	 * 
	 * @return Whether or not this prompt was skipped.
	 */
	public boolean wasSkipped() {
		return NoResponse.SKIPPED.equals(noResponse);
	}
	
	/**
	 * Returns whether or not this prompt was not displayed.
	 * 
	 * @return Whether or not this prompt was not displayed.
	 */
	public boolean wasNotDisplayed() {
		return NoResponse.NOT_DISPLAYED.equals(noResponse);
	}
	
	/**
	 * Returns an object representing the user's response.
	 * 
	 * @return An object representing the user's response.
	 */
	public Object getResponseValue() {
		if(wasSkipped()) {
			return NoResponse.SKIPPED.toString();
		}
		else if(wasNotDisplayed()) {
			return NoResponse.NOT_DISPLAYED.toString();
		}
		else {
			return null;
		}
	}

	/**
	 * Converts this response into a JSONObject.
	 * 
	 * @return A JSONObject representing this response.
	 */
	public abstract JSONObject toJson(final boolean withId);
	
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
				+ ((noResponse == null) ? 0 : noResponse.hashCode());
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
		if (noResponse != other.noResponse)
			return false;
		return true;
	}
}