package edu.ucla.cens.awserver.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.UserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validation service for checking a campaign privacy state against a list of user roles that are allowed access.  
 * 
 * @author Joshua Selsky
 */
public class CampaignPrivacyStateUserRoleValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignPrivacyStateUserRoleValidationService.class);
	private String _campaignPrivacyState;
	private List<String> _allowedUserRoles;
	
	public CampaignPrivacyStateUserRoleValidationService(AwRequestAnnotator annotator, Dao dao, String campaignPrivacyState, List<String> allowedUserRoles) {
		super(dao, annotator);
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
		_logger.info("checking a user's role in campaign against the running state of that campaign");

		// At this point in the flow, the campaign URN in the request must have been validated, so
		// just grab the running state from the db.
		
		getDao().execute(awRequest);
		
		List<?> results = awRequest.getResultList();
		
		if(results.size() != 1) {
			throw new ServiceException("expected 1 campaign to be found, but found " + results.size() + " instead");
		}
		
		String privacyState = (String) results.get(0);
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("found privacy_state " + privacyState + " for campaign URN " + awRequest.getCampaignUrn());
		}
		
		// Get the logged-in user's roles for the campaign in the request
		Map<String, List<UserRole>> campaignUserRoleMap = awRequest.getUser().getCampaignUserRoleMap();
		List<UserRole> userRoles = campaignUserRoleMap.get(awRequest.getCampaignUrn());
		int numberOfUserRoles = userRoles.size();
		int numberOfAllowedUserRolesNotFound = 0;
		
		if(null == userRoles || userRoles.isEmpty()) {
			throw new ServiceException("expected to find user roles for campaign, but none were found");
		}
		
		if(privacyState.equals(_campaignPrivacyState)) {
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