package org.ohmage.service;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.response.PhotoPromptResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ImageQueries;
import org.ohmage.query.SurveyResponseImageQueries;
import org.ohmage.query.SurveyResponseQueries;
import org.ohmage.query.SurveyUploadQuery;
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
	 * Creates new survey responses in the database.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param user The username of the user that created this survey response.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignUrn The unique identifier for the campaign to which the
	 * 					  survey belongs that the user used to create these
	 * 					  survey responses.
	 * 
	 * @param surveyUploadList The list of survey responses to add to the
	 * 						   database.
	 * 
	 * @param bufferedImageMap The map of image unique identifiers to 
	 * 						   BufferedImage objects to use when creating the
	 * 						   database entry.
	 * 
	 * @return A list of the indices of the survey responses that were 
	 * 		   duplicates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<Integer> createSurveyResponses(
			final Request request, final String user, final String client,
            final String campaignUrn,
            final List<SurveyResponse> surveyUploadList,
            final Map<String, BufferedImage> bufferedImageMap) 
            throws ServiceException {
		
		try {
			return SurveyUploadQuery.insertSurveys(user, client, campaignUrn, surveyUploadList, bufferedImageMap);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that, for all photo prompt responses, a corresponding image
	 * exists in the list of images.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param surveyResponses The survey responses.
	 * 
	 * @param images A map of image IDs to image contents.
	 * 
	 * @throws ServiceException Thrown if a prompt response exists but its
	 * 							corresponding contents don't.
	 */
	public static void verifyImagesExistForPhotoPromptResponses(
			final Request request, 
			final Collection<SurveyResponse> surveyResponses,
			final Map<String, BufferedImage> images) 
			throws ServiceException {
		
		for(SurveyResponse surveyResponse : surveyResponses) {
			for(Response promptResponse : surveyResponse.getResponses().values()) {
				if(promptResponse instanceof PhotoPromptResponse) {
					if(! images.containsKey(promptResponse.getResponseValue())) {
						request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, "An image key was found that was not present in the survey payload.");
						throw new ServiceException("An image key was found that was not present in the survey payload.");
					}
				}
			}
		}
	}
	
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
	 * @param usernames A user's username to which the results must only 
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
	 * @param surveyIds A collection of survey response IDs to which the 
	 * 					results must belong to any of them. Optional.
	 * 
	 * @param promptIds A collection of prompt response IDs to which the 
	 * 					results must belong ot any of them.
	 * 
	 * @param promptType A prompt type that limits all responses to those of
	 * 					 exactly this prompt type. Optional.
	 * 
	 * @return Returns a, possibly empty but never null, list of survey 
	 * 		   responses that match the given criteria.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<SurveyResponse> readSurveyResponseInformation(
			final Request request, final Campaign campaign,
			final String username, final String client, 
			final Date startDate, final Date endDate, 
			final SurveyResponse.PrivacyState privacyState, 
			final Collection<String> surveyIds, 
			final Collection<String> promptIds, 
			final String promptType) throws ServiceException {
		
		String campaignId = campaign.getId();
		
		try {
			// Populate the list with all of the survey response IDs.
			List<Long> surveyResponseIds = null;
			
			// Trim from the list all survey responses not made by a specified
			// user.
			if(username != null) {
				surveyResponseIds = SurveyResponseQueries.retrieveSurveyResponseIdsFromUser(campaignId, username);
			}
			
			// Trim from the list all survey responses not made by a specified
			// client
			if(client != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseQueries.retrieveSurveyResponseIdsWithClient(campaignId, client);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseQueries.retrieveSurveyResponseIdsWithClient(campaignId, client));
				}
			}
			
			// Trim from the list all survey responses made before some date.
			if(startDate != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseQueries.retrieveSurveyResponseIdsAfterDate(campaignId, startDate);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseQueries.retrieveSurveyResponseIdsAfterDate(campaignId, startDate));
				}
			}
			
			// Trim from the list all survey responses made after some date.
			if(endDate != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseQueries.retrieveSurveyResponseIdsBeforeDate(campaignId, endDate);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseQueries.retrieveSurveyResponseIdsBeforeDate(campaignId, endDate));
				}
			}
			
			// Trim from the list all survey responses without a specified 
			// privacy state.
			if(privacyState != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseQueries.retrieveSurveyResponseIdsWithPrivacyState(campaignId, privacyState);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseQueries.retrieveSurveyResponseIdsWithPrivacyState(campaignId, privacyState));
				}
			}
			
			// Trim from the list all survey responses without certain survey
			// IDs.
			if(surveyIds != null) {
				Set<Long> surveyIdIds = new HashSet<Long>();
				for(String surveyId : surveyIds) {
					surveyIdIds.addAll(SurveyResponseQueries.retrieveSurveyResponseIdsWithSurveyId(campaignId, surveyId));
				}
				
				if(surveyResponseIds == null) {
					surveyResponseIds = new LinkedList<Long>(surveyIdIds);
				}
				else {
					surveyResponseIds.retainAll(surveyIdIds);
				}
			}
			
			// Trim from the list all survey responses without certain prompt
			// IDs.
			if(promptIds != null) {
				Set<Long> promptIdIds = new HashSet<Long>();
				for(String promptId : promptIds) {
					promptIdIds.addAll(SurveyResponseQueries.retrieveSurveyResponseIdsWithPromptId(campaignId, promptId));
				}
				
				if(surveyResponseIds == null) {
					surveyResponseIds = new LinkedList<Long>(promptIdIds);
				}
				else {
					surveyResponseIds.retainAll(promptIdIds);
				}
			}
			
			// Trim from the list all survey responses without a certain prompt
			// type.
			if(promptType != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = SurveyResponseQueries.retrieveSurveyResponseIdsWithPromptType(campaignId, promptType);
				}
				else {
					surveyResponseIds.retainAll(SurveyResponseQueries.retrieveSurveyResponseIdsWithPromptType(campaignId, promptType));
				}
			}
			
			if(surveyResponseIds == null) {
				return SurveyResponseQueries.retrieveSurveyResponseFromIds(campaign, SurveyResponseQueries.retrieveSurveyResponseIdsFromCampaign(campaignId));
			}
			else if(surveyResponseIds.size() == 0) {
				return Collections.emptyList();
			}
			else {
				return SurveyResponseQueries.retrieveSurveyResponseFromIds(campaign, surveyResponseIds);
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
	public static void updateSurveyResponsePrivacyState(Request request, Long surveyResponseId, SurveyResponse.PrivacyState privacyState) throws ServiceException { 
		try {
			SurveyResponseQueries.updateSurveyResponsePrivacyState(surveyResponseId, privacyState);
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
			List<String> imageIds = SurveyResponseImageQueries.getImageIdsFromSurveyResponse(surveyResponseId);

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
				ImageQueries.deleteImage(imageId);
			}
			
			SurveyResponseQueries.deleteSurveyResponse(surveyResponseId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}