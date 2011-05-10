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
 * Validation service for checking a campaign running state against a list of user roles that are allowed access.  
 * 
 * @author Joshua Selsky
 */
public class CampaignRunningStateUserRoleValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignRunningStateUserRoleValidationService.class);
	private String _campaignRunningState;
	private List<String> _allowedUserRoles;
	
	public CampaignRunningStateUserRoleValidationService(AwRequestAnnotator annotator, String campaignRunningState, List<String> allowedUserRoles) {
		super(annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignRunningState)) {
			throw new IllegalArgumentException("a campaignRunningState is required");
		}
		if(null == allowedUserRoles || allowedUserRoles.isEmpty()) {
			throw new IllegalArgumentException("a list of allowed user roles is required");
		}	
		
		_campaignRunningState = campaignRunningState;
		_allowedUserRoles = allowedUserRoles;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking the user's role in a campaign against the running state of that campaign");

		Map<String, CampaignUserRoles> campaignUserRoleMap = awRequest.getUser().getCampaignUserRoleMap();
		
		if(! campaignUserRoleMap.containsKey(awRequest.getCampaignUrn())) {
			throw new ServiceException("could not locate campaign URN for user - was the user object properly populated with " +
				"all of the user's campaigns?");
		}
		
		List<UserRole> userRoles = campaignUserRoleMap.get(awRequest.getCampaignUrn()).getUserRoles();
		Campaign campaign = campaignUserRoleMap.get(awRequest.getCampaignUrn()).getCampaign();
		
		int numberOfUserRoles = userRoles.size();
		int numberOfAllowedUserRolesNotFound = 0;
		
		if(_campaignRunningState.equals(campaign.getRunningState())) {
			// now check the roles
			for(UserRole ur : userRoles) {
				if(! _allowedUserRoles.contains(ur.getRole())) {
					numberOfAllowedUserRolesNotFound++;
				}
			}
			if(numberOfAllowedUserRolesNotFound == numberOfUserRoles) {
				getAnnotator().annotate(awRequest, "user does not have sufficient privileges to access a campaign with a " +
					"running_state of " + _campaignRunningState);
			}
		}
	}
}