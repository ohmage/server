package org.ohmage.query.impl;

import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserMediaQueries;

/**
 * The implementation of the query for user's media.
 *
 * @author John Jenkins
 */
public class UserMediaQueries extends Query implements IUserMediaQueries {
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private UserMediaQueries(DataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserMediaQueries#getMediaOwner(java.lang.String)
	 */
	@Override
	public String getMediaOwner(
			final UUID id)
			throws DataAccessException {
		
		String sql =
			"SELECT u.username " +
			"FROM user u, url_based_resource ubr " +
			"WHERE ubr.uuid = ? " +
			"AND ubr.user_id = u.id";
		
		try {
			return 
				getJdbcTemplate().queryForObject(
					sql,
					new Object[] { id.toString() }, 
					String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
					"More than one media have the same ID: " + id.toString(),
					e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sql + 
					"' with parameter: " + 
					id.toString(),
				e);
		}
	}

}
