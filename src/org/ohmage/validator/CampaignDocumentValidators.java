package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class contains the functionality for validating campaign-document
 * information.
 * 
 * @author John Jenkins
 */
public class CampaignDocumentValidators {
	private static final Logger LOGGER = Logger.getLogger(CampaignDocumentValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private CampaignDocumentValidators() {}
	
	/**
	 * Validates a list of campaign ID and document role pairs.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param campaignAndRoleList A String representing the campaign ID,  
	 * 							  document role pairs. The pairs should be 
	 * 							  separated by
	 * 							  {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s 
	 * 							  and each pair should have its class ID 
	 * 							  separated from its document role by a 
	 * 							  {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.
	 * 
	 * @return Returns a Map of campaign ID, document role pairs. If the 
	 * 		   campaignAndRoleList is null or whitespace only, null is 
	 * 		   returned. 
	 * 
	 * @throws ValidationException Thrown if the list is malformed, any of the
	 * 							   pairs are malformed, or any of the 
	 * 							   individual values in the pairs are malformed
	 * 							   or missing.
	 */
	public static Map<String, String> validateCampaignIdAndDocumentRoleList(Request request, String campaignAndRoleList) throws ValidationException {
		LOGGER.info("Validating a list of campaign ID and document role pairs.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignAndRoleList)) {
			return null;
		}
		
		Map<String, String> result = new HashMap<String, String>();
		
		String[] campaignAndRoleArray = campaignAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < campaignAndRoleArray.length; i++) {
			String campaignAndRoleString = campaignAndRoleArray[i];
			
			if(! "".equals(campaignAndRoleString.trim())) {
				String[] campaignAndRole = campaignAndRoleString.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				if(campaignAndRole.length != 2) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign ID, document role pair is invalid: " + campaignAndRoleArray[i]);
					throw new ValidationException("The campaign ID, document role pair is invalid: " + campaignAndRoleArray[i]);
				}
				
				String campaignId = CampaignValidators.validateCampaignId(request, campaignAndRole[0]);
				String documentRole = DocumentValidators.validateRole(request, campaignAndRole[1]);
	
				result.put(campaignId, documentRole);
			}
		}
		
		return result;
	}
}