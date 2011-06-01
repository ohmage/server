/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.service;

import java.util.List;

import org.ohmage.domain.UserRole;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


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
		List<UserRole> userRoles = awRequest.getUser().getCampaignUserRoleMap().get(awRequest.getCampaignUrn()).getUserRoles();
		
		for(UserRole ur : userRoles) {
			if(_allowedRoles.contains(ur.getRole())) {
				return;
			}
		}
		
		_failedRequestAnnotator.annotate(awRequest, "user " + awRequest.getUser().getUserName() + " is not one of the " +
			"following roles: " + _allowedRoles);
	}
}
