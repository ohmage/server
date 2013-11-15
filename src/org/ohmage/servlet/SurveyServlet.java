package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.survey.Survey;
import org.ohmage.domain.survey.response.SurveyResponse;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * <p>
 * The controller for all requests for surveys and their responses.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(SurveyServlet.ROOT_MAPPING)
@SessionAttributes(
    {
        AuthFilter.ATTRIBUTE_AUTHORIZATION_TOKEN,
        AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN,
        AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM
    })
public class SurveyServlet {
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
     * The path and parameter key for stream point IDs.
     */
    public static final String KEY_SURVEY_RESPONSE_ID = "response_id";
    /**
     * The name of the parameter for querying for specific values.
     */
    public static final String KEY_QUERY = "query";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(SurveyServlet.class.getName());

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
     * @param token
     *        The user's authentication token.
     *
     * @param tokenIsParam
     *        Whether or not the authentication token was provided as a
     *        parameter.
     *
     * @param surveyBuilder
     *        A builder to use to create this new survey.
     */
    @RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
    public static @ResponseBody Survey createSurvey(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
            final AuthenticationToken token,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
            final boolean tokenIsParam,
        @RequestBody
            final Survey.Builder surveyBuilder) {

        LOGGER.log(Level.INFO, "Creating a survey creation request.");

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);

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
     * @param token
     *        The token of the user that is attempting to update the survey.
     *
     * @param tokenIsParam
     *        Whether or not the token was a parameter to the request.
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
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
            final AuthenticationToken token,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
            final boolean tokenIsParam,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @RequestBody
            final Survey.Builder surveyBuilder) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to update a survey with a new version: " +
                    surveyId);

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);

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
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
            final AuthenticationToken token,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
            final boolean tokenIsParam,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion,
        @RequestBody
            final List<SurveyResponse.Builder> surveyResponseBuilders) {

        LOGGER.log(Level.INFO, "Storing some new survey data.");

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);

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

        LOGGER.log(Level.INFO, "Validating the survey responses.");
        List<SurveyResponse> surveyResponses =
            new ArrayList<SurveyResponse>(surveyResponseBuilders.size());
        for(SurveyResponse.Builder surveyResponseBuilder : surveyResponseBuilders) {
            surveyResponses
                .add(
                    surveyResponseBuilder.setOwner(user.getUsername())
                        .build(survey));
        }

        LOGGER.log(Level.INFO, "Storing the validated data.");
        SurveyResponseBin.getInstance().addSurveyResponses(surveyResponses);
    }

    /**
     * Retrieves the data for the requesting user.
     *
     * @param authenticationToken
     *        The requesting user's authentication token.
     *
     * @param tokenIsParam
     *        Whether or not the requesting user's authentication token was a
     *        parameter.
     *
     * @param authorizationToken
     *        An authorization token sent by the requesting user.
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
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
            final AuthenticationToken authenticationToken,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
            final boolean tokenIsParam,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHORIZATION_TOKEN)
            final AuthorizationToken authorizationToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion) {

        LOGGER.log(Level.INFO, "Retrieving some survey data.");

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user =
            AuthFilter
                .retrieveUserFromAuth(
                    authorizationToken,
                    authenticationToken,
                    tokenIsParam);

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
     * Deletes a point.
     *
     * @param authenticationToken
     *        The requesting user's authentication token.
     *
     * @param tokenIsParam
     *        Whether or not the requesting user's authentication token was a
     *        parameter.
     *
     * @param authorizationToken
     *        An authorization token sent by the requesting user.
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
        method = RequestMethod.DELETE)
    public static @ResponseBody void deletePoint(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
            final AuthenticationToken authenticationToken,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
            final boolean tokenIsParam,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTHORIZATION_TOKEN)
            final AuthorizationToken authorizationToken,
        @PathVariable(KEY_SURVEY_ID) final String surveyId,
        @PathVariable(KEY_SURVEY_VERSION) final Long surveyVersion,
        @PathVariable(KEY_SURVEY_RESPONSE_ID) final String pointId) {

        LOGGER.log(Level.INFO, "Retrieving a specific survey data point.");

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user =
            AuthFilter
                .retrieveUserFromAuth(
                    authorizationToken,
                    authenticationToken,
                    tokenIsParam);

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