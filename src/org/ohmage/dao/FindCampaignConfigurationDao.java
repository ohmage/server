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
import org.ohmage.domain.Configuration;
import org.ohmage.domain.SurveyMapBuilder;
import org.ohmage.request.AwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * Configuration data access object.
 * 
 * @author selsky
 */
public class FindCampaignConfigurationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindCampaignConfigurationDao.class);
	private SurveyMapBuilder _surveyBuilder;
	private String _sql = "SELECT c.name, c.description, c.xml, crs.running_state, cps.privacy_state, c.creation_timestamp" +
		                  " FROM campaign c, campaign_running_state crs, campaign_privacy_state cps" +
		                  " WHERE urn = ?" +
		                  " AND c.running_state_id = crs.id" +
		                  " AND c.privacy_state_id = cps.id";
	
	public FindCampaignConfigurationDao(DataSource dataSource, SurveyMapBuilder builder) {
		super(dataSource);
		if(null == builder) {
			throw new IllegalArgumentException("a SurveyMapBuilder is required");
		}
		_surveyBuilder = builder;
	}
	
	/**
	 * For the campaign URN found in the awRequest, performs a lookup of the associated campaign configuration from the campaign
	 * table. If a configuration is found, a new Configuration object is set in the awRequest's result list.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		final String urn = awRequest.getCampaignUrn();
		try {
			awRequest.setResultList(getJdbcTemplate().query(_sql, new Object[] {awRequest.getCampaignUrn()}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					String name = rs.getString(1);
					String description = rs.getString(2);
					String xml = rs.getString(3);
					String runningState = rs.getString(4);
					String privacyState = rs.getString(5);
					String timestamp = rs.getTimestamp(6).toString();
					return new Configuration(urn, name, description, 
							runningState, privacyState, timestamp, _surveyBuilder.buildFrom(xml), xml);
				}
		    }));
			
		} catch (org.springframework.dao.DataAccessException dae) {
				
			_logger.error("an exception occurred running the sql '" + _sql + "' " + dae.getMessage());
			throw new DataAccessException(dae);
		}
	}
}
