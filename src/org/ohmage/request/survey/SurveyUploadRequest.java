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
package org.ohmage.request.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Audio;
import org.ohmage.domain.Image;
import org.ohmage.domain.Video;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.DateTimeUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ImageValidators;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Stores a survey and its associated images (if any are present in the payload)</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the uploader.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The password for the associated username/</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The campaign URN for the survey(s) being uploaded.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_TIMESTAMP}</td>
 *     <td>The creation timestamp for the campaign. This parameter is used to
 *     ensure that the client's campaign is up-to-date.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEYS}</td>
 *     <td>The survey data payload for the survey(s) being uploaded.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#IMAGES}</td>
 *     <td>A JSON object where the keys are the image IDs and the values are 
 *       the images' contents BASE64-encoded.</td>
 *     <td>Either this or the deprecated imageId/imageContents 
 *       multipart/form-post method must define all images in the payload.</td>
 *   </tr>
 *   <tr>
 *     <td>The image's ID.</td>
 *     <td>The image's constants.</td>
 *     <td>One for every image in the payload. This is deprecated in favor of
 *       the {@value org.ohmage.request.InputKeys#IMAGES} parameter.</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
// TODO: HT Do we need to add annotation for request.getParts()
public class SurveyUploadRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(SurveyUploadRequest.class);
	
	// The campaign creation timestamp is stored as a String because it is 
	// never used in any kind of calculation.
	private final String campaignUrn;
	private final DateTime campaignCreationTimestamp;
	private List<JSONObject> jsonData;
	private final Map<UUID, Image> imageContentsMap;
	private final Map<String, Video> videoContentsMap;
	private final Map<String, Audio> audioContentsMap;
	private final String owner;
	
	private Collection<UUID> surveyResponseIds;
	
	public SurveyUploadRequest(
		final HttpServletRequest httpRequest,
		final Map<String, String[]> parameters,
		final String campaignId,
		final String data,
		final String owner)
		throws IOException, InvalidRequestException {

		super(httpRequest, false, TokenLocation.PARAMETER, parameters);
		
		String tCampaignUrn = null;
		List<JSONObject> tJsonData = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a survey response upload request.");
			
			try {
				if(campaignId == null) {
					throw
						new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID,
							"The campaign ID was null.");
				}
				if(data == null) {
					throw
						new ValidationException(
							ErrorCode.SURVEY_INVALID_RESPONSES,
							"The list of survey responses was null.");
				}
				
				tCampaignUrn = 
					CampaignValidators.validateCampaignId(campaignId);
				tJsonData = CampaignValidators.validateUploadedJson(data);
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		campaignUrn = tCampaignUrn;
		campaignCreationTimestamp = null;
		jsonData = tJsonData;
		imageContentsMap = Collections.emptyMap();
		videoContentsMap = Collections.emptyMap();
		audioContentsMap = Collections.emptyMap();
		this.owner = owner;
	}
	
	/**
	 * Creates a new survey upload request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public SurveyUploadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, false, TokenLocation.PARAMETER, null);
		
		LOGGER.info("Creating a survey upload request.");

		String tCampaignUrn = null;
		DateTime tCampaignCreationTimestamp = null;
		List<JSONObject> tJsonData = null;
		Map<UUID, Image> tImageContentsMap = null;
		Map<String, Video> tVideoContentsMap = null;
		Map<String, Audio> tAudioContentsMap = null;
		// TODO: HT add Map<String,Document> tDocumentContentsMap = null;
		
		if(! isFailed()) {
			try {
				Map<String, String[]> parameters = getParameters();
				
				// Validate the campaign URN
				String[] t = parameters.get(InputKeys.CAMPAIGN_URN);
				if(t == null || t.length != 1) {
					throw new ValidationException(ErrorCode.CAMPAIGN_INVALID_ID, "campaign_urn is missing or there is more than one.");
				} else {
					tCampaignUrn = CampaignValidators.validateCampaignId(t[0]);
					
					if(tCampaignUrn == null) {
						throw new ValidationException(ErrorCode.CAMPAIGN_INVALID_ID, "The campaign ID is invalid.");
					}
				}
				
				// Validate the campaign creation timestamp
				t = parameters.get(InputKeys.CAMPAIGN_CREATION_TIMESTAMP);
				if(t == null || t.length != 1) {
					throw new ValidationException(ErrorCode.SERVER_INVALID_TIMESTAMP, "campaign_creation_timestamp is missing or there is more than one");
				} 
				else {
					
					// Make sure it's a valid timestamp.
					try {
						tCampaignCreationTimestamp =
							DateTimeUtils.getDateTimeFromString(t[0]);
					}
					catch(IllegalArgumentException e) {
						throw 
							new ValidationException(
								ErrorCode.SERVER_INVALID_DATE,
								e.getMessage(),
								e);
					}
				}
				
				byte[] surveyDataBytes =
					getParameter(httpRequest, InputKeys.SURVEYS);
				if(surveyDataBytes == null) {
					throw new ValidationException(
						ErrorCode.SURVEY_INVALID_RESPONSES, 
						"No value found for 'surveys' parameter or multiple surveys parameters were found.");
				}
				else {
					LOGGER.debug(new String(surveyDataBytes));
					try {
						tJsonData =
							CampaignValidators
								.validateUploadedJson(
									new String(surveyDataBytes, "UTF-8"));
					}
					catch(IllegalArgumentException e) {
						throw new ValidationException(
							ErrorCode.SURVEY_INVALID_RESPONSES, 
							"The survey responses could not be URL decoded.", e);
					}
				}
				
				// Extract images from the post body
				tImageContentsMap = new HashMap<UUID, Image>();
				t = getParameterValues(InputKeys.IMAGES);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SURVEY_INVALID_IMAGES_VALUE,
						"Multiple images parameters were given: " +
							InputKeys.IMAGES);
				}
				else if(t.length == 1) {
					LOGGER.debug("Validating the BASE64-encoded images.");
					Map<UUID, Image> images = 
							SurveyResponseValidators.validateImages(t[0]);
					
					if(images != null) {
						tImageContentsMap.putAll(images);
					}
				}
				
				// TODO: HT add handling for document as inline base64
				
				
				// check for duplicate UUID in the tImageContentsMap 
				if (StringUtils.hasDuplicate(tImageContentsMap.keySet()))
					throw new ValidationException(ErrorCode.SURVEY_DUPLICATE_RESOURCE_UUIDS, 
							"a duplicate uuid of resource key was detected in the multi-part upload");
				
				// Retrieve and validate images and videos.
				//List<UUID> imageIds = new ArrayList<UUID>();
				Set<UUID> resourceIds = new HashSet<UUID>();
				
				tVideoContentsMap = new HashMap<String, Video>();
				tAudioContentsMap = new HashMap<String, Audio>();
				// TODO: HT add tDocumentContentsMap = new HashMap<String, Document>();
				Collection<Part> parts = null;
		
				try {
					// FIXME - push to base class especially because of the ServletException that gets thrown
					parts = httpRequest.getParts();
					for(Part p : parts) {
						UUID id;
						String name = p.getName();
						try {
							id = UUID.fromString(name);
						}
						catch (IllegalArgumentException e) {
							LOGGER.info("Ignoring part: " + name);
							continue;
						}
						
						//imageIds.add(id);  // add all UUID					
						// check for duplicate
						if (!resourceIds.add(id)){
							throw new ValidationException(ErrorCode.SURVEY_DUPLICATE_RESOURCE_UUIDS, 
									"a duplicate uuid of resource key was detected in the multi-part upload");
						}
						String contentType = p.getContentType();
						
						if(contentType.startsWith("image")) {

							Image image = 
									ImageValidators
										.validateImageContents(
											id,
											getMultipartValue(httpRequest, name));
							if(image == null) {
								throw
									new ValidationException(
										ErrorCode.IMAGE_INVALID_DATA, 
										"The image data is missing: " + id);
							}
							tImageContentsMap.put(id, image);														
						}
						else if(contentType.startsWith("video/")) {
							tVideoContentsMap.put(
								name,  // put name instead of id in the map. why? 
								new Video(
									UUID.fromString(name),
									contentType.split("/")[1],
									getMultipartValue(httpRequest, name)));
						}
						else if(contentType.startsWith("audio/")) {
							try {
								tAudioContentsMap.put(
									name,
									new Audio(
										UUID.fromString(name),
										contentType.split("/")[1],
										getMultipartValue(httpRequest, name)));
							}
							catch(DomainException e) {
								throw
									new ValidationException(
										ErrorCode.SYSTEM_GENERAL_ERROR,
										"Could not create the Audio object.",
										e);
							}
						}
						else if(contentType.startsWith("application/") ||
								contentType.startsWith("text/")){ // HT: check this
							try {
								tAudioContentsMap.put(
									name,
									new Audio(
										UUID.fromString(name),
										contentType.split("/")[1],
										getMultipartValue(httpRequest, name)));
							}
							catch(DomainException e) {
								throw
									new ValidationException(
										ErrorCode.SYSTEM_GENERAL_ERROR,
										"Could not create the Audio object.",
										e);
							}	
						}
						if(LOGGER.isDebugEnabled()) 
							LOGGER.debug("succesfully created a BufferedImage for key " + id);
					}
				}
				catch(ServletException e) {
					LOGGER.info("This is not a multipart/form-post.");
				}
				catch(IOException e) {
					LOGGER.error("cannot parse parts", e);
					throw new ValidationException(e);
				}
				catch(DomainException e) {
					LOGGER.info("A Media object could not be built.", e);
					throw new ValidationException(e);
				}
				
				/*
				// HT: the uniqueness should be checked across all types of media, not per media
				Set<UUID> stringSet = new HashSet<UUID>(imageIds);
				
				if(stringSet.size() != imageIds.size()) {
					throw new ValidationException(ErrorCode.SURVEY_DUPLICATE_RESOURCE_UUIDS, "a duplicate uuid of resource key was detected in the multi-part upload");
				}
				for(UUID imageId : imageIds) {
					Image image = 
						ImageValidators
							.validateImageContents(
								imageId,
								getMultipartValue(
									httpRequest, 
									imageId.toString()));
					if(image == null) {
						throw
							new ValidationException(
								ErrorCode.IMAGE_INVALID_DATA, 
								"The image data is missing: " + imageId);
					}
					tImageContentsMap.put(imageId, image);
					
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("succesfully created a BufferedImage for key " + imageId);
					}
				}
				*/
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER, true);
			}

		}

		this.campaignUrn = tCampaignUrn;
		this.campaignCreationTimestamp = tCampaignCreationTimestamp;
		this.jsonData = tJsonData;
		this.imageContentsMap = tImageContentsMap;
		this.videoContentsMap = tVideoContentsMap;
		this.audioContentsMap = tAudioContentsMap;
		this.owner = null;
		
		surveyResponseIds = null;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a survey upload request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is a participant in the campaign.");
			UserCampaignServices.instance().verifyUserCanUploadSurveyResponses(getUser().getUsername(), campaignUrn);
			
			LOGGER.info("Verifying that the campaign is running.");
			CampaignServices.instance().verifyCampaignIsRunning(campaignUrn);
			
			Collection<String> campaignIds = new ArrayList<String>(1);
			campaignIds.add(campaignUrn);
			LOGGER.info("Generating the campaign object.");
			Map<Campaign, Collection<Campaign.Role>> campaigns = 
				UserCampaignServices
					.instance()
						.getCampaignInformation(
							getUser().getUsername(),
							campaignIds,
							null,
							null,
							null,
							null,
							null,
							null,
							null,
							null,
							false,
							false);
			if(campaigns.size() == 0) {
				throw
					new ServiceException(
						ErrorCode.CAMPAIGN_INVALID_ID,
						"The campaign does not exist: " + campaignUrn);
			}
			if(campaigns.size() > 1) {
				throw
					new ServiceException(
						"Multiple campaigns exist with the same ID.");
			}
			Campaign campaign = campaigns.keySet().iterator().next();
			
			if(campaignCreationTimestamp != null) {
				LOGGER.info("Verifying that the uploaded survey responses aren't out of date.");
				CampaignServices.instance().verifyCampaignIsUpToDate(campaign, campaignCreationTimestamp);
			}
			
			LOGGER.info("Verifying the uploaded data against the campaign.");
			List<SurveyResponse> surveyResponses = 
				CampaignServices.instance().getSurveyResponses(
						getUser().getUsername(), 
						getClient(),
						campaign, 
						jsonData);
			
			surveyResponseIds = new ArrayList<UUID>(surveyResponses.size());
			for(SurveyResponse surveyResponse : surveyResponses) {
				surveyResponseIds.add(surveyResponse.getSurveyResponseId());
			}

			LOGGER.info("Validating that all photo prompt responses have their corresponding images attached.");
			SurveyResponseServices.instance().verifyImagesExistForPhotoPromptResponses(surveyResponses, imageContentsMap);
			
			LOGGER.info("Validating that all video prompt responses have their corresponding videos attached.");
			SurveyResponseServices.instance().verifyVideosExistForVideoPromptResponses(surveyResponses, videoContentsMap);
			
			LOGGER.info("Validating that all audio prompt responses have their corresponding audio files attached.");
			SurveyResponseServices.instance().verifyAudioFilesExistForAudioPromptResponses(surveyResponses, audioContentsMap);
			
			// TODO: HT add a validator for ducument type
			
			LOGGER.info("Inserting " + surveyResponses.size() + " survey responses into the database.");
			List<Integer> duplicateIndexList = 
				SurveyResponseServices.instance().createSurveyResponses(
					((owner == null) ? getUser().getUsername() : owner), 
					getClient(), 
					campaignUrn, 
					surveyResponses, 
					imageContentsMap,
					videoContentsMap,
					audioContentsMap);

			LOGGER.info("Found " + duplicateIndexList.size() + " duplicate survey uploads");
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER, true);
		}
	}

	/**
	 * Responds to the image upload request with success or a failure message
	 * that contains a failure code and failure text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the survey upload request.");
		
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
	
	/**
	 * If the upload was successful, this records the UUIDs of the successfully
	 * uploaded survey responses to the audit's extras table.
	 * 
	 * @return The parents audit information with the successfully uploaded
	 * 		   survey responses if the request was successful.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> result = super.getAuditInformation();
		
		if((! isFailed()) && (surveyResponseIds != null)) {
			int numSurveyResponseIdsAdded = 0;
			String[] surveyResponseIdsArray = 
					new String[surveyResponseIds.size()];
			
			for(UUID surveyResponseId : surveyResponseIds) {
				surveyResponseIdsArray[numSurveyResponseIdsAdded] =
						surveyResponseId.toString();
				numSurveyResponseIdsAdded++;
			}
			
			result.put(
					"successfully_uploaded_survey_response_ids", 
					surveyResponseIdsArray);
		}
		
		return result;
	}
}