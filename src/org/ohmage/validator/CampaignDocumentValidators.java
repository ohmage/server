package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.ValidationException;
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
	public static Map<String, DocumentRoleCache.Role> validateCampaignIdAndDocumentRoleList(Request request, String campaignAndRoleList) throws ValidationException {
		LOGGER.info("Validating a list of campaign ID and document role pairs.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignAndRoleList)) {
			return null;
		}
		
		Map<String, DocumentRoleCache.Role> result = new HashMap<String, DocumentRoleCache.Role>();
		
		String[] campaignAndRoleArray = campaignAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < campaignAndRoleArray.length; i++) {
			String campaignAndRoleString = campaignAndRoleArray[i].trim();
			
			if((! StringUtils.isEmptyOrWhitespaceOnly(campaignAndRoleString)) &&
					(! campaignAndRoleString.equals(InputKeys.ENTITY_ROLE_SEPARATOR))) {
				String[] campaignAndRole = campaignAndRoleString.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				if(campaignAndRole.length != 2) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign ID, document role pair is invalid: " + campaignAndRoleString);
					throw new ValidationException("The campaign ID, document role pair is invalid: " + campaignAndRoleString);
				}
				
				String campaignId = CampaignValidators.validateCampaignId(request, campaignAndRole[0].trim());
				if(campaignId == null) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign ID in the campaign ID, document role pair is missing: " + campaignAndRoleString);
					throw new ValidationException("The campaign ID in the campaign ID, document role pair is missing: " + campaignAndRoleString);
				}
				
				DocumentRoleCache.Role documentRole = DocumentValidators.validateRole(request, campaignAndRole[1].trim());
				if(documentRole == null) {
					request.setFailed(ErrorCodes.DOCUMENT_INVALID_ROLE, "The document role in the campaign ID, document role pair is missing: " + campaignAndRoleString);
					throw new ValidationException("The document role in the campaign ID, document role pair is missing: " + campaignAndRoleString);
				}
	
				result.put(campaignId, documentRole);
			}
		}
		
		return result;
	}
}