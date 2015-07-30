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
package org.ohmage.request.campaign;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Campaign.OutputFormat;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;

/**
 * <p>A request to read information about a campaign or set of campaigns. The
 * optional parameters will limit the resulting list based on their value. For
 * example, if an initial list is given, a running state of running is given, 
 * and a user role of supervisor is given, then the list of campaigns whose
 * information is returned will be the ones from the initial list where the
 * campaign is running and the user is a supervisor.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#OUTPUT_FORMAT}</td>
 *     <td>The output format for the resulting list of campaigns. The different
 *       output formats are as follows:<br />
 *       <ul>
 *         <li>{@value org.ohmage.validator.CampaignValidators.OutputFormat.SHORT}
 *           <ul>
 *             <li>Name</li>
 *             <li>Description</li>
 *             <li>Running state</li>
 *             <li>Privacy state</li>
 *             <li>Creation time stamp</li>
 *             <li>A list of the requesting user's campaign roles</li>
 *           </ul>
 *         </li>
 *         <li>{@value org.ohmage.validator.CampaignValidators.OutputFormat.LONG}
 *           <ul>
 *             <li>Name</li>
 *             <li>Description</li>
 *             <li>Running state</li>
 *             <li>Privacy state</li>
 *             <li>Creation time stamp</li>
 *             <li>A list of the requesting user's campaign roles</li>
 *             <li>The classes to which this campaign is associated</li>
 *             <li>A map of roles to lists of the users in the campaign with
 *               those roles</li>
 *             <li>XML</li>
 *           </ul>
 *         </li>
 *         <li>{@value org.ohmage.validator.CampaignValidators.OutputFormat.XML}
 *           <ul>
 *             <li>The campaign's XML as a file attachment.</li>
 *           </ul>
 *         </li>
 *       </ul></td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN_LIST}</td>
 *     <td>A list of campaign identifiers to begin with. If this parameter is
 *       omitted the initial list will be all of the campaigns to which the 
 *       user is associated. The campaign identifiers should be separated by 
 *       {@link org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s. The user
 *       must be a member of each of the campaigns.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes where any campaigns that aren't associated with 
 *       all of these classes will be omitted from the result. The class 
 *       identifiers should be separated by
 *       {@link org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s. The user 
 *       must be a member of each of the classes.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_NAME_SEARCH}</td>
 *     <td>A space-separated, double-quote-respecting, search term to limit the
 *       results to only those campaigns whose name matches this term.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_DESCRIPTION_SEARCH}</td>
 *     <td>A space-separated, double-quote-respecting, search term to limit the
 *       results to only those campaigns whose description matches this term.
 *       </td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>This will remove all campaigns from the result whose creation 
 *       timestamp is before this date.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>This will remove all campaigns from the result whose creation
 *       timestamp is after this date.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>This will remove all campaigns from the result whose privacy state 
 *       is not this privacy state.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#RUNNING_STATE}</td>
 *     <td>This will remove all campaigns from the result whose running state
 *       is not this running state.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE}</td>
 *     <td>This will remove all campaigns from the result where the user does 
 *       not have this role.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class CampaignReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(CampaignReadRequest.class);

	private static final String JSON_KEY_USER_ROLES = "user_roles";
	
	private final Campaign.OutputFormat outputFormat;
	
	private final List<String> campaignIds;
	private final Collection<String> classIds;
	
	private final Collection<String> nameTokens;
	private final Collection<String> descriptionTokens;
	
	private final DateTime startDate;
	private final DateTime endDate;
	
	private final Campaign.PrivacyState privacyState;
	private final Campaign.RunningState runningState;
	
	private final Campaign.Role role;
	
	// For short and long reads.
	private Map<Campaign, Collection<Campaign.Role>> campaignResults;
	
	// For XML reads.
	//private String xmlResult;
	//private String campaignNameResult;
	
	/**
	 * Creates a campaign read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public CampaignReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, false, TokenLocation.EITHER, null);
		
		Campaign.OutputFormat tOutputFormat = null;
		
		List<String> tCampaignIds = null;
		Set<String> tClassIds = null;
		
		Set<String> tNameTokens = null;
		Set<String> tDescriptionTokens = null;
		
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		
		Campaign.PrivacyState tPrivacyState = null;
		Campaign.RunningState tRunningState = null;
		
		Campaign.Role tRole = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a campaign read reaquest.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.OUTPUT_FORMAT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_OUTPUT_FORMAT, 
							"Multiple output formats were found.");
				}
				else if(t.length == 1) {
					tOutputFormat = 
							CampaignValidators.validateOutputFormat(t[0]);
					
					if(tOutputFormat == null) {
						throw new ValidationException(
								ErrorCode.CAMPAIGN_INVALID_OUTPUT_FORMAT, 
								"The required output format is missing: " + 
									InputKeys.OUTPUT_FORMAT);
					}
				}
				else {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_OUTPUT_FORMAT, 
						"The required output format is missing: " + 
							InputKeys.OUTPUT_FORMAT);
				}
				
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple start dates were found: " +
								InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = CampaignValidators.validateStartDate(t[0]);
				}
				
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE,
							"Multiple end dates were found: " +
								InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = CampaignValidators.validateEndDate(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID, 
							"Multiple campaign ID lists were found: " +
								InputKeys.CAMPAIGN_URN_LIST);
				}
				else if(t.length == 1) {
					tCampaignIds = 
						CampaignValidators.validateCampaignIds(t[0]);
				}
				
				t = getParameterValues(InputKeys.CLASS_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID, 
							"Multiple class ID lists were found: " +
								InputKeys.CLASS_URN_LIST);
				}
				else if(t.length == 1) {
					tClassIds = ClassValidators.validateClassIdList(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_NAME_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_NAME,
							"Multiple campaign name search strings were given: " +
								InputKeys.CAMPAIGN_NAME_SEARCH);
				}
				else if(t.length == 1) {
					tNameTokens = CampaignValidators.validateNameSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_DESCRIPTION_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_DESCRIPTION,
							"Multiple campaign description search strings were given: " +
								InputKeys.CAMPAIGN_DESCRIPTION_SEARCH);
				}
				else if(t.length == 1) {
					tDescriptionTokens = 
							CampaignValidators.validateDescriptionSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.PRIVACY_STATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_PRIVACY_STATE, 
							"Multiple privacy state parameters were found: " +
								InputKeys.PRIVACY_STATE);
				}
				else if(t.length == 1) {
					tPrivacyState = 
							CampaignValidators.validatePrivacyState(t[0]);
				}
				
				t = getParameterValues(InputKeys.RUNNING_STATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_RUNNING_STATE, 
							"Multiple running state parameters were found: " +
								InputKeys.RUNNING_STATE);
				}
				else if(t.length == 1) {
					tRunningState = 
							CampaignValidators.validateRunningState(t[0]);
				}
				
				t = getParameterValues(InputKeys.USER_ROLE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ROLE, 
							"Multiple role parameters were found: " +
								InputKeys.USER_ROLE);
				}
				else if(t.length == 1) {
					tRole = CampaignValidators.validateRole(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		outputFormat = tOutputFormat;
		
		campaignIds = tCampaignIds;
		classIds = tClassIds;
		
		nameTokens = tNameTokens;
		descriptionTokens = tDescriptionTokens;
		
		startDate = tStartDate;
		endDate = tEndDate;
		
		privacyState = tPrivacyState;
		runningState = tRunningState;
		
		role = tRole;
		
		campaignResults = Collections.emptyMap();
		//xmlResult = "";
		//campaignNameResult = "";
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Getting the campaign information.");
			campaignResults =
				UserCampaignServices.instance().getCampaignInformation(
						getUser().getUsername(), 
						campaignIds, 
						classIds,
						nameTokens,
						descriptionTokens,
						startDate, 
						endDate, 
						privacyState, 
						runningState, 
						role, 
						OutputFormat.LONG.equals(outputFormat), 
						OutputFormat.LONG.equals(outputFormat));
			
			// If this is a request for XML and there were no campaigns visible
			// to the user based on the parameters, we need to report that
			// rather than attempt to attach an empty file.
			if(
				OutputFormat.XML.equals(outputFormat) && 
				(campaignResults.size() == 0)) {
				
				throw
					new ServiceException(
						ErrorCode.CAMPAIGN_INVALID_ID,
						"No campaigns were found.");
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with the requested information.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the campaign read request.");
		
		// Creates the writer that will write the response, success or fail.
		Writer writer;
		try {
			writer = 
					new BufferedWriter(
							new OutputStreamWriter(
									getOutputStream(
											httpRequest, 
											httpResponse)));
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
				
		// If available, update the token.
		if(getUser() != null) {
			final String token = getUser().getToken(); 
			if(token != null) {
				CookieUtils.setCookieValue(
						httpResponse, 
						InputKeys.AUTH_TOKEN, 
						token);
			}
		}
		
		String responseText = getFailureMessage();
		if(! isFailed()) {
			// If it has succeeded thus far, set the return value based on the
			// type of request.
			if(OutputFormat.SHORT.equals(outputFormat) || OutputFormat.LONG.equals(outputFormat)) {
				// Set the response's content type to "application/json".
				httpResponse.setContentType("application/json");
				
				try {
					// Create the JSONObject with which to respond.
					JSONObject result = new JSONObject();
					
					// Mark it as successful.
					result.put(JSON_KEY_RESULT, RESULT_SUCCESS);
					
					// Create and add the metadata.
					JSONObject metadata = new JSONObject();
					// Add the information for each of the campaigns into their own
					// JSONObject and add that to the result.
					JSONObject campaignInfo = new JSONObject();
					
					// Get all of the campaign IDs for the metadata.
					Set<String> resultCampaignIds = new HashSet<String>();
					
					// This is done, so we don't have to repeatedly check the
					// same value.
					boolean longOutput = OutputFormat.LONG.equals(outputFormat);
					
					// For each of the campaigns, process its information and
					// place it in its respective object.
					for(Campaign campaign : campaignResults.keySet()) {
						// Get the campaign's ID for the metadata.
						resultCampaignIds.add(campaign.getId());
						
						Collection<Campaign.Role> roles = 
								campaignResults.get(campaign);
						boolean supervisorOrAuthor = 
							roles.contains(Campaign.Role.SUPERVISOR) || 
							roles.contains(Campaign.Role.AUTHOR);
						
						try {
							// Create the JSONObject response. This may return
							// null if there is an error building it.
							JSONObject resultJson =
								campaign
									.toJson(
										false,	// ID 
										true,	// Classes
										longOutput,	// Any roles
										supervisorOrAuthor,	// Participants
										supervisorOrAuthor, // Analysts
										true,				// Authors
										supervisorOrAuthor,	// Supervisors
										longOutput,	// XML
										false);	// Surveys
							
							if(resultJson != null) {
								resultJson.put(JSON_KEY_USER_ROLES, roles);
							}
							
							campaignInfo
								.accumulate(campaign.getId(), resultJson);
						}
						catch(DomainException e) {
							LOGGER
								.error(
									"There was an error generating the campaign mask.",
									e);
							setFailed();
						}
					}
					
					metadata.put("number_of_results", resultCampaignIds.size());
					metadata.put("items", resultCampaignIds);
					
					result.put("metadata", metadata);
					result.put("data", campaignInfo);
					
					responseText = result.toString();
				}
				catch(JSONException e) {
					// Set the response's content type to "application/json".
					httpResponse.setContentType("application/json");
					
					// If anything fails, return a failure message.
					responseText = getFailureMessage();
				}
			}
			else if(OutputFormat.XML.equals(outputFormat)) {
				// Get the singular result.
				Campaign campaign = 
					campaignResults.keySet().iterator().next();
				
				// Set the type and force the browser to download it as the 
				// last step before beginning to stream the response.
				httpResponse.setContentType("text/xml");
				httpResponse
					.setHeader(
						"Content-Disposition",
						"attachment; filename=" + campaign.getName() + ".xml");
				
				try {
					responseText = campaign.getXml();
				}
				catch(DomainException e) {
					LOGGER
						.error(
							"There was an error generating the campaign mask.",
							e);
					setFailed();
				}
			}
			else {
				// Set the response's content type to "application/json".
				httpResponse.setContentType("application/json");
				
				responseText = getFailureMessage();
			}
		}
		if(isFailed()) {
			// Set the response's content type to "application/json".
			httpResponse.setContentType("application/json");
			
			// If it failed, get the failure message.
			responseText = getFailureMessage();
		}
			
		// Write the error response.
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			LOGGER.warn("Unable to write failed response message. Aborting.", e);
		}
		
		// Close it.
		try {
			writer.close();
		}
		catch(IOException e) {
			LOGGER.warn("Unable to close the writer.", e);
		}
	}
	
	/**
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> auditInfo = super.getAuditInformation();
		
		// Retrieve all of the campaign IDs from the result.
		List<String> campaignIds = new LinkedList<String>();
		for(Campaign campaign : campaignResults.keySet()) {
			campaignIds.add(campaign.getId());
		}
		
		// If any campaign IDs were found, add an entry into the audit 
		// information where the key distinguishes this as a result and the
		// value is the listof campaign IDs.
		if(campaignIds.size() > 0) {
			auditInfo.put(InputKeys.CAMPAIGN_URN, campaignIds.toArray(new String[0]));
		}
		
		return auditInfo;
	}
}
