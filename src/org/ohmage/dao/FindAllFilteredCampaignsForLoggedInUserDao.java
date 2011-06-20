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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.CampaignQueryResult;
import org.ohmage.domain.CampaignUrnUserRole;
import org.ohmage.request.AwRequest;
import org.ohmage.request.CampaignReadAwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * Performs a filtered search on campaigns for the logged-in user based on the details of the 2.2+ Campaign Read API spec.
 * 
 * @author selsky
 */
public class FindAllFilteredCampaignsForLoggedInUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllFilteredCampaignsForLoggedInUserDao.class);
	
	private String _select = "SELECT c.urn, c.name, c.description, c.xml, crs.running_state, cps.privacy_state, c.creation_timestamp " +
			                 "FROM campaign c, campaign_running_state crs, campaign_privacy_state cps " +
			                 "WHERE c.urn = ? " +
			                 "AND c.privacy_state_id = cps.id " +
			                 "AND c.running_state_id = crs.id ";
	
	private String _andPrivacyState = "AND cps.privacy_state = ? ";
	private String _andRunningState = "AND crs.running_state = ? ";
	private String _andStartDate = "AND c.creation_timestamp >= ? ";
	private String _andEndDate = "AND c.creation_timestamp <= ? ";
	private String _orderBy = " ORDER BY c.urn";
	
	public FindAllFilteredCampaignsForLoggedInUserDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(AwRequest awRequest) {
		_logger.info("about to generate SQL and run filtered campaign query");
		
		List<CampaignQueryResult> campaignList = new ArrayList<CampaignQueryResult>();
		
		try {
			
			// The venerable cast to the correct request type
			CampaignReadAwRequest req = (CampaignReadAwRequest) awRequest;
			
			List<CampaignUrnUserRole> campaignUrnUserRoleList = (List<CampaignUrnUserRole>) req.getResultList(); 

			// Convert to a Map for easier traversal and filtering based on whether the query params contain a user role and/or
			// campaign URNs
			Map<String, List<String>> urnToRolesMap = new HashMap<String, List<String>>();
			
			for(CampaignUrnUserRole cuur : campaignUrnUserRoleList) {

				if(req.getCampaignUrnList().isEmpty() || req.getCampaignUrnList().contains(cuur.getUrn())) {
				
					if(! urnToRolesMap.containsKey(cuur.getUrn())) {
						List<String> roleList = new ArrayList<String>();
						urnToRolesMap.put(cuur.getUrn(), roleList);
					}
					
					urnToRolesMap.get(cuur.getUrn()).add(cuur.getRole());
				}
			}
			
			// Now filter based on the user_role provided by the user
			if(null != req.getUserRole()) {
				Iterator<String> urnRoleMapIterator = urnToRolesMap.keySet().iterator();
				while(urnRoleMapIterator.hasNext()) {
					List<String> roles = urnToRolesMap.get(urnRoleMapIterator.next());
					if(! roles.contains(req.getUserRole())) {
						urnRoleMapIterator.remove();
					}
				}
			}
			
			if(urnToRolesMap.isEmpty()) { // nothing to do if there are no campaign URNs to query against
				awRequest.setResultList(Collections.emptyList());
				return;
			}
			
			// clean out the result list before adding objects of a different type to it
			req.getResultList().clear();
			
			Iterator<String> iterator = urnToRolesMap.keySet().iterator();
			
			// Build the SQL for each campaign URN based on further ACL filtering
			while(iterator.hasNext()) {
				
				String urn = iterator.next();
				
				List<Object> pList = new ArrayList<Object>();
				pList.add(urn);
				
				StringBuilder sql = new StringBuilder();
				sql.append(_select);
				
				final List<String> roles = urnToRolesMap.get(urn); 
				
				if(null != req.getPrivacyState()) { // shared, private
					String privacyState = req.getPrivacyState();
					if("private".equals(privacyState)) {
						// analysts cannot view
						if(roles.contains("analyst") && roles.size() == 1) {
							continue;
						}
					}
					sql.append(_andPrivacyState);
					pList.add(privacyState);
				}
				
				if(null != req.getRunningState()) { // running, stopped
					String runningState = req.getRunningState();
					if("stopped".equals(runningState)) {
						// participants cannot view a stopped campaign
						if(roles.contains("participant") && roles.size() == 1) {
							continue;
						}
					}
					sql.append(_andRunningState);
					pList.add(runningState);
				}
				
				if(null != req.getStartDate()) {
					sql.append(_andStartDate);
					pList.add(req.getStartDate());
				}
				
				if(null != req.getEndDate()) {
					sql.append(_andEndDate);
					pList.add(req.getEndDate());
				}
				
				sql.append(_orderBy);
				
				_logger.info("about to run the following SQL: " + sql.toString() + " with the following p list:" + pList);
				
				campaignList.addAll( 
					getJdbcTemplate().query(
						sql.toString(), 
						pList.toArray(),
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								
								CampaignQueryResult result = new CampaignQueryResult();
								result.setUrn(rs.getString(1));
								result.setName(rs.getString(2));
								result.setDescription(rs.getString(3));
								result.setXml(rs.getString(4));
								result.setRunningState(rs.getString(5));
								result.setPrivacyState(rs.getString(6));
								
								String ts = rs.getTimestamp(7).toString();
								
								if(ts.contains(".")) {
									
									int indexOfDot = ts.indexOf(".");
									result.setCreationTimestamp(ts.substring(0, indexOfDot));
									
								} else {
									
									result.setCreationTimestamp(ts);
								}
								
								// the user roles come from the urnRoleMap above
								result.setUserRoles(roles);
								
								return result;
							}
						}
					)
				);
			}
			
			req.setResultList(campaignList);
			_logger.info("found " + campaignList.size() + " query results");
		}	
		
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following some SQL.", dae);
			throw new DataAccessException(dae);
		}
		
		catch (ClassCastException cce) {
			_logger.error("attempted and could not cast to RetrieveCampaignAwRequest");
			throw new DataAccessException(cce);
		}
	}
}
