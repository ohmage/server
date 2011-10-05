package org.ohmage.domain.configuration.prompt;

import org.ohmage.domain.configuration.Prompt;
import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.PhotoPromptResponse;

public class PhotoPrompt extends Prompt {
	public static final String KEY_VERTICAL_RESOLUTION = "res";
	
	/**
	 */
	private final int verticalResolution;
	
	public PhotoPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final int verticalResolution, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.PHOTO, index);
		
		if(verticalResolution < 0) {
			throw new IllegalArgumentException("The vertical resolution cannot be negative.");
		}
		
		this.verticalResolution = verticalResolution;
	}
	
	/**
	 * @return
	 */
	public int getVerticalResolution() {
		return verticalResolution;
	}

	@Override
	public boolean validateValue(Object value) {
		// TODO: What class is 'value'? There is ImageIO, but that isn't 
		// available on Android. Image processing can be insanely slow. The
		// constraint is on the vertical resolution, so it doesn't make sense
		// to validate the UUID here. However, image processing can be insanely
		// slow.
		
		return true;
	}
	
	@Override
	public PhotoPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new PhotoPromptResponse(this, (NoResponse) response, repeatableSetIteration, null, null);
		}
		else if(response instanceof String) {
			return new PhotoPromptResponse(this, null, repeatableSetIteration, (String) response, null);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or String value.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + verticalResolution;
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
		PhotoPrompt other = (PhotoPrompt) obj;
		if (verticalResolution != other.verticalResolution)
			return false;
		return true;
	}
}