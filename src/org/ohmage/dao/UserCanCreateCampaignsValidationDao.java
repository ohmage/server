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

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Checks that the user has sufficient permissions to create a new campaign.
 * 
 * @author John Jenkins
 */
public class UserCanCreateCampaignsValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserCanCreateCampaignsValidationDao.class);
	
	private static final String SQL = "SELECT campaign_creation_privilege" +
									  " FROM user" +
									  " WHERE login_id=?";
	
	/**
	 * Creates a new DAO to check if the user has permissions to create a new
	 * campaign.
	 * 
	 * @param dataSource The DataSource to run our queries against.
	 */
	public UserCanCreateCampaignsValidationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Checks that the user has sufficient permissions to create a new
	 * campaign.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String username = awRequest.getUser().getUserName();

		@SuppressWarnings("rawtypes")
		List result;
		try {
			result = getJdbcTemplate().query(SQL, new Object[] { username }, new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + username, dae);
			throw new DataAccessException(dae);
		}
		
		if(! ((Boolean) result.get(0))) {
			awRequest.setFailedRequest(true);
		}
	}

}
