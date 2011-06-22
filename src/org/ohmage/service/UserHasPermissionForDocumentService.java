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
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.EntityAndRole;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks that the user has sufficient permissions for this document. If the
 * user doesn't have any of the parameterized roles, they must be a privileged
 * user in the class or a supervisor in the campaign.
 * 
 * @author John Jenkins
 */
public class UserHasPermissionForDocumentService extends AbstractAnnotatingService {
	public static Logger _logger = Logger.getLogger(UserHasPermissionForDocumentService.class);
	
	private final Dao _getCampaignIdsAndDocumentRoleDao;
	private final Dao _getClassIdsAndDocumentRoleDao;
	
	private final List<String> _roles;
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to respond with should the user not have
	 * 					sufficient permissions.
	 * 
	 * @param getCampaignIdsAndDocumentRoleDao A DAO for getting all of the
	 * 										   campaigns to which the document
	 * 										   belongs.
	 * 
	 * @param getClassIdsAndDocumentRoleDao A DAO for getting all of the 
	 * 										classes to which the document
	 * 										belongs.
	 * 
	 * @param roles A list of roles the user must have.
	 */
	public UserHasPermissionForDocumentService(AwRequestAnnotator annotator, 
			Dao getCampaignIdsAndDocumentRoleDao, Dao getClassIdsAndDocumentRoleDao, List<String> roles) {
		super(annotator);
		
		if(getCampaignIdsAndDocumentRoleDao == null) {
			throw new IllegalArgumentException("The DAO to get the campaign IDs for the document cannot be null.");
		}
		else if(getClassIdsAndDocumentRoleDao == null) {
			throw new IllegalArgumentException("The DAO to get the class IDs for the document cannot be null.");
		}
		else if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The list of roles cannot be null or empty.");
		}
		
		_getCampaignIdsAndDocumentRoleDao = getCampaignIdsAndDocumentRoleDao;
		_getClassIdsAndDocumentRoleDao = getClassIdsAndDocumentRoleDao;
		
		_roles = roles;
	}

	/** 
	 * Checks the user's highest role for the parameterized document. If it is
	 * null and this check is required, an exception is thrown. If it is set, 
	 * it checks that it is in the list of roles with which this service was 
	 * built.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that the user has the required permission to perform this action on a document.");
		
		String role = awRequest.getUser().getDocumentRole();
		if(role == null) {
			getAnnotator().annotate(awRequest, "The requesting user doesn't have sufficient permissions to perform this action on the document.");
			awRequest.setFailedRequest(true);
			return;
		}
		
		// Validate that the user has the appropriate permission for the 
		// document.
		if(! _roles.contains(role)) {
			_logger.debug("The user doesn't have the required role. Checking the campaigns and classes.");
			
			// Check if the requester is a supervisor in any of the campaigns.
			try {
				_getCampaignIdsAndDocumentRoleDao.execute(awRequest);
				
				ListIterator<?> campaignAndRoleListIter = awRequest.getResultList().listIterator();
				while(campaignAndRoleListIter.hasNext()) {
					EntityAndRole campaignAndRole = (EntityAndRole) campaignAndRoleListIter.next();
					
					if(awRequest.getUser().isSupervisorInCampaign(campaignAndRole.getEntityId())) {
						_logger.debug("The user is a supervisor in the campaign: " + campaignAndRole.getEntityId());
						return;
					}
				}
				
			}
			catch(DataAccessException e) {
				throw new ServiceException(e);
			}
			
			// Check if the requester is privileged in any of the classes.
			try {
				_getClassIdsAndDocumentRoleDao.execute(awRequest);
				
				ListIterator<?> classAndRoleListIter = awRequest.getResultList().listIterator();
				while(classAndRoleListIter.hasNext()) {
					EntityAndRole classAndRole = (EntityAndRole) classAndRoleListIter.next();
					_logger.debug("Checking class: " + classAndRole.getEntityId());
					
					if(awRequest.getUser().isPrivilegedInClass(classAndRole.getEntityId())) {
						_logger.debug("The user is privileged in a class: " + classAndRole.getEntityId());
						return;
					}
				}
				
			}
			catch(DataAccessException e) {
				throw new ServiceException(e);
			}
			
			// If they don't have the role, aren't supervisors in any of the 
			// campaigns to which the document belongs, and aren't privileged
			// in any of the classes to which the document belongs...
			getAnnotator().annotate(awRequest, "The requesting user doesn't have sufficient permissions to perform this action on the document.");
			awRequest.setFailedRequest(true);
		}
	}
}