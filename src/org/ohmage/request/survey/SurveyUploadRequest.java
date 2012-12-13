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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.ohmage.domain.Video;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.DateTimeUtils;
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
public class SurveyUploadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyUploadRequest.class);
	
	// The campaign creation timestamp is stored as a String because it is 
	// never used in any kind of calculation.
	private final String campaignUrn;
	private final DateTime campaignCreationTimestamp;
	private List<JSONObject> jsonData;
	private final Map<String, BufferedImage> imageContentsMap;
	private final Map<String, Video> videoContentsMap;
	
	private Collection<UUID> surveyResponseIds;
	
	/**
	 * Creates a new image upload request.
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
		super(httpRequest, false, null, null);
		
		LOGGER.info("Creating a survey upload request.");

		String tCampaignUrn = null;
		DateTime tCampaignCreationTimestamp = null;
		List<JSONObject> tJsonData = null;
		Map<String, BufferedImage> tImageContentsMap = null;
		Map<String, Video> tVideoContentsMap = null;
		
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
					
					// Make sure it's a valid timestamp
					try {
						tCampaignCreationTimestamp = DateTimeUtils.getDateTimeFromString(t[0]);
					}
					catch(IllegalArgumentException e) {
						setFailed(ErrorCode.SERVER_INVALID_DATE, e.getMessage());
						throw e;
					}
				}
				
				t = parameters.get(InputKeys.SURVEYS);
				if(t == null || t.length != 1) {
					throw new ValidationException(
						ErrorCode.SURVEY_INVALID_RESPONSES, 
						"No value found for 'surveys' parameter or multiple surveys parameters were found.");
				}
				else {
					try {
						tJsonData = CampaignValidators.validateUploadedJson(t[0]);
					}
					catch(IllegalArgumentException e) {
						throw new ValidationException(
							ErrorCode.SURVEY_INVALID_RESPONSES, 
							"The survey responses could not be URL decoded.", e);
					}
				}
				
				tImageContentsMap = new HashMap<String, BufferedImage>();
				t = getParameterValues(InputKeys.IMAGES);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SURVEY_INVALID_IMAGES_VALUE,
						"Multiple images parameters were given: " +
							InputKeys.IMAGES);
				}
				else if(t.length == 1) {
					LOGGER.debug("Validating the BASE64-encoded images.");
					Map<String, BufferedImage> images = 
							SurveyResponseValidators.validateImages(t[0]);
					
					if(images != null) {
						tImageContentsMap.putAll(images);
					}
				}
				
				// Retrieve and validate images and videos.
				List<String> imageIds = new ArrayList<String>();
				tVideoContentsMap = new HashMap<String, Video>();
				Collection<Part> parts = null;
				try {
					// FIXME - push to base class especially because of the ServletException that gets thrown
					parts = httpRequest.getParts();
					for(Part p : parts) {
						String name = p.getName();
						try {
							UUID.fromString(name);
						}
						catch (IllegalArgumentException e) {
							continue;
						}
							
						String contentType = p.getContentType();
						if(contentType.startsWith("image")) {
							imageIds.add(name);
						}
						else if(contentType.startsWith("video/")) {
							tVideoContentsMap.put(
								name, 
								new Video(
									UUID.fromString(name),
									contentType.split("/")[1],
									getMultipartValue(httpRequest, name)));
						}
					}
				}
				catch(ServletException e) {
					LOGGER.info("This is not a multipart/form-post.");
				}
				catch(IOException e) {
					LOGGER.error("cannot parse parts", e);
					setFailed();
					throw new ValidationException(e);
				}
				
				Set<String> stringSet = new HashSet<String>(imageIds);
				
				if(stringSet.size() != imageIds.size()) {
					throw new ValidationException(ErrorCode.IMAGE_INVALID_DATA, "a duplicate image key was detected in the multi-part upload");
				}

				for(String imageId : imageIds) {
					LOGGER.debug("Validating image ID: " + imageId);
					BufferedImage bufferedImage = ImageValidators.validateImageContents(getMultipartValue(httpRequest, imageId));
					if(bufferedImage == null) {
						LOGGER.debug("Image is null.");
						throw new ValidationException(ErrorCode.IMAGE_INVALID_DATA, "The image data is missing: " + imageId);
					}
					else {
						LOGGER.debug("Image is not null.");
					}
					tImageContentsMap.put(imageId, bufferedImage);
					
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("succesfully created a BufferedImage for key " + imageId);
					}
				}
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
			
			LOGGER.info("Verifying that the uploaded survey responses aren't out of date.");
			CampaignServices.instance().verifyCampaignIsUpToDate(campaignUrn, campaignCreationTimestamp);
			
			LOGGER.info("Generating the campaign object.");
			Campaign campaign = CampaignServices.instance().getCampaign(campaignUrn);
			
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
			
			LOGGER.info("Validating that all video prompt responses have their corresponding images attached.");
			SurveyResponseServices.instance().verifyVideosExistForVideoPromptResponses(surveyResponses, videoContentsMap);
			
			LOGGER.info("Inserting the data into the database.");
			List<Integer> duplicateIndexList = 
				SurveyResponseServices.instance().createSurveyResponses(
					getUser().getUsername(), 
					getClient(), 
					campaignUrn, 
					surveyResponses, 
					imageContentsMap,
					videoContentsMap);

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
		
		super.respond(httpRequest, httpResponse, null);
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
