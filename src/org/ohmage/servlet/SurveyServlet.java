package org.ohmage.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.survey.Survey;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * The controller for all requests for surveys and their responses.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(SurveyServlet.ROOT_MAPPING)
public class SurveyServlet extends OhmageServlet {
    /**
     * The root API mapping for this Servlet.
     */
    public static final String ROOT_MAPPING = "/surveys";

    /**
     * The path and parameter key for survey IDs.
     */
    public static final String KEY_SURVEY_ID = "id";
    /**
     * The path and parameter key for survey versions.
     */
    public static final String KEY_SURVEY_VERSION = "version";
    /**
     * The path and parameter key for survey point IDs.
     */
    public static final String KEY_SURVEY_RESPONSE_ID = "response_id";
    /**
     * The name of the parameter for querying for specific values.
     */
    public static final String KEY_QUERY = "query";
    /**
     * The name of all media parts in multipart requests.
     */
    public static final String KEY_MEDIA = "media";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(SurveyServlet.class.getName());

    /**
     * The {@link ObjectMapper} to use to decode the survey responses.
     */
    private static final ObjectMapper OBJECT_MAPPER = new OhmageObjectMapper();

    /**
     * The usage in this class is entirely static, so there is no need to
     * instantiate it.
     */
    private SurveyServlet() {
        // Do nothing.
    }

    /**
     * Creates a new survey.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param surveyBuilder
     *        A builder to use to create this new survey.
     */
    @RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
    public static @ResponseBody Survey createSurvey(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestBody
            final Survey.Builder surveyBuilder) {

        LOGGER.log(Level.INFO, "Creating a survey creation request.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.FINE, "Setting the owner of the survey.");
        surveyBuilder.setOwner(user.getUsername());

        LOGGER.log(Level.FINE, "Building the updated survey.");
        Survey result = surveyBuilder.build();

        LOGGER.log(Level.INFO, "Saving the new survey.");
        SurveyBin.getInstance().addSurvey(result);

        LOGGER.log(Level.INFO, "Returning the updated survey.");
        return result;
    }

    /**
     * Returns a list of visible survey IDs.
     *
     * @param search
     *        A value that should appear in either the name or description.
     *
     * @return A list of visible survey IDs.
     */
    @RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
    public static @ResponseBody List<String> getSurveyIds(
        @RequestParam(value = KEY_QUERY, required = false)
            final String query) {

        LOGGER.log(Level.INFO, "Creating a survey ID read request.");

        return SurveyBin.getInstance().getSurveyIds(query);
    }

    /**
     * Returns a list of versions for the given survey.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @return A list of the visible versions.
     */
    @RequestMapping(
        value = "{" + KEY_SURVEY_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody List<Long> getSurveyVersions(
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @RequestParam(value = KEY_QUERY, required = false)
            final String query) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to read the versions of a survey: " +
                    surveyId);

        return SurveyBin.getInstance().getSurveyVersions(surveyId, query);
    }

    /**
     * Returns the definition for a given survey.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @param surveyVersion
     *        The version of the survey.
     *
     * @return The survey definition.
     */
    @RequestMapping(
        value = "{" + KEY_SURVEY_ID + "}/{" + KEY_SURVEY_VERSION + "}",
        method = RequestMethod.GET)
    public static @ResponseBody Survey getSurveyDefinition(
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request for a survey definition: " +
                    surveyId + ", " +
                    surveyVersion);

        LOGGER.log(Level.INFO, "Retrieving the survey.");
        Survey result =
            SurveyBin
                .getInstance()
                .getSurvey(surveyId, surveyVersion);

        LOGGER.log(Level.FINE, "Ensuring that a survey was found.");
        if(result == null) {
            throw
                new UnknownEntityException(
                    "The survey ID-verion pair is unknown.");
        }

