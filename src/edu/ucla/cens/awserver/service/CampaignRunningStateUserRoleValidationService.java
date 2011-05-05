package edu.ucla.cens.awserver.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.domain.UserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.ValidatorException;

/**
 * Validation service for checking a campaign running state against a user role.  
 * 
 * @author Joshua Selsky
 */
public class CampaignRunningStateUserRoleValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignRunningStateUserRoleValidationService.class);
	private String _campaignRunningState;
	private List<String> _allowedUserRoles;
	
	public CampaignRunningStateUserRoleValidationService(AwRequestAnnotator annotator, Dao dao, String campaignRunningState, List<String> allowedUserRoles) {
		super(dao, annotator);
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
		_logger.info("checking a user's role in campaign against the running state of that campaign");

		// At this point in the flow, the campaign URN in the request must have been validated, so
		// just grab the running state from the db.
		
		getDao().execute(awRequest);
		
		List<?> results = awRequest.getResultList();
		
		if(results.size() != 1) {
			throw new ValidatorException("expected 1 campaign to be found, but found " + results.size() + " instead");
		}
		
		String runningState = (String) results.get(0);
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("found running_state " + runningState + " for campaign URN " + awRequest.getCampaignUrn());
		}
		
		// Get the logged-in user's roles for the campaign in the request
		Map<String, List<UserRole>> campaignUserRoleMap = awRequest.getUser().getCampaignUserRoleMap();
		List<UserRole> userRoles = campaignUserRoleMap.get(awRequest.getCampaignUrn());
		int numberOfUserRoles = userRoles.size();
		int numberOfAllowedUserRolesNotFound = 0;
		
		if(null == userRoles || userRoles.isEmpty()) {
			throw new ValidatorException("expected to find user roles for campaign, but none were found");
		}
		
		if(runningState.equals(_campaignRunningState)) { _logger.info("hello");
			// now check the roles
			for(UserRole ur : userRoles) {
				if(! _allowedUserRoles.contains(ur.getRole())) {
					numberOfAllowedUserRolesNotFound++;
				}
			}
			
			_logger.info(numberOfUserRoles);
			_logger.info(numberOfAllowedUserRolesNotFound);
			
			if(numberOfAllowedUserRolesNotFound == numberOfUserRoles) {
				getAnnotator().annotate(awRequest, "user does not have sufficient privileges to access a campaign with a " +
					"running_state of " + _campaignRunningState);
			}
		}
	}
}