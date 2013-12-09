package org.ohmage.bin;

import java.util.List;
import java.util.Map;

import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * The interface to the database-backed survey response repositoy.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class SurveyResponseBin {
    /**
     * The singular instance of this class.
     */
    private static SurveyResponseBin instance;

    /**
     * Initializes the singleton instance to this.
     */
    protected SurveyResponseBin() {
        instance = this;
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static final SurveyResponseBin getInstance() {
        return instance;
    }

    /**
     * Adds new survey responses to the repository.
     *
     * @param surveyResponses
     *        The survey responses to add.
     *
     * @param media
     *        The map of ID to media to store with the data.
     *
     * @throws IllegalArgumentException
     *         The survey responses are null.
     *
     * @throws InvalidArgumentException
     *         Two survey responses have the same ID.
     */
    public abstract void addSurveyResponses(
        final List<SurveyResponse> surveyResponses,
        final Map<String, MultipartFile> media)
        throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Retrieves survey responses specific to a user and a survey.
     *
     * @param username
     *        The user's username.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @param surveyVersion
     *        The version of the survey.
     *
     * @return The survey responses that matches the parameters.
     *
     * @throws IllegalArgumentException
     *         A required parameter was null.
     */
    public abstract MultiValueResult<? extends SurveyResponse> getSurveyResponses(
        final String username,
        final String surveyId,
        final long surveyVersion)
        throws IllegalArgumentException;

    /**
     * Retrieves a specific survey response.
     *
     * @param username
     *        The user's username.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @param surveyVersion
     *        The version of the survey.
     *
     * @param pointId
     *        The unique identifier of the specific point.
     *
     * @return The survey responses that matches the parameters.
     *
     * @throws IllegalArgumentException
     *         A required parameter was null.
     */
    public abstract SurveyResponse getSurveyResponse(
        final String username,
        final String surveyId,
        final long surveyVersion,
        final String pointId)
        throws IllegalArgumentException;

    /**
     * Deletes a specific survey response.
     *
     * @param username
     *        The user's username.
     *
     * @param surveyId
     *        The survey's unique identifier.
     *
     * @param surveyVersion
     *        The version of the survey.
     *
     * @param pointId
     *        The unique identifier of the specific point.
     *
     * @throws IllegalArgumentException
     *         A required parameter was null.
     */
    public abstract void deleteSurveyResponse(
        final String username,
        final String surveyId,
        final long surveyVersion,
        final String pointId)
        throws IllegalArgumentException;
}