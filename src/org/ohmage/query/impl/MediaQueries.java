package org.ohmage.query.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IImageQueries;
import org.ohmage.query.IMediaQueries;
import org.springframework.jdbc.core.RowMapper;

/**
 * The implementation for the query for media.
 *
 * @author John Jenkins
 * @author Hongsuda T.
 */
public class MediaQueries extends Query implements IMediaQueries {
	private static final Logger LOGGER = Logger.getLogger(MediaQueries.class);
	
	private IImageQueries imageQueries;

	private static final String SQL_EXISTS_MEDIA = 
			"SELECT EXISTS(" +
				"SELECT ubr.uuid " +
				"FROM url_based_resource ubr " +
				"WHERE ubr.uuid = ? )";
	
	private static final String SQL_GET_MEDIA_URL = 
			"SELECT url " +
			"FROM url_based_resource " +
			"WHERE uuid = ?";

	private static final String SQL_GET_MEDIA_URL_AND_METADATA = 
			"SELECT url, metadata " +
			"FROM url_based_resource " +
			"WHERE uuid = ?";

	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private MediaQueries(DataSource dataSource, 
			IImageQueries iImageQueries) {
		super(dataSource);
		
		if(iImageQueries == null) {
			throw new IllegalArgumentException("An instance of IImageQueries is a required argument.");
		}
		this.imageQueries = iImageQueries;

	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IMediaQueries#getMediaExists(java.lang.String)
	 */
	public Boolean getMediaExists(UUID id) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_EXISTS_MEDIA, new Object[] { id.toString() }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_MEDIA + "' with parameter: " + id, e);
		}
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
						SQL_GET_MEDIA_URL, 
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

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IMediaQueries#getMediaUrlAndMetadata(java.lang.String)
	 */
	@Override
	
	public Map<String, String> getMediaUrlAndMetadata(final UUID id) throws DataAccessException {
		try {
			final Map<String, String> result = new HashMap<String, String>();
			
			getJdbcTemplate().queryForObject(
					SQL_GET_MEDIA_URL_AND_METADATA, 
					new Object[] { id.toString() }, 
					new RowMapper<Object> () {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							result.put(rs.getString("url"),	rs.getString("metadata"));
							return null;
						}
					}
				);
			
			return result;
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
			throw new DataAccessException("Error executing SQL '" + SQL_GET_MEDIA_URL_AND_METADATA + "' with parameter: " + id.toString(), e);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IMediaQueries#deleteMediaDiskOnly(java.lang.String, java.lang.boolean)
	 */
	@Override
	public void deleteMediaDiskOnly(URL mediaUrl, boolean isPhoto) {
	    
	    if (isPhoto) {
		imageQueries.deleteImageDiskOnly(mediaUrl);
	    } else { 
		try {
		    // Delete the original image.
		    if((new File(mediaUrl.getFile())).delete()) {
			LOGGER.warn("The media file has been deleted: " + mediaUrl);
		    }
		}
		catch(SecurityException e) {
		    LOGGER.error("The system would not allow us to delete the media.", e);
		}
	    }
	}
}
