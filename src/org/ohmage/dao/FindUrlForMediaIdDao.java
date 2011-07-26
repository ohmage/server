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
import org.ohmage.domain.UrlPrivacyState;
import org.ohmage.request.AwRequest;
import org.ohmage.request.MediaQueryAwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * DAO for looking up a URL and its associated privacy state from url_based_resource and survey_response.
 * 
 * @author Joshua Selsky
 */
public class FindUrlForMediaIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindUrlForMediaIdDao.class);
	
	private String _sql = "SELECT url_based_resource.url, survey_response_privacy_state.privacy_state, user.username "
			               + "FROM url_based_resource, survey_response, survey_response_privacy_state, prompt_response, user "
			               + "WHERE url_based_resource.uuid = ? "
			               + "AND prompt_response.response = url_based_resource.uuid "
			               + "AND survey_response.privacy_state_id = survey_response_privacy_state.id "
			               + "AND prompt_response.survey_response_id = survey_response.id " +
			               	 "AND survey_response.user_id = user.id";
	
	public FindUrlForMediaIdDao(DataSource dataSource) {
		super(dataSource);
	}
		
	@Override
	public void execute(AwRequest awRequest) {
		try {
		
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql,
					new Object[] { ((MediaQueryAwRequest) awRequest).getMediaId() }, // FIXME
					new RowMapper() {
					    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					    	return new UrlPrivacyState(rs.getString(1), rs.getString(2), rs.getString(3));
					    }
					})
			);
		
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ ((MediaQueryAwRequest) awRequest).getMediaId() , dae);
			throw new DataAccessException(dae);
		}
	}
}
