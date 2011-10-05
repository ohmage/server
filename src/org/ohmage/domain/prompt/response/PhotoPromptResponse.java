package org.ohmage.domain.prompt.response;

import java.io.File;

import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ErrorCodeException;

/**
 * A photo prompt response.
 * 
 * @author John Jenkins
 */
public class PhotoPromptResponse extends PromptResponse {
	private final String uuid;
	private final File photo;
	
	/**
	 * Creates a photo prompt response.
	 * 
	 * @param promptId The prompt's campaign-unique identifier.
	 * 
	 * @param repeatableSetId The campaign-unique identifier for the repeatable
	 * 						  set in which this prompt is contained.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set.
	 * 
	 * @param uuid The photo's UUID. This must be null if a NoResponse is 
	 * 			   given.
	 * 
	 * @param photo A File object referencing the file. This may be null if 
	 * 				there is no copy of the photo locally.
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
	public PhotoPromptResponse(final String promptId, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final String uuid, final File photo, final NoResponse noResponse)
			throws ErrorCodeException {
		
		super(promptId, PromptTypeKeys.TYPE_IMAGE, 
				repeatableSetId, repeatableSetIteration, noResponse);
		
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
	 * 
	 * @return The UUID. This may be null if no response was given.
	 */
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * Returns a File reference to the photo or null if none was given.
	 * 
	 * @return A File reference to the photo.
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
}