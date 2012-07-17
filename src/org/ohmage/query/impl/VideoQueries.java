package org.ohmage.query.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IVideoQueries;

public class VideoQueries extends Query implements IVideoQueries {
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private VideoQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IVideoQueries#getVideoUrl(java.util.UUID)
	 */
	@Override
	public URL getVideoUrl(final UUID videoId) throws DataAccessException {
		String sql = 
			"SELECT url " +
			"FROM url_based_resource " +
			"WHERE uuid = ?";
		
		try {
			return
				new URL(
					getJdbcTemplate().queryForObject(
						sql, 
						new Object[] { videoId.toString() },
						String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
					"Multiple URL-based resources have the same UUID.",
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
		catch(MalformedURLException e) {
			throw new DataAccessException(
				"The URL is malformed for video: " + videoId.toString(),
				e);
		}
	}

}
