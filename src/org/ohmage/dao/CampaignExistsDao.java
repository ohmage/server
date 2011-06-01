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


/**
 * Checks to make sure that the campaign exists.
 * 
 * @author John Jenkins
 */
public class CampaignExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignExistsDao.class);
	
	private static final String SQL = "SELECT EXISTS(SELECT * " +
									  "FROM campaign " +
									  "WHERE urn = ?)";
	
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public CampaignExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to make sure that the campaign in question exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String campaignUrn = awRequest.getCampaignUrn();
		
		try {
			int count = getJdbcTemplate().queryForInt(SQL, new Object[] { campaignUrn });
			
			if(count == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + campaignUrn, dae);
			throw new DataAccessException(dae);
		}
	}
}
