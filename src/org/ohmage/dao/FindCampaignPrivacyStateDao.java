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
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * @author Joshua Selsky
 */
public class FindCampaignPrivacyStateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindCampaignPrivacyStateDao.class);
	
	private String _sql = "SELECT privacy_state"
		  	             + " FROM campaign c"
		  	             + " WHERE urn = ?";
	
	public FindCampaignPrivacyStateDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Finds the running_state for the campaign URN in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] {awRequest.getCampaignUrn()},
					new SingleColumnRowMapper())
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ awRequest.getCampaignUrn(), dae);
			throw new DataAccessException(dae);
		}
	}
}
