package org.ohmage.request.campaign;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.CampaignInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.CampaignValidators.OutputFormat;
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
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
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

	private static final long MILLIS_IN_A_SECOND = 1000;
	
	private final CampaignValidators.OutputFormat outputFormat;
	
	private final List<String> campaignIds;
	private final List<String> classIds;
	
	private final Calendar startDate;
	private final Calendar endDate;
	
	private final CampaignPrivacyStateCache.PrivacyState privacyState;
	private final CampaignRunningStateCache.RunningState runningState;
	
	private final CampaignRoleCache.Role role;
	
	// For short and long reads.
	private Map<CampaignInformation, List<CampaignRoleCache.Role>> shortOrLongResult;
	
	// For XML reads.
	private String xmlResult;
	private String campaignNameResult;
	
	/**
	 * Creates a campaign read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters.
	 */
	public CampaignReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a campaign read reaquest.");
		
		CampaignValidators.OutputFormat tOutputFormat = null;
		
		List<String> tCampaignIds = null;
		List<String> tClassIds = null;
		
		Calendar tStartDate = null;
		Calendar tEndDate = null;
		
		CampaignPrivacyStateCache.PrivacyState tPrivacyState = null;
		CampaignRunningStateCache.RunningState tRunningState = null;
		
		CampaignRoleCache.Role tRole = null;
		
		try {
			tOutputFormat = CampaignValidators.validateOutputFormat(this, httpRequest.getParameter(InputKeys.OUTPUT_FORMAT));
			if(tOutputFormat == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_OUTPUT_FORMAT, "The required output format is missing: " + InputKeys.OUTPUT_FORMAT);
				throw new ValidationException("The required output format is missing: " + InputKeys.OUTPUT_FORMAT);
			}
			else if(httpRequest.getParameterValues(InputKeys.OUTPUT_FORMAT).length > 1) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_OUTPUT_FORMAT, "Multiple output formats were found.");
				throw new ValidationException("Multiple output formats were found.");
			}
			
			tStartDate = CampaignValidators.validateStartDate(this, httpRequest.getParameter(InputKeys.START_DATE));
			if((tStartDate != null) && (httpRequest.getParameterValues(InputKeys.START_DATE).length > 1)) {
				setFailed(ErrorCodes.SERVER_INVALID_DATE, "Multiple start dates were found.");
				throw new ValidationException("Multiple start dates were found.");
			}
			
			tEndDate = CampaignValidators.validateEndDate(this, httpRequest.getParameter(InputKeys.END_DATE));
			if((tStartDate != null) && (httpRequest.getParameterValues(InputKeys.START_DATE).length > 1)) {
				setFailed(ErrorCodes.SERVER_INVALID_DATE, "Multiple end dates were found.");
				throw new ValidationException("Multiple end dates were found.");
			}
			
			// TODO: Should this really be an issue? Should we simply return
			// nothing? There was a GitHub issue, and it was decided that it 
			// was better to send an error to the user than to return nothing.
			LOGGER.info("Verifying that if both the start date and end date are present that the start date isn't after the end date.");
			if((tStartDate != null) && (tEndDate != null) && (tStartDate.after(tEndDate))) {
				setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date cannot be after the end date.");
				throw new ValidationException("The start date cannot be after the end date.");
			}
			
			tCampaignIds = CampaignValidators.validateCampaignIds(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST));
			if(OutputFormat.XML.equals(tOutputFormat)) {
				if(tCampaignIds == null) {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_OUTPUT_FORMAT, "For an output format of '" + OutputFormat.XML.name() + "', exactly one campaign is required.");
					throw new ValidationException("For an output format of '" + OutputFormat.XML.name() + "' exactly one campaign is required.");
				}
				else if(tCampaignIds.size() > 1) {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_OUTPUT_FORMAT, "For an output format of '" + OutputFormat.XML.name() + "', only one campaign ID is allowed.");
					throw new ValidationException("For an output format of '" + OutputFormat.XML.name() + "' only one campaign ID is allowed.");
				}
			}
			else if((tCampaignIds != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Multiple campaign ID lists were found.");
				throw new ValidationException("Multiple campaign ID lists were found.");
			}
			
			tClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if((tClassIds != null) && (httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Multiple class ID lists were found.");
				throw new ValidationException("Multiple class ID lists were found.");
			}
			
			tPrivacyState = CampaignValidators.validatePrivacyState(this, httpRequest.getParameter(InputKeys.PRIVACY_STATE));
			if((tPrivacyState != null) && (httpRequest.getParameterValues(InputKeys.PRIVACY_STATE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_PRIVACY_STATE, "Multiple privacy state parameters were found.");
				throw new ValidationException("Multiple privacy state parameters were found.");
			}
			
			tRunningState = CampaignValidators.validateRunningState(this, httpRequest.getParameter(InputKeys.RUNNING_STATE));
			if((tRunningState != null) && (httpRequest.getParameterValues(InputKeys.RUNNING_STATE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_RUNNING_STATE, "Multiple running state parameters were found.");
				throw new ValidationException("Multiple running state parameters were found.");
			}
			
			tRole = CampaignValidators.validateRole(this, httpRequest.getParameter(InputKeys.USER_ROLE));
			if((tRole != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "Multiple role parameters were found.");
				throw new ValidationException("Multiple role parameters were found.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		outputFormat = tOutputFormat;
		
		campaignIds = tCampaignIds;
		classIds = tClassIds;
		
		startDate = tStartDate;
		endDate = tEndDate;
		
		privacyState = tPrivacyState;
		runningState = tRunningState;
		
		role = tRole;
		
		shortOrLongResult = Collections.emptyMap();
		xmlResult = "";
		campaignNameResult = "";
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
			if(campaignIds != null) {
				LOGGER.info("Verifying that all of the campaigns exist and that the user belongs to them in some capacity.");
				UserCampaignServices.campaignsExistAndUserBelongs(this, campaignIds, getUser().getUsername());
			}
			
			if(OutputFormat.SHORT.equals(outputFormat) || OutputFormat.LONG.equals(outputFormat)) {
				if(classIds != null) {
					LOGGER.info("Verifying that all of the classes exist and that the user belongs to them in some capacity.");
					UserClassServices.classesExistAndUserBelongs(this, classIds, getUser().getUsername());
				}
				
				LOGGER.info("Generating the list of campaign IDs based on the parameters.");
				List<String> resultCampaignIds = UserCampaignServices.getCampaignsForUser(this, getUser().getUsername(), 
						campaignIds, classIds, startDate, endDate, privacyState, runningState, role);
				
				LOGGER.info("Gathering the information about the campaigns.");
				shortOrLongResult = UserCampaignServices.getCampaignAndUserRolesForCampaigns(this, getUser().getUsername(), resultCampaignIds, OutputFormat.LONG.equals(outputFormat));
			}
			else if(OutputFormat.XML.equals(outputFormat)) {
				LOGGER.info("Gathering the XML for the campaign.");
				xmlResult = CampaignServices.getCampaignXml(this, campaignIds.get(0));
				
				LOGGER.info("Gathering the name of the campaign.");
				campaignNameResult = CampaignServices.getCampaignName(this, campaignIds.get(0));
			}
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
		catch(DataAccessException e) {
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
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
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
				CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
			}
		}
		
		// Set the response's content type to "text/html" and if it is a
		// successful read, it will change it to whatever is appropriate.
		httpResponse.setContentType("text/html");
		
		String responseText;
		if(isFailed()) {
			// If it failed, get the failure message.
			responseText = getFailureMessage();
		}
		else {
			// If it has succeeded thus far, set the return value based on the
			// type of request.
			if(OutputFormat.SHORT.equals(outputFormat) || OutputFormat.LONG.equals(outputFormat)) {
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
					for(CampaignInformation campaign : shortOrLongResult.keySet()) {
						// Get the campaign's ID for the metadata.
						resultCampaignIds.add(campaign.getId());
						
						List<CampaignRoleCache.Role> roles = shortOrLongResult.get(campaign);
						boolean supervisorOrAuthor = 
							roles.contains(CampaignRoleCache.Role.SUPERVISOR) || 
							roles.contains(CampaignRoleCache.Role.AUTHOR);
						
						// Create the JSONObject response. This may return null
						// if there is an error building it.
						JSONObject resultJson = campaign.toJson(
								false,	// ID 
								longOutput,	// Classes
								longOutput,	// Any roles
								supervisorOrAuthor,	// Participants
								supervisorOrAuthor, // Analysts
								true,				// Authors
								supervisorOrAuthor,	// Supervisors
								longOutput);// XML
						
						if(resultJson != null) {
							resultJson.put(JSON_KEY_USER_ROLES, roles);
						}
						
						campaignInfo.accumulate(campaign.getId(), resultJson);
					}
					
					metadata.put("number_of_results", resultCampaignIds.size());
					metadata.put("items", resultCampaignIds);
					
					result.put("metadata", metadata);
					result.put("data", campaignInfo);
					
					responseText = result.toString();
				}
				catch(JSONException e) {
					// If anything fails, return a failure message.
					responseText = getFailureMessage();
				}
			}
			else if(OutputFormat.XML.equals(outputFormat)) {
				// Set the type and force the browser to download it as the 
				// last step before beginning to stream the response.
				httpResponse.setContentType("text/xml");
				httpResponse.setHeader("Content-Disposition", "attachment; filename=" + campaignNameResult + ".xml");
				
				responseText = xmlResult;
			}
			else {
				responseText = getFailureMessage();
			}
		}
			
		// Write the error response.
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			LOGGER.error("Unable to write failed response message. Aborting.", e);
			return;
		}
		
		// Flush it and close.
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			LOGGER.error("Unable to flush or close the writer.", e);
		}
	}
	
	/**
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> auditInfo = new HashMap<String, String[]>();
		
		// Retrieve all of the campaign IDs from the result.
		List<String> campaignIds = new LinkedList<String>();
		for(CampaignInformation campaign : shortOrLongResult.keySet()) {
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