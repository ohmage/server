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
package org.ohmage.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Gets all document IDs or the requesting user for each of the campaigns in 
 * the request with each of the roles in the list.
 * 
 * @author John Jenkins
 */
public class FindAllDocumentsForRequestingUserFromCampaignListWithRolesDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllDocumentsForRequestingUserFromCampaignListWithRolesDao.class);
	
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER = 
		"SELECT distinct(d.uuid) " +
		"FROM user u, campaign c, user_role ur, user_role_campaign urc, " +
			"document d, document_role dr, document_privacy_state dps, document_campaign_role dcar " +
		"WHERE u.login_id = ? " +
		"AND dr.role = ? " +
		"AND c.urn = ? " +
		"AND dcar.document_id = d.id " +
		"AND dcar.document_role_id = dr.id " +
		"AND dcar.campaign_id = c.id " +
		"AND dcar.campaign_id = urc.campaign_id " +
		"AND urc.user_id = u.id " +
		"AND urc.user_role_id = ur.id " +
		"AND (" +
			"(d.privacy_state_id = dps.id " +
			"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "' " +
			"AND (" +
				"(ur.role = '" + CampaignRoleCache.ROLE_ANALYST + "')" +
				" OR " +
				"(ur.role = '" + CampaignRoleCache.ROLE_AUTHOR + "')" +
			"))" +
			" OR " +
			"(ur.role = '" + CampaignRoleCache.ROLE_SUPERVISOR + "')" +
		")";
	
	private List<String> _roles;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param roles The list of roles to use when specifying which document IDs
	 * 				to retrieve.
	 */
	public FindAllDocumentsForRequestingUserFromCampaignListWithRolesDao(DataSource dataSource, List<String> roles) {
		super(dataSource);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The list of roles cannot be null or an empty list.");
		}
		
		_roles = roles;
	}

	/**
	 * Gets the list of document IDs based on the requesting user, each of the
	 * roles in the list, and each of the classes in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of class IDs.
		String campaignIdList;
		try {
			campaignIdList = (String) awRequest.getToProcessValue(InputKeys.CAMPAIGN_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key in toProcess map: " + InputKeys.CAMPAIGN_URN_LIST, e);
		}
		String[] campaignIdArray = campaignIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		Set<String> documentIds = new HashSet<String>();
		// For each of the roles,
		for(String role : _roles) {
			// For each of the campaigns,
			for(int i = 0; i < campaignIdArray.length; i++) {
				// Get the document IDs for this user with the role for the
				// current campaign.
				List<?> currDocumentIds;
				try {
					currDocumentIds = getJdbcTemplate().query(SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER, 
															  new Object[] { awRequest.getUser().getUserName(), role, campaignIdArray[i] }, 
															  new SingleColumnRowMapper());
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER + "' with parameters: " +
							awRequest.getUser().getUserName() + ", " + role + ", " + campaignIdArray[i], e);
					throw new DataAccessException(e);
				}
				
				// Add the new document IDs to the list.
				ListIterator<?> currDocumentIdsIter = currDocumentIds.listIterator();
				while(currDocumentIdsIter.hasNext()) {
					documentIds.add((String) currDocumentIdsIter.next());
				}
			}
		}
		
		awRequest.setResultList(new ArrayList<String>(documentIds));
	}
}
