package org.ohmage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ohmage.bin.MediaBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.domain.user.User;
import org.ohmage.javax.servlet.filter.AuthFilter;
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
@RequestMapping(MediaController.ROOT_MAPPING)
public class MediaController extends OhmageController {
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
        LoggerFactory.getLogger(MediaController.class.getName());

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

        LOGGER.info("Retrieving some media data.");

        LOGGER.info("Retrieving the requested media.");
        final Media mediaFile = MediaBin.getInstance().getMedia(mediaId);
        if(mediaFile == null) {
            throw new UnknownEntityException("The media file is unknown.");
        }

        LOGGER.info("Checking if a survey response is associated with the media.");
        SurveyResponse surveyResponse =
            SurveyResponseBin
                .getInstance()
                .getSurveyResponseForMedia(mediaId);
        if(surveyResponse != null) {
            LOGGER.info("The media file is associated with a survey response, " +
                        "so permissions must be checked.");

            LOGGER.info("Validating the user from the token");
            User user =
                OhmageController
                    .validateAuthorization(
                            authToken,
                            new Scope(
                                    Scope.Type.STREAM,
                                    surveyResponse.getSchemaId(),
                                    surveyResponse.getSchemaVersion(),
                                    Scope.Permission.READ));

            LOGGER.info("Verifying that the requester has given a sufficient " +
                        "token to view the response and its corresponding " +
                        "media.");
            if(! surveyResponse.getOwner().equals(user.getId())) {
                throw
                    new InsufficientPermissionsException(
                        "The given auth token does not give the requester " +
                            "permission to read this media file.");
            }
        }

        LOGGER.debug("Building the headers.");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", mediaFile.getContentType());
        responseHeaders
            .add("Content-Length", Long.toString(mediaFile.getSize()));

        LOGGER.debug("Returning the media as a resource.");
        return
            new ResponseEntity<Resource>(
                new InputStreamResource(mediaFile.getStream()),
                responseHeaders,
                HttpStatus.OK);
    }
}
