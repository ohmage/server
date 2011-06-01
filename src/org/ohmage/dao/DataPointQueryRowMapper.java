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

import org.ohmage.domain.DataPointQueryResult;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author selsky
 */
public class DataPointQueryRowMapper implements RowMapper {
	
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		DataPointQueryResult result = new DataPointQueryResult();
		result.setPromptId(rs.getString(1));
		result.setPromptType(rs.getString(2));
		result.setResponse(rs.getObject(3));
		
		Object o = rs.getObject(4);
		if(null == o) {
			result.setRepeatableSetIteration(null);	
		} else {
			result.setRepeatableSetIteration(rs.getInt(4));
		}
		
		result.setRepeatableSetId(rs.getString(5));
		result.setTimestamp(rs.getString(6));
		result.setTimezone(rs.getString(7));
		result.setLocationStatus(rs.getString(8));
		result.setLocation(rs.getString(9));
		result.setSurveyId(rs.getString(10));
		return result;
	}
}
