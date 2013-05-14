package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IOmhQueries;
import org.springframework.jdbc.core.RowMapper;

/**
 * Implementing class for the IOmhQueries interface.
 *
 * @author John Jenkins
 */
public class OmhQueries extends Query implements IOmhQueries {
	/**
	 * Creates the OMH queries using reflection.
	 * 
	 * @param dataSource The data source to return.
	 */
	private OmhQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IOmhQueries#getCredentials(java.lang.String)
	 */
	@Override
	public Map<String, String> getCredentials(
			final String domain)
			throws DataAccessException {
		
		String sql = 
			"SELECT auth_key, auth_value " +
			"FROM omh_authentication " +
			"WHERE domain = ?";
		
		try {
			final Map<String, String> result = new HashMap<String, String>();
			
			getJdbcTemplate()
				.query(
					sql,
					new Object[] { domain },
					new RowMapper<Object>() {
						/**
						 * Adds each result row to the result map.
						 */
						@Override
						public Object mapRow(
								final ResultSet rs,
								final int rowNum)
								throws SQLException {
							
							result.put(
								rs.getString("auth_key"),
								rs.getString("auth_value"));
							return null;
						}
						
					}
				);
			
			return result;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sql +
					"' with parameter: " +
					domain,
				e);
		}
	}

}