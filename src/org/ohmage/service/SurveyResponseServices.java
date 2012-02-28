package org.ohmage.service;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.response.PhotoPromptResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IImageQueries;
import org.ohmage.query.ISurveyResponseImageQueries;
import org.ohmage.query.ISurveyResponseQueries;
import org.ohmage.query.ISurveyUploadQuery;

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
	private static SurveyResponseServices instance;
	
	private IImageQueries imageQueries;
	private ISurveyUploadQuery surveyUploadQuery;
	private ISurveyResponseQueries surveyResponseQueries;
	private ISurveyResponseImageQueries surveyResponseImageQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iImageQueries or iSurveyUploadQuery
	 * or iSurveyResponseQueries or iSurveyResponseImageQueries is null
	 */
	private SurveyResponseServices(IImageQueries iImageQueries, 
			                       ISurveyUploadQuery iSurveyUploadQuery,
			                       ISurveyResponseQueries iSurveyResponseQueries,
			                       ISurveyResponseImageQueries iSurveyResponseImageQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		if(iImageQueries == null) {
			throw new IllegalArgumentException("An instance of IImageQueries is required.");
		}
		if(iSurveyUploadQuery == null) {
			throw new IllegalArgumentException("An instance of ISurveyUploadQuery is required.");
		}
		if(iSurveyResponseQueries == null) {
			throw new IllegalArgumentException("An instance of ISurveyResponseQueries is required.");
		}
		if(iSurveyResponseImageQueries == null) {
			throw new IllegalArgumentException("An instance of ISurveyResponseImageQueries is required.");
		}
		
		imageQueries = iImageQueries;
		surveyUploadQuery = iSurveyUploadQuery;
		surveyResponseQueries = iSurveyResponseQueries;
		surveyResponseImageQueries = iSurveyResponseImageQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static SurveyResponseServices instance() {
		return instance;
	}
	
	/**
	 * Creates new survey responses in the database.
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
	public List<Integer> createSurveyResponses(final String user, 
			final String client, final String campaignUrn,
            final List<SurveyResponse> surveyUploadList,
            final Map<String, BufferedImage> bufferedImageMap) 
            throws ServiceException {
		
		try {
			return surveyUploadQuery.insertSurveys(user, client, campaignUrn, surveyUploadList, bufferedImageMap);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves all of the survey response privacy states.
	 * 
	 * @return A list of the survey response privacy states.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<SurveyResponse.PrivacyState> getSurveyResponsePrivacyStates() 
			throws ServiceException {
		
		try {
			return surveyResponseQueries.retrieveSurveyResponsePrivacyStates();
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that, for all photo prompt responses, a corresponding image
	 * exists in the list of images.
	 * 
	 * @param surveyResponses The survey responses.
	 * 
	 * @param images A map of image IDs to image contents.
	 * 
	 * @throws ServiceException Thrown if a prompt response exists but its
	 * 							corresponding contents don't.
	 */
	public void verifyImagesExistForPhotoPromptResponses(
			final Collection<SurveyResponse> surveyResponses,
			final Map<String, BufferedImage> images) 
			throws ServiceException {
		
		for(SurveyResponse surveyResponse : surveyResponses) {
			for(Response promptResponse : surveyResponse.getResponses().values()) {
				if(promptResponse instanceof PhotoPromptResponse) {
					Object responseValue = promptResponse.getResponseValue();
					if((responseValue instanceof UUID) && 
							(! images.containsKey(responseValue.toString()))) {
						throw new ServiceException(
								ErrorCode.SURVEY_INVALID_RESPONSES, 
								"An image key was found that was not present in the survey payload.");
					}
				}
			}
		}
	}
	
	/**
	 * Generates a list of SurveyResponse objects where each object
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
	public List<SurveyResponse> readSurveyResponseInformation(
			final Campaign campaign, final Collection<String> usernames, 
			final String client, 
			final Date startDate, final Date endDate, 
			final SurveyResponse.PrivacyState privacyState, 
			final Collection<String> surveyIds, 
			final Collection<String> promptIds, 
			final String promptType) throws ServiceException {
		
		try {
			return surveyResponseQueries.retrieveSurveyResponseDynamically(
					campaign, 
					usernames, 
					startDate, 
					endDate, 
					privacyState, 
					surveyIds, 
					promptIds, 
					promptType);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		/*
		String campaignId = campaign.getId();
		
		try {
			// Populate the list with all of the survey response IDs.
			List<UUID> surveyResponseIds = null;
			
			// Trim from the list all survey responses not made by a specified
			// user.
			if(username != null) {
				surveyResponseIds = surveyResponseQueries.retrieveSurveyResponseIdsFromUser(campaignId, username);
			}
			
			// Trim from the list all survey responses not made by a specified
			// client
			if(client != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = surveyResponseQueries.retrieveSurveyResponseIdsWithClient(campaignId, client);
				}
				else {
					surveyResponseIds.retainAll(surveyResponseQueries.retrieveSurveyResponseIdsWithClient(campaignId, client));
				}
			}
			
			// Trim from the list all survey responses made before some date.
			if(startDate != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = surveyResponseQueries.retrieveSurveyResponseIdsAfterDate(campaignId, startDate);
				}
				else {
					surveyResponseIds.retainAll(surveyResponseQueries.retrieveSurveyResponseIdsAfterDate(campaignId, startDate));
				}
			}
			
			// Trim from the list all survey responses made after some date.
			if(endDate != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = surveyResponseQueries.retrieveSurveyResponseIdsBeforeDate(campaignId, endDate);
				}
				else {
					surveyResponseIds.retainAll(surveyResponseQueries.retrieveSurveyResponseIdsBeforeDate(campaignId, endDate));
				}
			}
			
			// Trim from the list all survey responses without a specified 
			// privacy state.
			if(privacyState != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = surveyResponseQueries.retrieveSurveyResponseIdsWithPrivacyState(campaignId, privacyState);
				}
				else {
					surveyResponseIds.retainAll(surveyResponseQueries.retrieveSurveyResponseIdsWithPrivacyState(campaignId, privacyState));
				}
			}
			
			// Trim from the list all survey responses without certain survey
			// IDs.
			if(surveyIds != null) {
				Set<UUID> surveyIdIds = new HashSet<UUID>();
				for(String surveyId : surveyIds) {
					surveyIdIds.addAll(surveyResponseQueries.retrieveSurveyResponseIdsWithSurveyId(campaignId, surveyId));
				}
				
				if(surveyResponseIds == null) {
					surveyResponseIds = new LinkedList<UUID>(surveyIdIds);
				}
				else {
					surveyResponseIds.retainAll(surveyIdIds);
				}
			}
			
			// Trim from the list all survey responses without certain prompt
			// IDs.
			if(promptIds != null) {
				
				Set<UUID> promptIdIds = new HashSet<UUID>();
				for(String promptId : promptIds) {
					promptIdIds.addAll(surveyResponseQueries.retrieveSurveyResponseIdsWithPromptId(campaignId, promptId));
				}
				
				if(surveyResponseIds == null) {
					surveyResponseIds = new LinkedList<UUID>(promptIdIds);
				}
				else {
					surveyResponseIds.retainAll(promptIdIds);
				}
			}
			
			// Trim from the list all survey responses without a certain prompt
			// type.
			if(promptType != null) {
				if(surveyResponseIds == null) {
					surveyResponseIds = surveyResponseQueries.retrieveSurveyResponseIdsWithPromptType(campaignId, promptType);
				}
				else {
					surveyResponseIds.retainAll(surveyResponseQueries.retrieveSurveyResponseIdsWithPromptType(campaignId, promptType));
				}
			}
			
			if(surveyResponseIds == null) {
				List<UUID> allIds = 
					surveyResponseQueries.retrieveSurveyResponseIdsFromCampaign(campaignId);
				
				if(allIds.size() == 0) {
					return Collections.emptyList();
				}
				else {
					List<SurveyResponse> surveyResponses = new ArrayList<SurveyResponse>(allIds.size());
					for(UUID surveyResponseId : allIds) {
						surveyResponses.add(surveyResponseQueries.retrieveSurveyResponseFromId(campaign, surveyResponseId));
					}
					
					return surveyResponses;
				}
			}
			else if(surveyResponseIds.size() == 0) {
				return Collections.emptyList();
			}
			else {
				List<SurveyResponse> surveyResponses = new ArrayList<SurveyResponse>(surveyResponseIds.size());
				for(UUID surveyResponseId : surveyResponseIds) {
					surveyResponses.add(surveyResponseQueries.retrieveSurveyResponseFromId(campaign, surveyResponseId));
				}
				
				// This is a bit of a hack, but it avoids having to generate
				// John's dreaded dynamic SQL. :)
				
				// If the surveyResponses contain prompt responses for prompt 
				// ids not present in the query to our API, those prompt 
				// responses need to be pruned out of the SurveyResponse 
				if(promptIds != null && ! (promptIds.size() == 1 && new ArrayList<String>(promptIds).get(0).equals(SurveyResponseReadRequest.URN_SPECIAL_ALL))) {
					for(SurveyResponse surveyResponse : surveyResponses) {
						surveyResponse.filterPromptResponseByPromptIds(promptIds);
					}
				}
				
				return surveyResponses;
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		*/
	}
	
	/**
	 * Updates the privacy state on a survey.
	 * 
	 * @param surveyResponseId  The key for the survey to update.
	 * @param privacyState  The new privacy state value.
	 * @throws ServiceException  If an error occurs.
	 */
	public void updateSurveyResponsePrivacyState(
			final UUID surveyResponseId, 
			final SurveyResponse.PrivacyState privacyState) 
			throws ServiceException {
		
		try {
			surveyResponseQueries.updateSurveyResponsePrivacyState(surveyResponseId, privacyState);
		} 
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes all of the images associated with a survey response then deletes
	 * the survey response itself.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void deleteSurveyResponse(final UUID surveyResponseId) 
			throws ServiceException {
		
		try {
			List<UUID> imageIds = surveyResponseImageQueries.getImageIdsFromSurveyResponse(surveyResponseId);

			// TODO:
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
			for(UUID imageId : imageIds) {
				if(imageId != null) {
					imageQueries.deleteImage(imageId);
				}
			}
			
			surveyResponseQueries.deleteSurveyResponse(surveyResponseId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}