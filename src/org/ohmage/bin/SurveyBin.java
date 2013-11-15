package org.ohmage.bin;

import java.util.List;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Survey;

/**
 * <p>
 * The interface to the database-backed survey repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class SurveyBin {
    /**
     * The singular instance of this class.
     */
    private static SurveyBin instance;

    /**
     * Initializes the singleton instance to this.
     */
    protected SurveyBin() {
        instance = this;
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static final SurveyBin getInstance() {
        return instance;
    }

    /**
     * Adds a new survey to the repository.
     *
     * @param survey
     *        The survey to add.
     *
     * @throws IllegalArgumentException
     *         The survey is null.
     *
     * @throws InvalidArgumentException
     *         A survey with the same ID-version pair already exists.
     */
    public abstract void addSurvey(
        final Survey survey)
        throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Returns a list of the visible survey IDs.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @return A list of the visible survey IDs.
     */
    public abstract List<String> getSurveyIds(final String query);

    /**
     * Returns a list of the versions for a given survey.
     *
     * @param surveyId
     *        The unique identifier for the survey.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @return A list of the versions of the survey.
     *
     * @throws IllegalArgumentException
     *         The survey ID is null.
     */
    public abstract List<Long> getSurveyVersions(
        final String surveyId,
        final String query)
        throws IllegalArgumentException;

    /**
     * Returns a Survey object for the desired survey.
     *
     * @param surveyId
     *        The unique identifier for the survey.
     *
     * @param surveyVersion
     *        The version of the survey.
     *
     * @return A Survey object that represents this survey.
     *
     * @throws IllegalArgumentException
     *         The survey ID and/or version are null.
     */
    public abstract Survey getSurvey(
        final String surveyId,
        final Long surveyVersion)
        throws IllegalArgumentException;

    /**
     * Returns whether or not a survey exists.
     *
     * @param surveyId
     *        The survey's unique identifier. Required.
     *
     * @param surveyVersion
     *        A specific version of a survey. Optional.
     *
     * @return Whether or not the survey exists.
     *
     * @throws IllegalArgumentException
     *         The survey ID is null.
     */
    public abstract boolean exists(
        final String surveyId,
        final Long surveyVersion)
        throws IllegalArgumentException;

    /**
     * Returns a Survey object that represents the survey with the greatest
     * version number or null if no surveys exist with the given ID.
     *
     * @param surveyId
     *        The unique identifier for the survey.
     *
     * @return A Survey object that represents the survey with the greatest
     *         version number or null if no surveys exist with the given ID.
     *
     * @throws IllegalArgumentException
     *         The survey ID is null.
     */
    public abstract Survey getLatestSurvey(
        final String surveyId)
        throws IllegalArgumentException;
}