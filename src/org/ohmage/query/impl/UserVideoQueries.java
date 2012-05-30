package org.ohmage.query.impl;

import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserVideoQueries;

public class UserVideoQueries extends Query implements IUserVideoQueries {
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private UserVideoQueries(DataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserVideoQueries#getVideoOwner(java.util.UUID)
	 */
	@Override
	public String getVideoOwner(
			final UUID videoId)
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
					new Object[] { videoId.toString() }, 
					String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
					"More than one video has the same ID.",
					e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + 
					sql + 
					"' with parameter: " + 
					videoId.toString(),
				e);
		}
	}

}
