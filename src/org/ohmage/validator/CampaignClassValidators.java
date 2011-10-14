package org.ohmage.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class contains the functionality for validation class-campaign 
 * information.
 * 
 * @author John Jenkins
 */
public class CampaignClassValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private CampaignClassValidators() {}

	/**
	 * Validates a list of class ID and campaign role pairs.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param classesAndRoles A String representing the class ID, campaign role
	 * 						  pairs. The pairs should be separated by
	 * 						  {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s
	 * 						  and each pair should have its class ID separated
	 * 						  from its campaign role by a
	 * 						  {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.
	 *  
	 * @return A map of class IDs to a set of campaign roles.
	 * 
	 * @throws ValidationException Thrown if the list is malformed, any of the
	 * 							   pairs are malformed, or any of the 
	 * 							   individual values in the pairs are malformed
	 * 							   or missing.
	 */
	public static Map<String, Set<Campaign.Role>> validateClassesAndRoles(final Request request, final String classesAndRoles) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(classesAndRoles)) {
			return null;
		}
		
		Map<String, Set<Campaign.Role>> result = new HashMap<String, Set<Campaign.Role>>();
		
		String[] classAndRoleArray = classesAndRoles.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classAndRoleArray.length; i++) {
			String classAndRoleString = classAndRoleArray[i].trim();
			
			if((! StringUtils.isEmptyOrWhitespaceOnly(classAndRoleString)) &&
					(! classAndRoleString.equals(InputKeys.ENTITY_ROLE_SEPARATOR))) {
				String[] classAndRole = classAndRoleString.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				if(classAndRole.length != 2) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class ID, campaign role pair is invalid: " + classAndRoleString);
					throw new ValidationException("The class ID, campaign role pair is invalid: " + classAndRoleString);
				}
				
				String classId = ClassValidators.validateClassId(request, classAndRole[0].trim());
				if(classId == null) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class ID in the class ID, campaign role pair is missing: " + classAndRoleString);
					throw new ValidationException("The class ID in the class ID, campaign role pair is missing: " + classAndRoleString);
				}
				
				Campaign.Role campaignRole = CampaignValidators.validateRole(request, classAndRole[1].trim());
				if(campaignRole == null) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "The campaign role in the class ID, campaign role pair is missing: " + classAndRoleString);
					throw new ValidationException("The campaign role in the class ID, campaign role pair is missing: " + classAndRoleString);
				}
				
				Set<Campaign.Role> roles = result.get(classId);
				if(roles == null) {
					roles = new HashSet<Campaign.Role>();
					roles.add(campaignRole);
				}
				result.put(classId, roles);
			}
		}
		
		return result;
	}
}