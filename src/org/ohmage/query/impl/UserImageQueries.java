/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserImageQueries;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class is responsible for the functionality to create, read, update, and
 * delete user-image associations.
 * 
 * @author John Jenkins
 */
public final class UserImageQueries extends Query implements IUserImageQueries {
	// Returns a boolean representing whether or not some photo prompt response
	// exists whose response value is the same as some photo's ID.
	private static final String SQL_EXISTS_IMAGE_FOR_USER_IN_RESPONSE =
		"SELECT EXISTS(" +
			"SELECT pr.response " +
			"FROM user u, prompt_response pr, survey_response sr " +
			"WHERE u.username = ? " +
			"AND pr.response = ? " +
			"AND pr.prompt_type = 'photo' " +
			"AND pr.survey_response_id = sr.id " +
			"AND sr.user_id = u.id" +
		")";
	
	// Retrieves the name of the user that created the image.
	private static final String SQL_GET_IMAGE_OWNER =
		"SELECT u.username " +
		"FROM user u, url_based_resource ubr " +
		"WHERE ubr.uuid = ? " +
		"AND ubr.user_id = u.id";
	
	// Retrieves the URL for each image associated with a user across all 
	// survey responses for all campaigns.
	private static final String SQL_GET_URLS_FOR_ALL_IMAGE_RESPONSES_FOR_USER =
		"SELECT ubr.url " +
		"FROM user u, survey_response sr, prompt_response pr, url_based_resource ubr " +
		"WHERE u.username = ? " +
		"AND u.id = sr.user_id " +
		"AND sr.id = pr.survey_response_id " +
		"AND pr.prompt_type = 'photo' " +
		"AND pr.response = ubr.uuid";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserImageQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Returns whether or not a photo prompt response exists for some user 
	 * whose response value is the photo's ID.
	 *  
	 * @param username The username of the user.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Whether or not a photo prompt response exists for some user
	 * 		   whose response value is the photo's ID.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public Boolean responseExistsForUserWithImage(String username, UUID imageId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_EXISTS_IMAGE_FOR_USER_IN_RESPONSE, new Object[] { username, imageId.toString() }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_IMAGE_FOR_USER_IN_RESPONSE + "' with parameters: " + 
					username + ", " + imageId, e);
		}
	}

	/**
	 * Retrieves the username of the user that created this image.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Returns the creator of the image or null if the image doesn't
	 * 		   exist.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public String getImageOwner(UUID imageId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_GET_IMAGE_OWNER, new Object[] { imageId.toString() }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("More than one image has the same ID.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_OWNER + "' with parameter: " + imageId, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserImageQueries#getImageUrlsFromUsername(java.lang.String)
	 */
	@Override
	public Collection<URL> getImageUrlsFromUsername(String username)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_URLS_FOR_ALL_IMAGE_RESPONSES_FOR_USER, 
					new Object[] { username }, 
					new RowMapper<URL>() {
						/**
						 * Converts the URL string to a URL object.
						 */
						@Override
						public URL mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							try {
								return new URL(rs.getString("url"));
							}
							catch(MalformedURLException e) {
								throw new SQLException(
									"The URL in the database is malformed: " +
										rs.getString("url"));
							}
						}
					});
			
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_URLS_FOR_ALL_IMAGE_RESPONSES_FOR_USER + 
						"' with parameter: " + 
						username, 
					e);
		}
	}
}
