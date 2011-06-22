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
import org.ohmage.request.MediaUploadAwRequest;
import org.ohmage.request.SurveyUploadAwRequest;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Finds the running_state for a campaign.
 * 
 * @author Joshua Selsky
 */
public class CampaignStateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignStateDao.class);
	
	private static final String SQL = "SELECT crs.running_state " +
									  "FROM campaign c, campaign_running_state crs " +
									  "WHERE c.urn = ? " +
									  "AND c.running_state_id = crs.id";
	
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public CampaignStateDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// hack for now until toProcess is utilized

		try {
			
			if(awRequest instanceof SurveyUploadAwRequest) {
				SurveyUploadAwRequest req = (SurveyUploadAwRequest) awRequest;
				req.setCampaignRunningState(
						(String) getJdbcTemplate().queryForObject(SQL, new Object[] { req.getCampaignUrn() }, new SingleColumnRowMapper()));
			} 
			else if (awRequest instanceof MediaUploadAwRequest) {
				MediaUploadAwRequest req = (MediaUploadAwRequest) awRequest;
				req.setCampaignRunningState(
						(String) getJdbcTemplate().queryForObject(SQL, new Object[] { req.getCampaignUrn() }, new SingleColumnRowMapper()));
			}
			else {
				throw new IllegalStateException("This request type is unsupported: " + awRequest.getClass().getName());
				
			}	
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + awRequest.getCampaignUrn());
			throw new DataAccessException(dae);
		}
	}
}
