package edu.ucla.cens.awserver.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.Campaign;
import edu.ucla.cens.awserver.domain.CampaignUserRoles;
import edu.ucla.cens.awserver.domain.UserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validation service for checking a campaign privacy state against a list of user roles that are allowed access.  
 * 
 * @author Joshua Selsky
 */
public class CampaignPrivacyStateUserRoleValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignPrivacyStateUserRoleValidationService.class);
	private String _campaignPrivacyState;
	private List<String> _allowedUserRoles;
	
	public CampaignPrivacyStateUserRoleValidationService(AwRequestAnnotator annotator, String campaignPrivacyState, List<String> allowedUserRoles) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignPrivacyState)) {
			throw new IllegalArgumentException("a campaignRunningState is required");
		}
		if(null == allowedUserRoles || allowedUserRoles.isEmpty()) {
			throw new IllegalArgumentException("a list of allowed user roles is required");
		}	
		
		_campaignPrivacyState = campaignPrivacyState;
		_allowedUserRoles = allowedUserRoles;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking the user's role in a campaign against the privacy state of that campaign");
		
		Map<String, CampaignUserRoles> campaignUserRoleMap = awRequest.getUser().getCampaignUserRoleMap();
		
		if(! campaignUserRoleMap.containsKey(awRequest.getCampaignUrn())) {
			throw new ServiceException("could not locate campaign URN for user - was the user object properly populated with " +
				"all of the user's campaigns?");
		}
		
		List<UserRole> userRoles = campaignUserRoleMap.get(awRequest.getCampaignUrn()).getUserRoles();
		Campaign campaign = campaignUserRoleMap.get(awRequest.getCampaignUrn()).getCampaign();
		
		int numberOfUserRoles = userRoles.size();
		int numberOfAllowedUserRolesNotFound = 0;
		
		if(null == userRoles || userRoles.isEmpty()) {
			throw new ServiceException("expected to find user roles for campaign, but none were found");
		}
		
		if(_campaignPrivacyState.equals(campaign.getPrivacyState())) {
			// now check the roles
			for(UserRole ur : userRoles) {
				if(! _allowedUserRoles.contains(ur.getRole())) {
					numberOfAllowedUserRolesNotFound++;
				}
			}
			if(numberOfAllowedUserRolesNotFound == numberOfUserRoles) {
				getAnnotator().annotate(awRequest, "user does not have sufficient privileges to access a campaign with a " +
					"privacy_state of " + _campaignPrivacyState);
			}
		}
	}
}