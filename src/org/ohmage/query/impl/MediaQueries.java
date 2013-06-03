package org.ohmage.query.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IMediaQueries;

/**
 * The implementation for the query for media.
 *
 * @author John Jenkins
 */
public class MediaQueries extends Query implements IMediaQueries {
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private MediaQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IMediaQueries#getMediaUrl(java.lang.String)
	 */
	@Override
	public URL getMediaUrl(final UUID id) throws DataAccessException {
		String sql = 
			"SELECT url " +
			"FROM url_based_resource " +
			"WHERE uuid = ?";
		
		try {
			return
				new URL(
					getJdbcTemplate().queryForObject(
						sql, 
						new Object[] { id.toString() },
						String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
					"Multiple URL-based resources have the same ID: " +
						id.toString(),
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
		catch(MalformedURLException e) {
			throw new DataAccessException(
				"The URL is malformed for the media: " + id.toString(),
				e);
		}
	}

}
