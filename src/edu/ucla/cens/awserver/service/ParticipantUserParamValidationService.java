package edu.ucla.cens.awserver.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CampaignRoleCache;
import edu.ucla.cens.awserver.domain.CampaignUserRoles;
import edu.ucla.cens.awserver.domain.UserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates that the user_list or user parameter only contains the logged-in user, if the logged-in user is a participant.
 * 
 * @author Joshua Selsky
 */
public class ParticipantUserParamValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(ParticipantUserParamValidationService.class);
	
	public ParticipantUserParamValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking to see if a participant is attempting to query another user's data");
		
		Map<String, CampaignUserRoles> userRoleMap = awRequest.getUser().getCampaignUserRoleMap();
		List<UserRole> rolesInCampaign = userRoleMap.get(awRequest.getCampaignUrn()).getUserRoles();
		
		if(null == rolesInCampaign || rolesInCampaign.isEmpty()) {
			throw new ServiceException("expected user roles to be found for campaign, but none were found -- was the user" +
				" object properly populated?");
		}
		
		if(rolesInCampaign.size() == 1 && CampaignRoleCache.ROLE_PARTICIPANT.equals(rolesInCampaign.get(0).getRole())) {
			String userString = null;
			
			if(null != awRequest.getToValidateValue(InputKeys.USER_LIST)) {
				
				userString = (String) awRequest.getToValidateValue(InputKeys.USER_LIST);
			}
			else if(null != awRequest.getToValidateValue(InputKeys.USER)) {
				
				userString = (String) awRequest.getToValidateValue(InputKeys.USER);
				
			} else {
				
				throw new ServiceException("neither a " + InputKeys.USER_LIST + " nor a " + InputKeys.USER +  " parameter was found" +
					" and one is requried");
			}
			
			if(! awRequest.getUser().getUserName().equals(userString)) {
				getAnnotator().annotate(awRequest, "participants may not run queries against other user's data");
			}
		}
	}
}
