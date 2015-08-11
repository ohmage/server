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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignImageQueries;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.IImageQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.impl.QueryResultsList;
import org.ohmage.request.campaign.CampaignSearchRequest;

/**
 * This class contains the services that pertain to campaigns.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class CampaignServices {
	private static final Logger LOGGER = Logger.getLogger(CampaignServices.class);

	private static CampaignServices instance;
	private ICampaignQueries campaignQueries;
	private ICampaignImageQueries campaignImageQueries;
	private IImageQueries imageQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iCampaignQueries is null
	 */
	private CampaignServices(ICampaignQueries iCampaignQueries,
			ICampaignImageQueries iCampaignImageQueries,
			IImageQueries iImageQueries) {
		
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		if(iCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignQueries is required.");
		}
		if(iCampaignImageQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignImageQueries is a required argument.");
		}
		if(iImageQueries == null) {
			throw new IllegalArgumentException("An instance of IImageQueries is a required argument.");
		}
		
		campaignQueries = iCampaignQueries;
		campaignImageQueries = iCampaignImageQueries;
		imageQueries = iImageQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static CampaignServices instance() {
		return instance;
	}
	
	/**
	 * Creates a new campaign.
	 * 
	 * @param campaignId The new campaign's unique identifier.
	 * 
	 * @param name The new campaign's name.
	 * 
	 * @param xml The new campaign's XML.
	 * 
	 * @param description The new campaign's description.
	 * 
	 * @param runningState The new campaign's initial running state.
	 * 
	 * @param privacyState The new campaign's initial privacy state.
	 * 
	 * @param classIds A List of class identifiers for classes that are going
	 * 				   to be initially associated with the campaign.
	 * 
	 * @param creatorUsername The username of the user that will be set as the
	 * 						  author.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void createCampaign(
			final Campaign campaign,
			final Collection<String> classIds, 
			final String creatorUsername) 
			throws ServiceException {
		
		try {
			campaignQueries
				.createCampaign(campaign, classIds, creatorUsername);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if a campaign already exists or not based on the 'shouldExist'
	 * flag.
	 * 
	 * @param campaignId The unique identifier of a campaign whose existence is
	 * 					 being checked.
	 * 
	 * @param shouldExist Whether or not the campaign should already exist.
	 * 
	 * @throws ServiceException Thrown if there is an error, if the campaign
	 * 							exists and it shouldn't, or if the campaign
	 * 							doesn't exist and it should.
	 */
	public void checkCampaignExistence(final String campaignId, 
			final boolean shouldExist) throws ServiceException {
		
		try {
			if(campaignQueries.getCampaignExists(campaignId)) {
				if(! shouldExist) {
					throw new ServiceException(
							ErrorCode.CAMPAIGN_INVALID_XML, 
							"The campaign already exists.");
				}
			}
			else {
				if(shouldExist) {
					throw new ServiceException(
							ErrorCode.CAMPAIGN_INVALID_ID, 
							"The campaign does not exist.");
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if the existence of every campaign in a List of campaign IDs
	 * matches the parameterized 'shouldExist'.
	 * 
	 * @param campaignIds A List of campaign IDs to check.
	 * 
	 * @param shouldExist Whether or not every campaign in the List should 
	 * 					  exist or not.
	 * 
	 * @throws ServiceException Thrown if any of the campaigns exist and they
	 * 							shouldn't or if any of the campaigns don't 
	 * 							exist and they should.
	 */
	public void checkCampaignsExistence(
			final Collection<String> campaignIds, final boolean shouldExist) 
			throws ServiceException {
		
		for(String campaignId : campaignIds) {
			checkCampaignExistence(campaignId, shouldExist);
		}
	}
	
	/**
	 * Retrieves the XML for a campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return Returns the XML for the campaign. If the campaign doens't exist,
	 * 		   null is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public String getCampaignXml(final String campaignId) 
			throws ServiceException {
		
		try {
			return campaignQueries.getXml(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the name of a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return Returns the name of the campaign. If the campaign doesn't exist
	 * 		   null is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public String getCampaignName(final String campaignId) 
			throws ServiceException {
		
		try {
			return campaignQueries.getName(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Ensures that the prompt ID exists in the campaign XML of the campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign whose XML is 
	 * 					 being checked.
	 * 
	 * @param promptId A prompt ID that is unique to the campaign's XML and is
	 * 				   being checked for existance.
	 * 
	 * @throws ServiceException Throws a 
	 * 							{@value org.ohmage.annotator.ErrorCodes#CAMPAIGN_NOT_FOUND}
	 * 							if the campaign doesn't exist or a
	 * 							{@value org.ohmage.annotator.ErrorCodes##CAMPAIGN_UNKNOWN_PROMPT_ID}
	 * 							if the prompt ID doesn't exist in the 
	 * 							campaign's XML. Also, thrown if there is an 
	 * 							error.
	 */
	public void ensurePromptExistsInCampaign(final String campaignId, 
			final String promptId) throws ServiceException {
		
		// Get the XML.
		String xml;
		try {
			xml = campaignQueries.getXml(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		if(xml == null) {
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INVALID_ID, 
					"There is no such campaign with the campaign ID: " + 
						campaignId);
		}
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(xml));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			throw new ServiceException("Unable to read XML.", e);
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			throw new ServiceException("Invalid XML.", e);
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			throw new ServiceException("XML cannot be parsed.", e);
		}
		
		// Find all the prompt IDs with the parameterized promptId.
		Element root = document.getRootElement();
		Nodes nodes = root.query("/campaign/surveys/survey/contentList/prompt[id='" + promptId + "']");
		if(nodes.size() == 0) {
			throw new ServiceException(
					ErrorCode.SURVEY_INVALID_PROMPT_ID, 
					"The following prompt ID is not part of the campaign's XML: " + 
						promptId);
		}
	}
	
	/**
	 * Verifies that the campaign is running.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the campaign is not running or if 
	 * 							there is an error.
	 */
	public void verifyCampaignIsRunning(final String campaignId) 
			throws ServiceException {
		
		try {
			if(! Campaign.RunningState.RUNNING.equals(
					campaignQueries.getCampaignRunningState(campaignId))) {
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INVALID_RUNNING_STATE, 
						"The campaign is not running.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the given timestamp is the same as the campaign's creation
	 * timestamp.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param creationTimestamp The expected campaign creation timestamp.
	 * 
	 * @throws ServiceException Thrown if the expected and actual campaign 
	 * 							creation timestamps are not the same or if 
	 * 							there is an error.
	 */
	public void verifyCampaignIsUpToDate(final Campaign campaign, 
			final DateTime creationTimestamp) throws ServiceException {
		
		long creationTimeWithoutMillis = 
			campaign.getCreationTimestamp().getMillis() / 1000;
		long givenTimeWithoutMillis = creationTimestamp.getMillis() / 1000;
		
		if(creationTimeWithoutMillis != givenTimeWithoutMillis) {
			throw new ServiceException(
					ErrorCode.CAMPAIGN_OUT_OF_DATE, 
					"The given timestamp is not the same as the campaign's creation timestamp.");
		}
	}
	
	/**
	 * Finds the configuration for the campaign identified by the campaign id.
	 * 
	 * @param campaignId The campaign id to use for lookup.
	 * @return a Campaign instance created from the XML for the campaign.
	 * @throws ServiceException If an error occurred in the data layer.
	 */
	public Campaign getCampaign(final String campaignId)
			throws ServiceException {
		
		try {
			return campaignQueries.findCampaignConfiguration(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Gets all of the campaigns, removing those that don't match the given
	 * criteria. The results are sorted alphabetically for paging.
	 * 
	 * @param campaignId Limits the results to only those campaigns that have 
	 * 					 this unique identifier.
	 * 
	 * @param surveyId Limits the results to only those campaigns that have a
	 * 				   survey whose unique identifier matches this.
	 * 
	 * @param promptId Limits the results to only those campaigns that have a
	 * 				   prompt whose unique identifier matches this.
	 * 
	 * @param numToSkip The number of campaigns to skip.
	 * 
	 * @param numToReturn The number of campaigns to return.
	 * 
	 * @return A collection of all campaigns that matched the given criteria.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public List<Campaign> getCampaigns(
		final Collection<String> campaignIds,
		final Collection<String> surveyIds,
		final Collection<String> promptIds,
		final Collection<String> classIds,
		final DateTime startDate,
		final DateTime endDate,
		final Campaign.PrivacyState privacyState,
		final Campaign.RunningState runningState,
		final long numToSkip,
		final long numToReturn)
			throws ServiceException {
		
		try {
			return 
				campaignQueries
					.getCampaigns(
						campaignIds, 
						surveyIds, 
						promptIds,
						classIds,
						startDate,
						endDate,
						privacyState,
						runningState,
						numToSkip, 
						numToReturn);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	
	/**
	 * Begins with all of the campaigns that exist in the system and then 
	 * removes those that don't match the parameterized criteria. If a  
	 * parameter is null, it is ignored. Therefore, if all parameters are null,
	 * then all campaign IDs are returned.
	 * 
	 * @param partialCampaignId Only return campaigns whose ID contains this
	 * 							value.
	 * 
	 * @param partialCampaignName Only return campaigns whose name contains 
	 * 							  this value.
	 * 
	 * @param partialDescription Only return campaigns whose description 
	 * 							 contains this value.
	 * 
	 * @param partialXml Only return campaigns whose XML contains this value.
	 * 
	 * @param partialAuthoredBy Only return campaigns whose authored by value
	 * 							contains this value.
	 * 
	 * @param startDate Only return campaigns that were created on or after 
	 * 					this date.
	 * 
	 * @param endDate Only return campaigns that were created on or before this
	 * 				  date.
	 * 
	 * @param privacyState Only return campaigns with this privacy state.
	 * 
	 * @param runningState Only return campaigns with this running state.
	 * 
	 * @return The set of campaign IDs.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Collection<Campaign> campaignSearch(
			final String requestUsername,
			final String partialCampaignId,
			final String partialCampaignName,
			final String partialDescription,
			final String partialXml,
			final String partialAuthoredBy,
			final DateTime startDate,
			final DateTime endDate,
			final Campaign.PrivacyState privacyState,
			final Campaign.RunningState runningState) 
			throws ServiceException {
		
		try {
			Collection<Object> campaignSqlParameters = new LinkedList<Object>();
			
			String campaignSqlStmt = campaignQueries.getVisibleCampaignSearchSql(
					campaignSqlParameters,
					requestUsername,
					partialCampaignId,
					partialCampaignName,
					partialDescription,
					partialXml,
					partialAuthoredBy,
					startDate,
					endDate,
					privacyState,
					runningState);
						
			Collection<Campaign> campaignResults = 
					UserCampaignServices.instance().getCampaignInformation(
							campaignSqlStmt,
							campaignSqlParameters,
							requestUsername,
							true,
							true);

			return campaignResults;
			
		} catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	

	/**
	 * Verifies that the survey responses as JSONObjects are valid survey
	 * responses for the given campaign.
	 * 
	 * @param username The username of the user that generated these survey
	 * 				   responses.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaign The campaign.
	 * 
	 * @param jsonSurveyResponses The collection of survey responses as 
	 * 							  JSONObjects.
	 * 
	 * @return A list of SurveyResponse objects representing the JSON survey 
	 * 		   responses.
	 * 
	 * @throws ServiceException Thrown if one of the survey responses is
	 * 							malformed.
	 */
	public List<SurveyResponse> getSurveyResponses(
			final String username, final String client, 
			final Campaign campaign, 
			final Collection<JSONObject> jsonSurveyResponses) 
			throws ServiceException {
		
		try {
			List<SurveyResponse> result = new ArrayList<SurveyResponse>(jsonSurveyResponses.size());
			
			for(JSONObject jsonResponse : jsonSurveyResponses) {
				result.add(new SurveyResponse(username, campaign.getId(), client, campaign, jsonResponse));
			}
			
			return result;
		}
		catch(DomainException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates a campaign. The 'request' and 'campaignId' are required; 
	 * however, the remaining parameters may be null indicating that they 
	 * should not be updated.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param xml The new XML for the campaign or null if the XML should not be
	 * 			  updated.
	 * 
	 * @param description The new description for the campaign or null if the
	 * 					  description should not be updated.
	 * 
	 * @param runningState The new running state for the campaign or null if 
	 * 					   the running state should not be updated.
	 * 
	 * @param privacyState The new privacy state for the campaign or null if 
	 * 					   the privacy state should not be updated.
	 * 
	 * @param classesToAdd The collection of classes to associate with the
	 * 					   campaign.
	 * 
	 * @param classesToRemove The collection of classes to disassociate from
	 * 						  the campaign.
	 * 
	 * @param usersAndRolesToAdd A map of usernames to a list of roles that the
	 * 							 users should be granted in the campaign or 
	 * 							 null if no users should be granted any new 
	 * 							 roles.
	 * 
	 * @param usersAndRolesToRemove A map of usernames to a list of roles that
	 * 								should be revoked from the user in the
	 * 								campaign or null if no users should have 
	 * 								any of their roles revoked.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void updateCampaign(final String campaignId, 
			final String xml, final String description, 
			final Campaign.RunningState runningState, 
			final Campaign.PrivacyState privacyState, 
			final Collection<String> classesToAdd, 
			final Collection<String> classesToRemove,
			final Map<String, Set<Campaign.Role>> usersAndRolesToAdd, 
			final Map<String, Set<Campaign.Role>> usersAndRolesToRemove) 
			throws ServiceException {
		
		try {
			campaignQueries.updateCampaign(campaignId, xml, description, runningState, privacyState, classesToAdd, classesToRemove, usersAndRolesToAdd, usersAndRolesToRemove);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
		
	/**
	 * Deletes a campaign and everything associated with it.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void deleteCampaign(final String campaignId) 
			throws ServiceException {
		// First, retrieve the path information for all of the images 
		// associated with this campaign.
		Collection<URL> imageUrls;
		try {
			imageUrls =
				campaignImageQueries.getImageUrlsFromCampaign(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		try {
			campaignQueries.deleteCampaign(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// If the transaction succeeded, delete all of the images from the 
		// disk.
		for(URL imageUrl : imageUrls) {
			imageQueries.deleteImageDiskOnly(imageUrl);
		}
	}

	// ---------------- deprecated methods -----------------------
	/**
	 * Begins with all of the campaigns that exist in the system and then 
	 * removes those that don't match the parameterized criteria. If a  
	 * parameter is null, it is ignored. Therefore, if all parameters are null,
	 * then all campaign IDs are returned.
	 * 
	 * @param partialCampaignId Only return campaigns whose ID contains this
	 * 							value.
	 * 
	 * @param partialCampaignName Only return campaigns whose name contains 
	 * 							  this value.
	 * 
	 * @param partialDescription Only return campaigns whose description 
	 * 							 contains this value.
	 * 
	 * @param partialXml Only return campaigns whose XML contains this value.
	 * 
	 * @param partialAuthoredBy Only return campaigns whose authored by value
	 * 							contains this value.
	 * 
	 * @param startDate Only return campaigns that were created on or after 
	 * 					this date.
	 * 
	 * @param endDate Only return campaigns that were created on or before this
	 * 				  date.
	 * 
	 * @param privacyState Only return campaigns with this privacy state.
	 * 
	 * @param runningState Only return campaigns with this running state.
	 * 
	 * @return The set of campaign IDs.
	 * 
	 * @throws ServiceException There was an error.
	 */
	// deprecated!  Very inefficient!
	public Set<String> xCampaignIdSearch(
			final String partialCampaignId,
			final String partialCampaignName,
			final String partialDescription,
			final String partialXml,
			final String partialAuthoredBy,
			final DateTime startDate,
			final DateTime endDate,
			final Campaign.PrivacyState privacyState,
			final Campaign.RunningState runningState) 
			throws ServiceException {
		
		try {
			Set<String> result = null;
			
			if(partialCampaignId != null) {
				result = new HashSet<String>(
						campaignQueries.getCampaignsFromPartialId(
								partialCampaignId));
			}
			
			if(partialCampaignName != null) {
				List<String> campaignIds =
					campaignQueries.getCampaignsFromPartialName(
							partialCampaignName);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(partialDescription != null) {
				List<String> campaignIds =
					campaignQueries.getCampaignsFromPartialDescription(
							partialDescription);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(partialAuthoredBy != null) {
				List<String> campaignIds =
					campaignQueries.getCampaignsFromPartialAuthoredBy(
							partialAuthoredBy);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(startDate != null) {
				List<String> campaignIds = 
					campaignQueries.getCampaignsOnOrAfterDate(startDate);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(endDate != null) {
				List<String> campaignIds = 
					campaignQueries.getCampaignsOnOrBeforeDate(endDate);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(privacyState != null) {
				List<String> campaignIds =
					campaignQueries.getCampaignsWithPrivacyState(privacyState);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(runningState != null) {
				List<String> campaignIds =
					campaignQueries.getCampaignsWithRunningState(runningState);
				
				if(result == null) {
					result = new HashSet<String>(campaignIds);
				}
				else {
					result.retainAll(campaignIds);
				}
			}
			
			if(result == null) {
				result = new HashSet<String>(
						campaignQueries.getAllCampaignIds());
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
}
