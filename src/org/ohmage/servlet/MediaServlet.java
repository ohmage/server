package org.ohmage.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.MediaBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.domain.user.User;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>
 * The controller for all requests for media.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(MediaServlet.ROOT_MAPPING)
public class MediaServlet extends OhmageServlet {
    /**
     * The root API mapping for this Servlet.
     */
    public static final String ROOT_MAPPING = "/media";

    /**
     * The path and parameter key for survey media IDs.
     */
    public static final String KEY_MEDIA_ID = "media_id";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(MediaServlet.class.getName());

    /**
     * Retrieves the data for the requesting user.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param mediaId
     *        The unique identifier of the desired media.
     *
     * @return The media as a {@link Resource} object.
     */
    @RequestMapping(
        value = "{" + KEY_MEDIA_ID + "}",
        method = RequestMethod.GET)
    public static ResponseEntity<Resource> getMedia(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_MEDIA_ID) final String mediaId) {

        LOGGER.log(Level.INFO, "Retrieving some media data.");

        LOGGER.log(Level.INFO, "Retrieving the requested media.");
        final Media mediaFile = MediaBin.getInstance().getMedia(mediaId);
        if(mediaFile == null) {
            throw new UnknownEntityException("The media file is unknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Checking if a survey response is associated with the media.");
        SurveyResponse surveyResponse =
            SurveyResponseBin
                .getInstance()
                .getSurveyResponseForMedia(mediaId);
        if(surveyResponse != null) {
            LOGGER
                .log(
                    Level.INFO,
                    "The media file is associated with a survey response, " +
                        "so permissions must be checked.");

            LOGGER
                .log(Level.INFO, "Verifying that auth information was given.");
            if(authToken == null) {
                throw
                    new AuthenticationException(
                        "The media file is associated with a survey " +
                            "response and no authentication information was " +
                            "given.");
            }

            LOGGER
                .log(
                    Level.INFO,
                    "Retrieving the user associated with the token.");
            User user = authToken.getUser();

            LOGGER
                .log(
                    Level.INFO,
                    "Verifying that the requester has given a sufficient " +
                        "token to view the response and its corresponding " +
                        "media.");
            if(! surveyResponse.getOwner().equals(user.getId())) {
                throw
                    new InsufficientPermissionsException(
                        "The given auth token does not give the requester " +
                            "permission to read this media file.");
            }
        }

        LOGGER.log(Level.FINE, "Building the headers.");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", mediaFile.getContentType());
        responseHeaders
            .add("Content-Length", Long.toString(mediaFile.getSize()));

        LOGGER.log(Level.FINE, "Returning the media as a resource.");
        return
            new ResponseEntity<Resource>(
                new InputStreamResource(mediaFile.getStream()),
                responseHeaders,
                HttpStatus.OK);
    }
}