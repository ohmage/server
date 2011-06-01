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
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.UserRoleImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


/**
 * Dao for retrieving the roles out of the user_role table.
 * 
 * @author selsky
 */
public class UserRoleDao implements ParameterLessDao {
	private JdbcTemplate _jdbcTemplate;
	private String _sql = "select id, role from user_role";
	private static Logger logger = Logger.getLogger(UserRoleDao.class);
	
	/**
	 * @throws IllegalArgumentException if the provided DataSource is null
	 */
	public UserRoleDao(DataSource dataSource) {
		_jdbcTemplate = new JdbcTemplate(dataSource); 
	}
	
	/**
	 * @return a list of all of the user roles found in the database
	 */
	@Override
	public List<?> execute() {
		try {
			
			return _jdbcTemplate.query(_sql, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new UserRoleImpl(rs.getInt(1), rs.getString(2));
				}
			});
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			logger.error("an error occurred when attempting to run the following SQL: " + _sql);
			throw new DataAccessException(dae.getMessage());
		
		}
	}
}
