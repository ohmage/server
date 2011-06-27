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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.UserRoleCampaignResult;
import org.ohmage.domain.UserRoleImpl;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;


/**
 * DAO for finding a user's campaign roles and campaign metadata for each campaign that a particular user belongs to.
 * 
 * @author Joshua Selsky
 */
public class UserRoleCampaignPopulationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserRoleCampaignPopulationDao.class);
	
	private static final String _selectSql = "SELECT campaign.urn, campaign.name, campaign.description, campaign_running_state.running_state,"
			                                 + " campaign_privacy_state.privacy_state, campaign.creation_timestamp, user_role_campaign.user_role_id,"
			                                 + " user_role.role"
        									 + " FROM campaign, user, user_role_campaign, user_role, campaign_privacy_state, campaign_running_state"
        									 + " WHERE user.username = ?"
        									 +   " AND user.id = user_role_campaign.user_id"
        									 +   " AND campaign.id = user_role_campaign.campaign_id"
        									 +   " AND user_role.id = user_role_campaign.user_role_id"
        									 +	 " AND campaign_privacy_state.id = campaign.privacy_state_id"
	                                         +	 " AND campaign_running_state.id = campaign.running_state_id";

	public UserRoleCampaignPopulationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Populating user-role-campaign connections in user object in awRequest.");
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_selectSql, 
					new String[] {
					    awRequest.getUser().getUserName()
					}, 
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleCampaignResult result = new UserRoleCampaignResult();
							result.setCampaignUrn(rs.getString(1));
							result.setCampaignName(rs.getString(2));
							result.setCampaignDescription(rs.getString(3));
							result.setCampaignRunningState(rs.getString(4));
							result.setCampaignPrivacyState(rs.getString(5));
							result.setCampaignCreationTimestamp(StringUtils.stripMillisFromJdbcTimestampString(rs.getString(6)));
							result.setUserRole(new UserRoleImpl(rs.getInt(7), rs.getString(8)));
							return result;
						}
					}
				)
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" + _selectSql + "' with the following parameters: " + 
					awRequest.getUser().getUserName() + " (password omitted)");
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}
}
