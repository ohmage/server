package org.ohmage.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.dao.ImageDaos;
import org.ohmage.dao.SurveyResponseDaos;
import org.ohmage.dao.SurveyResponseImageDaos;
import org.ohmage.domain.SurveyResponseInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class is responsible for creating, reading, updating, and deleting 
 * survey responses only. While it may read information from other classes, the
 * only values the functions in this class should take and return should 
 * pertain to survey responses. Likewise, it should only change the state of
 * survey responses.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class SurveyResponseServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private SurveyResponseServices() {}
	
	/**
	 * Generates a list of SurveyResponseInformation objects where each object
	 * represents an individual survey response and the list is the result of
	 * the optional parameters below. The campaign ID is required because the
	 * largest scale on which survey responses can be queried is by campaign.
	 * <br />
	 * <br />
	 * The remaining parameters are all optional as they further reduce the 
	 * list. For instance, if all optional parameters were null, the list would
	 * be all of the survey responses in the campaign. If the username were set
	 * to a specific user, the result would be all of the survey responses made
	 * by that user. If the username were set to a specific user and the start
	 * date were set to some date, the result would be all of the survey 
	 * responses made by that user on and after that date.<br />
	 * <br />
	 * If two parameters compete, like the start date is after the end date or
	 * the survey ID and prompt ID are present but that prompt is not part of
	 * that survey, no error will be thrown but the results will be empty.
	 *  
	 * @param request The Request that is performing this service. Required.
	 * 
	 * @param campaignId The campaign's unique identifier. Required.
	 * 
	 * @param username A user's username to which the results must only 
	 * 				   pertain. Optional.
	 * 
	 * @param client A client value to limit the results to only those uploaded
	 * 				 by this client. Optional.
	 * 
	 * @param startDate A date which limits the responses to those generated 
	 * 					on or after. Optional.
	 * 
	 * @param endDate An date which limits the responses to those generated on
	 * 				  or before. Optional.
	 * 
	 * @param privacyState A survey response privacy state that limits the 
	 * 					   results to only those with this privacy state.
	 * 					   Optional.
	 * 
	 * @param surveyId A campaign-wide unique survey ID that limits the 
	 * 				   responses to only those made for that survey. Optional.
	 * 
	 * @param promptId A campaign-wide unique prompt ID that limits the 
	 * 				   responses to only those made for that prompt. Optional.
	 * 
	 * @param promptType A prompt type that limits all responses to those of
	 * 					 exactly this prompt type. Optional.
	 * 
	 * @return Returns a, possibly empty but never null, list of survey 
	 * 		   responses that match the given criteria.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<SurveyResponseInformation> readSurveyResponseInformation(
			final Request request, final String campaignId,
			final String username, final String client, 
			final Date startDate, final Date endDate, 
			final SurveyResponsePrivacyStateCache.PrivacyState privacyState, 
			final String surveyId, final String promptId, final String promptType) throws ServiceException {
		try {
			// Populate the list with all of the survey response IDs.
			List<Long> surveyResponseIds = null;
			
			// Trim from the list all survey responses not made by a specified
			// user.
			if(username != null) {
				surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsFromUser(campaignId, username);
			}
			
			// Trim from the list all survey responses not made by a specified
			// client
			if(client != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsWithClient(campaignId, client);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsWithClient(campaignId, client));
				}
			}
			
			// Trim from the list all survey responses made before some date.
			if(startDate != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsAfterDate(campaignId, startDate);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsAfterDate(campaignId, startDate));
				}
			}
			
			// Trim from the list all survey responses made after some date.
			if(endDate != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsBeforeDate(campaignId, endDate);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsBeforeDate(campaignId, endDate));
				}
			}
			
			// Trim from the list all survey responses without a specified 
			// privacy state.
			if(privacyState != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsWithPrivacyState(campaignId, privacyState);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsWithPrivacyState(campaignId, privacyState));
				}
			}
			
			// Trim from the list all survey responses without a certain survey
			// ID.
			if(surveyId != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsWithSurveyId(campaignId, surveyId);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsWithSurveyId(campaignId, surveyId));
				}
			}
			
			// Trim from the list all survey responses without a certain prompt
			// ID.
			if(promptId != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsWithPromptId(campaignId, promptId);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsWithPromptId(campaignId, promptId));
				}
			}
			
			// Trim from the list all survey responses without a certain prompt
			// type.
			if(promptType != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseDaos.retrieveSurveyResponseIdsWithPromptType(campaignId, promptType);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseDaos.retrieveSurveyResponseIdsWithPromptType(campaignId, promptType));
				}
			}
			
			if((surveyResponseIds == null) || (surveyResponseIds.size() == 0)) {
				return Collections.emptyList();
			}
			else {
				return SurveyResponseDaos.retrieveSurveyResponseFromIds(surveyResponseIds);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates the privacy state on a survey.
	 * 
	 * @param request  The request to fail should an error occur.
	 * @param surveyResponseId  The key for the survey to update.
	 * @param privacyState  The new privacy state value.
	 * @throws ServiceException  If an error occurs.
	 */
	public static void updateSurveyResponsePrivacyState(Request request, Long surveyResponseId, SurveyResponsePrivacyStateCache.PrivacyState privacyState) throws ServiceException { 
		try {
			SurveyResponseDaos.updateSurveyResponsePrivacyState(surveyResponseId, privacyState);
		} 
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes all of the images associated with a survey response then deletes
	 * the survey response itself.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void deleteSurveyResponse(Request request, Long surveyResponseId) throws ServiceException {
		try {
			List<String> imageIds = SurveyResponseImageDaos.getImageIdsFromSurveyResponse(surveyResponseId);

			// Here we are deleting the images then deleting the survey 
			// response. If this fails, the entire service is aborted, but the
			// individual services that deleted the images are not rolled back.
			// This leads to an appropriate division of labor in the code; 
			// otherwise, the deleteSurveyResponse() function would need to 
			// modify the url_based_resource table and file system. If an image
			// deletion fails, the survey response will exist, but the images 
			// from the survey response that were deleted will be deleted. Not 
			// all of them will be deleted, however; the one that caused the
			// exception will remain. While this may seem like an error, I felt
			// that it would be worse to have the user stuck in a situation 
			// where no images were being deleted than one where only part of a
			// survey response was deleted. If the user is trying to delete an
			// image, this will give them the chance to delete it even if 
			// another image and/or the survey response are causing a problem.
			for(String imageId : imageIds) {
				ImageDaos.deleteImage(imageId);
			}
			
			SurveyResponseDaos.deleteSurveyResponse(surveyResponseId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}