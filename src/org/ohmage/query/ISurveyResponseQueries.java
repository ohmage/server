package org.ohmage.query;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;

public interface ISurveyResponseQueries {
	/**
	 * Returns the survey response privacy states.
	 * 
	 * @return A list of all of the survey response privacy states.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<SurveyResponse.PrivacyState> retrieveSurveyResponsePrivacyStates()
			throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses made in
	 * a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsFromCampaign(final String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses made by
	 * a given user in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsFromUser(final String campaignId,
			final String username) throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses made by
	 * a given client in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param client The client value.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsWithClient(final String campaignId,
			final String client) throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses made on
	 * or after a given date in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param startDate The date.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsAfterDate(final String campaignId,
			final Date startDate) throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses made on
	 * or before a given date in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param endDate The date.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsBeforeDate(final String campaignId,
			final Date endDate) throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses with a
	 * given privacy state in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param privacyState The privacy state.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsWithPrivacyState(
			final String campaignId,
			final SurveyResponse.PrivacyState privacyState)
			throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses with a
	 * given survey ID in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param surveyId The survey's unique identifier.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsWithSurveyId(final String campaignId,
			final String surveyId) throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses with a
	 * given prompt ID in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param promptId The prompt's unique identifier.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsWithPromptId(final String campaignId,
			final String promptId) throws DataAccessException;

	/**
	 * Retrieves the survey response ID for all of the survey responses with a
	 * given prompt type in a given campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param promptType The prompt's type.
	 * 
	 * @return A, possibly empty but never null, list of survey response unique
	 * 		   identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<UUID> retrieveSurveyResponseIdsWithPromptType(final String campaignId,
			final String promptType) throws DataAccessException;

	/**
	 * Retrieves the information about a survey response including all of the
	 * individual prompt responses.
	 * 
	 * @param campaign The campaign that contains all of the surveys.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @return A SurveyResponseInformation object.
	 * 
	 * @throws DataAccessException Thrown if there is an error. This may be due
	 * 							   to a problem with the database or an issue
	 * 							   with the content in the database not being
	 * 							   understood by the SurveyResponseInformation
	 * 							   constructor.
	 */
	SurveyResponse retrieveSurveyResponseFromId(final Campaign campaign,
			final UUID surveyResponseId) throws DataAccessException;

	/**
	 * Retrieves the information about a list of survey responses including all
	 * of their individual prompt responses.
	 * 
	 * @param campaign The campaign that contains all of the surveys.
	 * 
	 * @param surveyResponseIds A collection of unique identifiers for survey
	 * 							responses whose information is being queried.
	 * 
	 * @return A list of SurveyResponseInformation objects each relating to a
	 * 		   survey response ID.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<SurveyResponse> retrieveSurveyResponseFromIds(final Campaign campaign,
			final Collection<UUID> surveyResponseIds)
			throws DataAccessException;
	
	/**
	 * Retrieves the information about survey responses that match the given
	 * criteria. The criteria is based on the parameters. All parameters are
	 * optional except the campaign, and a null parameter is the equivalent to
	 * an omitted parameter. If all parameters are null, except the campaign,
	 * then all of the information about all of the survey responses will be
	 * returned for that campaign.
	 * 
	 * @param campaign The campaign to which the survey responses must belong.
	 * 
	 * @param usernames Limits the results to only those submitted by any one 
	 * 					of the users in the list.
	 * 
	 * @param startDate Limits the results to only those survey responses that
	 * 					occurred on or after this date.
	 * 
	 * @param endDate Limits the results to only those survey responses that
	 * 				  occurred on or before this date.
	 * 
	 * @param privacyState Limits the results to only those survey responses
	 * 					   with this privacy state.
	 * 
	 * @param surveyIds Limits the results to only those survey responses that 
	 * 					were derived from a survey in this collection.
	 * 
	 * @param promptIds Limits the results to only those survey responses that 
	 * 					were derived from a prompt in this collection.
	 * 
	 * @param promptType Limits the results to only those survey responses that
	 * 					 are of the given prompt type.
	 * 
	 * @param surveyResponsesToSkip The number of survey responses to skip once
	 * 								the result has been aggregated from the 
	 * 								server.
	 * 
	 * @param surveyResponsesToProcess The number of survey responses to 
	 * 								   analyze once the survey responses to 
	 * 								   skip have been skipped.
	 * 
	 * @return A list of SurveyResponse objects where each object 
	 * 		   represents a survey response that matched the given criteria.
	 *  
	 * @throws DataAccessException Thrown if there is an error. 
	 */
	List<SurveyResponse> retrieveSurveyResponseDynamically(
			final Campaign campaign,
			final Collection<String> usernames,
			final Date startDate,
			final Date endDate,
			final SurveyResponse.PrivacyState privacyState,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final String promptType,
			final long surveyResponsesToSkip,
			final long surveyResponsesToProcess) 
			throws DataAccessException;

	/**
	 * Updates the privacy state on a survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * @param privacyState The survey's new privacy state
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void updateSurveyResponsePrivacyState(UUID surveyResponseId,
			SurveyResponse.PrivacyState newPrivacyState)
			throws DataAccessException;

	/**
	 * Deletes a survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	void deleteSurveyResponse(UUID surveyResponseId) throws DataAccessException;

}