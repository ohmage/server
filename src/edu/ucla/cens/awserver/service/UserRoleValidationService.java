package edu.ucla.cens.awserver.service;

import java.util.List;

import edu.ucla.cens.awserver.domain.UserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Configurable user role validation to be used in various service workflows. Any flow where the logged-in user's role needs to be 
 * checked against specific roles will find this class useful. The AwRequest will be marked as failed if the logged-in user does
 * not belong to at least one of the roles set on construction. 
 * 
 * @author joshua selsky
 */
public class UserRoleValidationService extends AbstractAnnotatingService {
	private List<String> _allowedRoles;
	private AwRequestAnnotator _failedRequestAnnotator;
	
	/**
	 * @param annotator - required and used to annotate the AwRequest instead of errors
	 * @param roles - required. The String roles to be validated against
	 * @throws IllegalArgumentException if the annotator, userRoleCacheService, or roles are empty or null
	 */
	public UserRoleValidationService(AwRequestAnnotator annotator, List<String> roles) {
		super(annotator);
		if(null == roles || roles.size() < 1) {
			throw new IllegalArgumentException("roles are required");
		}
		_allowedRoles = roles;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		List<UserRole> userRoles = awRequest.getUser().getCampaignUserRoleMap().get(awRequest.getCampaignUrn());
		
		for(UserRole ur : userRoles) {
			if(_allowedRoles.contains(ur.getRole())) {
				return;
			}
		}
		
		_failedRequestAnnotator.annotate(awRequest, "user " + awRequest.getUser().getUserName() + " is not one of the " +
			"following roles: " + _allowedRoles);
	}
}
