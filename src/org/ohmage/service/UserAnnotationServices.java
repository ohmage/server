/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IAnnotationQueries;
import org.ohmage.util.StringUtils;

/**
 * This class contains services for user-annotation relationships.
 * 
 * @author Joshua Selsky
 */
public final class UserAnnotationServices {
	private static final Logger LOGGER = Logger.getLogger(UserAnnotationServices.class);
	private static UserAnnotationServices instance;
	
	private IAnnotationQueries annotationQueries;
	
	private UserAnnotationServices(IAnnotationQueries annotationQueries) {
		if(instance != null) {
			throw new IllegalStateException("Only one instance of this class is allowed.");
		}
		if(annotationQueries == null) {
			throw new IllegalArgumentException("An instance of IAnnotationQueries is required.");
		}
		
		this.annotationQueries = annotationQueries; 
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static UserAnnotationServices instance() {
		return instance;
	}
	
	/**
	 * Determines whether the provided user has the correct role to annotate
	 * the survey response. Only supervisors in campaigns can annotate 
	 * survey responses. 
	 * 
	 * @param username the user attempting to create an annotation
	 * @param campaignIds the campaigns the user belongs to
	 * @param surveyResponseId the survey response id to annotate
	 * @return true if the user can create the annotation; false otherwise.
	 * @throws ServiceException if there was a problem dispatching to the data
	 *                          layer
	 * @throws IllegalArgumentException if the input parameters are malformed  
	 */	
	public void userCanCreateSurveyResponseAnnotation(final String username, final Collection<String> campaignIds, final UUID surveyResponseId)
		throws ServiceException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("A username is required.");
		}
		if(campaignIds == null || campaignIds.isEmpty()) {
			throw new IllegalArgumentException("A non-null, non-empty list of campaign ids is required.");
		}
		if(surveyResponseId == null) {
			throw new IllegalArgumentException("A survey id is required.");
		}
		
		// Get the campaign that the survey id belongs to
		String campaignUrn = SurveyResponseServices.instance().getCampaignForSurveyResponseId(surveyResponseId);
		
		if(campaignUrn == null) {
			throw new ServiceException("Could not find a campaign URN for the survey id " + surveyResponseId);
		}
		
		if(! campaignIds.contains(campaignUrn)) {
			throw new ServiceException(ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "A user attempted to update a survey response for a campaign he or she is not a member of.");
		}
		
		// Create a temporary single-item list in order to use
		// UserCampaignServices.getCampaignAndUserRolesForCampaigns()
		LinkedList<String> ll = new LinkedList<String>();
		ll.add(campaignUrn);
		
		// Get the roles for this user in the campaign
		Map<Campaign, List<Campaign.Role>> campaignRoleMap = UserCampaignServices.instance().getCampaignAndUserRolesForCampaigns(username, ll, false);
		
		// Bad! Somehow this user exists in a campaign without a role
		if(campaignRoleMap.isEmpty()) {
			LOGGER.warn("A user is attempting to create a survey response annotation for campaign " + campaignUrn + ", but the user has no role in the campaign.");
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The logged-in user does not have the permissions to create a survey response annotation.");
		}
		
		// Just grab the only List in the Map
		List<Campaign.Role> roleList = campaignRoleMap.get(campaignRoleMap.keySet().iterator().next());
		
		if(! roleList.contains(Campaign.Role.SUPERVISOR)) {
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The logged-in user does not have the permissions to create a survey response annotation.");
		}
	}
	
	/**
	 * Dispatches to the data layer to create an annotation on a survey
	 * response.
	 * 
	 * @param client the ubiquitous client parameter
	 * @param time the annotation time (epoch milllis) 
	 * @param timezone the annotation timezone
	 * @param annotationText the annotation text
	 * @param surveyId  the unique survey identifier
	 * @return the UUID representing the key for the annotation
	 * @throws ServiceException if an error occurs
	 */
	public UUID createSurveyResponseAnnotation(String client, Long time, TimeZone timezone, String annotationText, UUID surveyId) 
			throws ServiceException {
		try {
			UUID annotationId = UUID.randomUUID();
			annotationQueries.createSurveyResponseAnnotation(annotationId, client, time, timezone, annotationText, surveyId);
			return annotationId;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Dispatches to the data layer to create an annotation on a survey
	 * response.
	 * 
	 * @param client the ubiquitous client parameter
	 * @param time the annotation time (epoch milllis) 
	 * @param timezone the annotation timezone
	 * @param annotationText the annotation text
	 * @param promptResponseId the unique prompt response identifier
 	 * @return the UUID representing the key for the annotation
	 * @throws ServiceException if an error occurs
	 */
	public UUID createPromptResponseAnnotation(String client, Long time, TimeZone timezone, String annotationText, Integer promptResponseId) 
			throws ServiceException {
		try {
			UUID annotationId = UUID.randomUUID();
			annotationQueries.createPromptResponseAnnotation(annotationId, client, time, timezone, annotationText, promptResponseId);
			return annotationId;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

}
