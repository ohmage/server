package org.ohmage.request.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Reads the information about all of the users in all of the campaigns and
 * classes in the lists. The requester must be a supervisor in all of the
 * campaigns and privileged in all of the classes.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN_LIST}</td>
 *     <td>A list of campaign identifiers where the identifiers are separated 
 *       by {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of class identifiers where the identifiers are separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserReadRequest.class);
	
	private final List<String> campaignIds;
	private final List<String> classIds; 
	
	private Map<String, UserPersonal> result;
	
	/**
	 * Creates a new user read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the required parameters.
	 */
	public UserReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating a user read request.");
		
		List<String> tCampaignIds = null;
		List<String> tClassIds = null;
		
		try {
			tCampaignIds = CampaignValidators.validateCampaignIds(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST));
			if((tCampaignIds != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Multiple campaign ID list parameters were found.");
				throw new ValidationException("Multiple campaign ID list parameters were found.");
			}
			
			tClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if((tClassIds != null) && (httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Multiple class ID list parameters were found.");
				throw new ValidationException("Multiple class ID list parameters were found.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		campaignIds = tCampaignIds;
		classIds = tClassIds;
		
		result = new HashMap<String, UserPersonal>();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			if(campaignIds != null) {
				LOGGER.info("Verifying that all of the campaigns in the list exist.");
				CampaignServices.checkCampaignsExistence(this, campaignIds, true);
				
				LOGGER.info("Verifying that the requester may read the information about hte users in the campaigns.");
				UserCampaignServices.verifyUserCanReadUsersInfoInCampaigns(this, getUser().getUsername(), campaignIds);
				
				LOGGER.info("Gathering the information about the users in the campaigns.");
				result.putAll(UserCampaignServices.getPersonalInfoForUsersInCampaigns(this, campaignIds));
			}
			
			if(classIds != null) {
				LOGGER.info("Verifying that all of the classes in the list exist.");
				ClassServices.checkClassesExistence(this, classIds, true);
				
				LOGGER.info("Verifying that the requester is privileged in all of the classes.");
				UserClassServices.userHasRoleInClasses(this, getUser().getUsername(), classIds, ClassRoleCache.Role.PRIVILEGED);
				
				LOGGER.info("Gathering the information about the users in the classes.");
				result.putAll(UserClassServices.getPersonalInfoForUsersInClasses(this, classIds));
			}
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
	
	/**
	 * Creates a JSONObject from the result object and responds with that 
	 * object.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		JSONObject jsonResult = new JSONObject();
		try {
			for(String username : result.keySet()) {
				UserPersonal personalInfo = result.get(username);
				
				if(personalInfo == null) {
					jsonResult.put(username, new JSONObject());
				}
				else {
					jsonResult.put(username, personalInfo.toJsonObject());
				}
			}
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the respons object.");
			setFailed();
		}
		super.respond(httpRequest, httpResponse, jsonResult);
	}
}