package org.ohmage.domain.configuration;

import org.json.JSONObject;

/**
 * This is the generic class for responses.
 * 
 * @author John Jenkins
 */
public abstract class Response {
	/**
	 * @author  jojenki
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
	 */
	private final NoResponse noResponse;
	
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
	 * Returns a String representation of the prompt response's value or, if
	 * there was no response, a string representing why.
	 * 
	 * @return A String representation of the prompt response's value or, if
	 * 		   there was no response, a string representing why.
	 */
	public String getResponseValue() {
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
	 * @return
	 */
	public abstract JSONObject toJson();
	
	/**
	 * Retrieves the ID of the survey item to which this response pertains.
	 * 
	 * @return The unique identifier for the survey item to which this response
	 * 		   pertains.
	 */
	public abstract String getId();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((noResponse == null) ? 0 : noResponse.hashCode());
		return result;
	}

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