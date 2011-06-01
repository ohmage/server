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

import org.springframework.jdbc.core.RowMapper;

/**
 * Maps each row from a query ResultSet to a LoginResult. Used by JdbcTemplate in call-back fashion. 
 * 
 * @author selsky
 */
public class AuthenticationResultRowMapper implements RowMapper {
	
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException { // The Spring classes will wrap this exception
		                                                                 // in a Spring DataAccessException
		LoginResult lr = new LoginResult();
		lr.setUserId(rs.getInt(1));
		lr.setEnabled(rs.getBoolean(2));
		lr.setNew(rs.getBoolean(3));
		//lr.setCampaignId(rs.getInt());
		//lr.setCampaignUrn(rs.getString(5));
		//lr.setUserRoleId(rs.getInt(6));
		return lr;
	}
}
