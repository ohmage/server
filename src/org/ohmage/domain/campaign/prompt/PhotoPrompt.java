package org.ohmage.domain.campaign.prompt;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.PhotoPromptResponse;

/**
 * This class represents a photo prompt.
 * 
 * @author John Jenkins
 */
public class PhotoPrompt extends Prompt {
	private static final String JSON_KEY_VERTICAL_RESOLUTION = "vertical_resolution";
	
	/**
	 * The campaign configuration property key for the desired vertical
	 * resolution of the image.
	 */
	public static final String XML_KEY_VERTICAL_RESOLUTION = "res";
	
	private final int verticalResolution;
	
	/**
	 * Creates a photo prompt.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param unit The unit value for this prompt.
	 * 
	 * @param text The text to be displayed to the user for this prompt.
	 * 
	 * @param abbreviatedText An abbreviated version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayType This prompt's
	 * 					 {@link org.ohmage.domain.campaign.Prompt.DisplayType}.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param verticalResolution The desired vertical resolution of the image.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if the vertical resolution is
	 * 									negative.
	 */
	public PhotoPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final int verticalResolution, final int index) {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.PHOTO, index);
		
		if(verticalResolution < 0) {
			throw new IllegalArgumentException("The vertical resolution cannot be negative.");
		}
		
		this.verticalResolution = verticalResolution;
	}
	
	/**
	 * Returns the desired vertical resolution of the image.
	 * 
	 * @return The desired vertical resolution of the image.
	 */
	public int getVerticalResolution() {
		return verticalResolution;
	}

	/**
	 * Validates that the given value is valid for this prompt.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A {@link NoResponse} value if a {@link NoResponse} object or no
	 * 		   response string value were given or a UUID value representing 
	 * 		   the photo's UUID.
	 */
	@Override
	public Object validateValue(final Object value) {
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new IllegalArgumentException("The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it is already a UUID value, then return it.
		else if(value instanceof UUID) {
			return (UUID) value;
		}
		// If it is a String value then attempt to decode it into a NoResponse
		// value or a UUID value.
		else if(value instanceof String) {
			String valueString = (String) value;
			
			try {
				return NoResponse.valueOf(valueString);
			}
			catch(IllegalArgumentException notNoResponse) {
				try {
					return UUID.fromString(valueString);
				}
				catch(IllegalArgumentException notUuid) {
					throw new IllegalArgumentException("The string response value was not decodable into a UUID.");
				}
			}
		}
		else {
			throw new IllegalArgumentException("The value is not decodable as a reponse value.");
		}
	}
	
	/**
	 * Creates a response to this prompt based on a response value.
	 * 
	 * @param response The response from the user as an Object.
	 * 
	 * @param repeatableSetIteration If this prompt belongs to a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which the response to
	 * 								 this prompt was made.
	 * 
	 * @throws IllegalArgumentException Thrown if this prompt is part of a
	 * 									repeatable set but the repeatable set
	 * 									iteration value is null, if the
	 * 									repeatable set iteration value is 
	 * 									negative, or if the value is not a 
	 * 									valid response value for this prompt.
	 */
	@Override
	public PhotoPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		
		if((repeatableSetIteration == null) && (getParent() != null)) {
			throw new IllegalArgumentException("The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if((repeatableSetIteration != null) && (repeatableSetIteration < 0)) {
			throw new IllegalArgumentException("The repeatable set iteration value is negative.");
		}
		
		Object validatedResponse = validateValue(response);
		if(validatedResponse instanceof NoResponse) {
			return new PhotoPromptResponse(
					this, 
					(NoResponse) validatedResponse, 
					repeatableSetIteration, 
					null, 
					null,
					false
				);
		}
		else if(validatedResponse instanceof UUID) {
			return new PhotoPromptResponse(
					this, 
					null, 
					repeatableSetIteration, 
					(UUID) validatedResponse,
					null,
					false
				);
		}
			
		throw new IllegalArgumentException("The response was not a valid response.");
	}
	
	/**
	 * Creates a JSONObject that represents this photo prompt.
	 * 
	 * @return A JSONObject that represents this photo prompt.
	 */
	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = super.toJson();
			
			if(result == null) {
				// FIXME: Ignore the exception thrown, allowing it to 
				// propagate.
				return null;
			}
			
			result.put(JSON_KEY_VERTICAL_RESOLUTION, verticalResolution);
			
			return result;
		}
		catch(JSONException e) {
			// FIXME: Throw an exception.
			return null;
		}
	}

	/**
	 * Generates a hash code for this prompt.
	 * 
	 * @return A hash code for this prompt.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + verticalResolution;
		return result;
	}

	/**
	 * Determines if this prompt is logically equivalent to another prompt.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the other object is logically equivalent to this
	 * 		   prompt.
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