        LOGGER.log(Level.INFO, "Returning the survey.");
        return result;
    }

    /**
     * Updates an existing survey with a new version.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @param surveyBuilder
     *        A builder to use to create this new survey, which should be based
     *        on the survey that is being updated.
     */
    @RequestMapping(
        value = "{" + KEY_SURVEY_ID + "}",
        method = RequestMethod.POST)
    public static @ResponseBody Survey updateSurvey(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @RequestBody
            final Survey.Builder surveyBuilder) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to update a survey with a new version: " +
                    surveyId);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Retrieving the latest version of the survey.");
        Survey latestSchema =
            SurveyBin.getInstance().getLatestSurvey(surveyId);

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the new version of the survey is greater " +
                    "than all existing ones.");
        long latestVersion = latestSchema.getVersion();
        if(latestVersion >= surveyBuilder.getVersion()) {
            throw
                new InvalidArgumentException(
                    "The new version of this survey must be greater than " +
                        "the existing latest version of " +
                        latestVersion +
                        ".");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the user updating the survey is the owner " +
                    "of the original survey.");
        if(! latestSchema.getOwner().equals(user.getUsername())) {
            throw
                new InsufficientPermissionsException(
                    "Only the owner of this survey may update it.");
        }

        LOGGER
            .log(
                Level.FINE,
                "Setting the ID of the new survey to the ID of the old " +
                    "survey.");
        surveyBuilder.setSchemaId(surveyId);

        LOGGER
            .log(
                Level.FINE,
                "Setting the request user as the owner of this new survey.");
        surveyBuilder.setOwner(user.getUsername());

        LOGGER.log(Level.FINE, "Building the updated survey.");
        Survey result = surveyBuilder.build();

        LOGGER.log(Level.INFO, "Saving the updated survey.");
        SurveyBin.getInstance().addSurvey(result);

        LOGGER.log(Level.INFO, "Returning the updated survey.");
        return result;
    }

    /**
     * Stores data points.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @param surveyVersion
     *        The version of the survey.
     *
     * @param data
     *        The list of data points to save.
     */
    @RequestMapping(
        value = "{" + KEY_SURVEY_ID + "}/{" + KEY_SURVEY_VERSION + "}/data",
        method = RequestMethod.POST)
    public static @ResponseBody void storeData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion,
        @RequestPart(SurveyResponse.JSON_KEY_RESPONSES)
            final JsonNode surveyResponses,
        @RequestPart(KEY_MEDIA) final List<MultipartFile> media) {

        LOGGER.log(Level.INFO, "Storing some new survey data.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Retrieving the survey.");
        Survey survey =
            SurveyBin
                .getInstance()
                .getSurvey(surveyId, surveyVersion);

        LOGGER.log(Level.FINE, "Ensuring that a survey was found.");
        if(survey == null) {
            throw
                new UnknownEntityException(
                    "The survey ID-verion pair is unknown.");
        }

        LOGGER.log(Level.FINE, "Building the media map.");
        Map<String, MultipartFile> mediaMap =
            new HashMap<String, MultipartFile>();
        for(MultipartFile part : media) {
            mediaMap.put(part.getOriginalFilename(), part);
        }

        LOGGER.log(Level.INFO, "Converting the JSON into our object.");
        List<SurveyResponse.Builder> surveyResponseBuilders;
        try {
            surveyResponseBuilders =
                OBJECT_MAPPER
                    .readValue(
                        surveyResponses.toString(),
                        new TypeReference<List<SurveyResponse.Builder>>() {});
        }
        catch(JsonParseException e) {
            throw
                new InvalidArgumentException(
                    "The survey responses were not valid JSON.",
                    e);
        }
        catch(JsonMappingException e) {
            throw
                new InvalidArgumentException("The responses are invalid.", e);
        }
        catch(IOException e) {
            throw
                new IllegalStateException(
                    "The responses could not be read.",
                    e);
        }

        LOGGER.log(Level.INFO, "Validating the survey responses.");
        List<SurveyResponse> surveyResponseList =
            new ArrayList<SurveyResponse>(surveyResponseBuilders.size());
        for(SurveyResponse.Builder surveyResponse : surveyResponseBuilders) {
            surveyResponseList
                .add(
                    surveyResponse
                        .setOwner(user.getUsername())
                        .build(survey, mediaMap));
        }

        LOGGER.log(Level.INFO, "Storing the validated data.");
        SurveyResponseBin
            .getInstance()
            .addSurveyResponses(surveyResponseList, mediaMap);
    }

    /**
     * Retrieves the data for the requesting user.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param surveyId
     *        The unique identifier of the survey whose data is being
     *        requested.
     *
     * @param surveyVersion
     *        The version of the survey whose data is being requested.
     *
     * @return The data that conforms to the request parameters.
     */
    @RequestMapping(
        value = "{" + KEY_SURVEY_ID + "}/{" + KEY_SURVEY_VERSION + "}/data",
        method = RequestMethod.GET)
    public static @ResponseBody MultiValueResult<? extends SurveyResponse> getData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion) {

        LOGGER.log(Level.INFO, "Retrieving some survey data.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Finding and returning the requested data.");
        return
            SurveyResponseBin
                .getInstance()
                .getSurveyResponses(
                    user.getUsername(),
                    surveyId,
                    surveyVersion);
    }

    /**
     * Retrieves a point.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param surveyId
     *        The unique identifier of the survey whose data is being
     *        requested.
     *
     * @param surveyVersion
     *        The version of the survey whose data is being requested.
     *
     * @param pointId
     *        The unique identifier for a specific point.
     *
     * @return The data that conforms to the request parameters.
     */
    @RequestMapping(
        value =
            "{" + KEY_SURVEY_ID + "}" +
            "/" +
            "{" + KEY_SURVEY_VERSION + "}" +
            "/data" +
            "/" +
            "{" + KEY_SURVEY_RESPONSE_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody SurveyResponse getPoint(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion,
        @PathVariable(KEY_SURVEY_RESPONSE_ID) final String pointId) {

        LOGGER.log(Level.INFO, "Retrieving a specific survey data point.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Returning the survey data.");
        return
            SurveyResponseBin
                .getInstance()
                .getSurveyResponse(
                    user.getUsername(),
                    surveyId,
                    surveyVersion,
                    pointId);
    }

    /**
     * Deletes a point.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param surveyId
     *        The unique identifier of the survey whose data is being
     *        requested.
     *
     * @param surveyVersion
     *        The version of the survey whose data is being requested.
     *
     * @param pointId
     *        The unique identifier for a specific point.
     */
    @RequestMapping(
        value =
            "{" + KEY_SURVEY_ID + "}" +
            "/" +
            "{" + KEY_SURVEY_VERSION + "}" +
            "/data" +
            "/" +
            "{" + KEY_SURVEY_RESPONSE_ID + "}",
        method = RequestMethod.DELETE)
    public static @ResponseBody void deletePoint(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion,
        @PathVariable(KEY_SURVEY_RESPONSE_ID) final String pointId) {

        LOGGER.log(Level.INFO, "Deleting a specific survey data point.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Deleting the survey data.");
        SurveyResponseBin
            .getInstance()
            .deleteSurveyResponse(
                user.getUsername(),
                surveyId,
                surveyVersion,
                pointId);
    }
}