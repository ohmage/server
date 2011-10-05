package org.ohmage.domain.configuration.prompt.response;

import java.io.File;

import org.ohmage.domain.configuration.PromptResponse;
import org.ohmage.domain.configuration.prompt.PhotoPrompt;

/**
 * A photo prompt response.
 * 
 * @author John Jenkins
 */
public class PhotoPromptResponse extends PromptResponse {
	/**
	 */
	private final String uuid;
	/**
	 */
	private final File photo;

	public PhotoPromptResponse(
			final PhotoPrompt prompt, final NoResponse noResponse, 
			final Integer repeatableSetIteration, 
			final String uuid, final File photo) {
		
		super(prompt, noResponse, repeatableSetIteration);
		
		if((uuid == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both UUID and no response cannot be null.");
		}
		else if((uuid != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both UUID and no response were given.");
		}
		else {
			this.uuid = uuid;
		}
		
		this.photo = photo;
	}
	
	/**
	 * Returns the UUID.
	 * @return  The UUID. This may be null if no response was given.
	 */
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * Returns a File reference to the photo or null if none was given.
	 * @return  A File reference to the photo.
	 */
	public File getPhoto() {
		return photo;
	}

	/**
	 * Returns the photo's UUID.
	 * 
	 * @return The photo's UUID.
	 */
	@Override
	public String getResponseValue() {
		String noResponseString = super.getResponseValue();
		
		if(noResponseString == null) {
			return uuid;
		}
		else {
			return noResponseString;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		PhotoPromptResponse other = (PhotoPromptResponse) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}