package org.ohmage.domain.survey.prompt;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to submit an image file.
 * </p>
 *
 * @author John Jenkins
 */
public class ImagePrompt extends MediaPrompt {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "image_prompt";

    /**
     * The JSON key for the maximum dimension of an image.
     */
    public static final String JSON_KEY_MAX_DIMENSION = "max_dimension";

    /**
     * The maximum allowed dimension of the two dimensions of an image.
     */
    @JsonProperty(JSON_KEY_MAX_DIMENSION)
    private final Integer maxDimension;

    /**
     * Creates a new image prompt.
     *
     * @param surveyItemId
     *        The survey-unique identifier for this prompt.
     *
     * @param condition
     *        The condition on whether or not to show this prompt.
     *
     * @param displayType
     *        The display type to use to visualize the prompt.
     *
     * @param text
     *        The text to display to the user.
     *
     * @param displayLabel
     *        The text to use as a short name in visualizations.
     *
     * @param skippable
     *        Whether or not this prompt may be skipped.
     *
     * @param defaultResponse
     *        The default response for this prompt or null if a default is not
     *        allowed.
     *
     * @param maxDimension
     *        The maximum allowed dimension of the two dimensions of an image.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public ImagePrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_DISPLAY_TYPE) final DisplayType displayType,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE) final String defaultResponse,
        @JsonProperty(JSON_KEY_MAX_DIMENSION) final Integer maxDimension)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            displayType,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        if(! DisplayType.CAMERA.equals(displayType)) {
            throw
                new InvalidArgumentException(
                    "The display type '" +
                        displayType.toString() +
                        "' is not valid for the prompt, which must be '" +
                        DisplayType.CAMERA.toString() +
                        "': " +
                        getSurveyItemId());
        }

        this.maxDimension = maxDimension;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.prompt.MediaPrompt#validateResponse(org.ohmage.domain.survey.Media)
     */
    @Override
    public void validateResponse(final Media response)
        throws InvalidArgumentException {

        // For now, we are not placing any image-specific limitations on their
        // responses.

//        // Validate the image.
//        ImageInputStream in;
//        try {
//            in = ImageIO.createImageInputStream(response.getInputStream());
//        }
//        catch(IOException e) {
//            throw new IllegalStateException("The media could not be read.", e);
//        }
//
//        // If there is a limit on the size of the image, check it here.
//        if(maxDimension != null) {
//            // Find readers for the image.
//            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
//
//            // As long as a reader can be found, use it.
//            if(readers.hasNext()) {
//                ImageReader reader = readers.next();
//
//                try {
//                    reader.setInput(in);
//
//                    if(reader.getWidth(0) > maxDimension) {
//                        throw
//                            new InvalidArgumentException(
//                                "The width of the image exceeds the max " +
//                                    "dimension of '" +
//                                    maxDimension +
//                                    "': " +
//                                    getSurveyItemId());
//                    }
//
//                    if(reader.getHeight(0) > maxDimension) {
//                        throw
//                            new InvalidArgumentException(
//                                "The height of the image exceeds the max " +
//                                    "dimension of '" +
//                                    maxDimension +
//                                    "': " +
//                                    getSurveyItemId());
//                    }
//                }
//                catch(IOException e) {
//                    throw
//                        new IllegalStateException(
//                            "The media could not be read.",
//                            e);
//                }
//                finally {
//                    reader.dispose();
//                }
//            }
//            // Otherwise, indicate that the image's format is unknown.
//            else {
//                throw
//                    new InvalidArgumentException(
//                        "The image's format, '" +
//                            response.getContentType() +
//                            "', is unknown: " +
//                            getSurveyItemId());
//            }
//        }
    }
}