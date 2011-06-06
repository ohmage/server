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
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointQueryAwRequest;


/**
 * @author selsky
 */
public class DataPointQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DataPointQueryDao.class);
	
	private String _select = "SELECT pr.prompt_id, pr.prompt_type, pr.response, pr.repeatable_set_iteration, pr.repeatable_set_id,"
			            + " sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id"
	                    + " FROM prompt_response pr, survey_response sr, user u, campaign c"
	                    + " WHERE pr.survey_response_id = sr.id"
	                    + " AND u.username = ?"
	                    + " AND sr.user_id = u.id"
	                    + " AND c.urn = ?"
	                    + " AND c.id = sr.campaign_id"
	                    + " AND sr.msg_timestamp BETWEEN ? AND ?"
	                    + " AND prompt_id in ";
	
	public DataPointQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		DataPointQueryAwRequest req = (DataPointQueryAwRequest) awRequest;
		List<String> metadataPromptIds = req.getMetadataPromptIds();
		String[] promptIds = req.getDataPointIds();
		
		int numberOfMetadataPoints = metadataPromptIds.size();
		int numberOfPromptIds = promptIds.length;
		int totalNumberOfParams = numberOfMetadataPoints + numberOfPromptIds;
		
		StringBuilder builder = new StringBuilder("(");
		for(int i = 0; i < totalNumberOfParams; i++) {
			builder.append("?");
			if(i < totalNumberOfParams - 1) {
				builder.append(",");
			}
		}
		builder.append(")");
		
		final String sql = _select + builder.toString();
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(sql);
		}
		
		final List<Object> paramObjects = new ArrayList<Object>();
		paramObjects.add(req.getUserNameRequestParam());
		paramObjects.add(req.getCampaignUrn());
		paramObjects.add(req.getStartDate());
		paramObjects.add(req.getEndDate());
		
		for(int i = 0; i < numberOfPromptIds; i++) {
			paramObjects.add(promptIds[i]);
		}
		
		for(int i = 0; i < numberOfMetadataPoints; i++) {
			paramObjects.add(metadataPromptIds.get(i));
		}
		
		try {
		
			List<?> results = getJdbcTemplate().query(sql, paramObjects.toArray(), new DataPointQueryRowMapper());
			_logger.info("found " + results.size() + " query results");
			req.setResultList(results);
			
		} catch(org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running the following SQL '" + sql + "' with the parameters: " +
				paramObjects, dae);
			
			throw new DataAccessException(dae);
		}
	}
}
