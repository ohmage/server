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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointFunctionQueryAwRequest;
import org.ohmage.request.MediaQueryAwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.request.UserStatsQueryAwRequest;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Near duplicate of FindAllCampaignsForUserDao except this class is to be used in a multi-user (new data point query API) scenario. 
 * 
 * @author selsky
 */
public class FindAllCampaignsForCurrentUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllCampaignsForCurrentUserDao.class);
	
	private String _sql = "SELECT c.urn " +
			              "FROM campaign c, user_role_campaign urc, user u " +
			              "WHERE urc.campaign_id = c.id AND urc.user_id = u.id AND u.username = ?";
	
	public FindAllCampaignsForCurrentUserDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// Hack continued.
		String user;
		if(awRequest instanceof SurveyResponseReadAwRequest) {
			user = ((SurveyResponseReadAwRequest) awRequest).getCurrentUser();
		}
		else if(awRequest instanceof UserStatsQueryAwRequest) {
			user = ((UserStatsQueryAwRequest) awRequest).getUserNameRequestParam();
		}
		else if(awRequest instanceof DataPointFunctionQueryAwRequest) {
			user = ((DataPointFunctionQueryAwRequest) awRequest).getUserNameRequestParam();
		}
		else if(awRequest instanceof MediaQueryAwRequest) {
			user = ((MediaQueryAwRequest) awRequest).getUserNameRequestParam();
		}
		else {
			throw new DataAccessException("Invalid AwRequest for this DAO.");
		}
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] { user },
					new SingleColumnRowMapper())
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter"
				+ ((SurveyResponseReadAwRequest) awRequest).getCurrentUser(), dae);
			throw new DataAccessException(dae);
		}
	}
}
