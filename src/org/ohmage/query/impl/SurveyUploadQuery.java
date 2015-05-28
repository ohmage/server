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

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.cache.MediaDirectoryCache;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.Audio;
import org.ohmage.domain.IMedia;
import org.ohmage.domain.Image;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.Media;
import org.ohmage.domain.Video;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.RepeatableSetResponse;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.prompt.PhotoPrompt.NoResponseMedia;
import org.ohmage.domain.campaign.response.AudioPromptResponse;
import org.ohmage.domain.campaign.response.DocumentPromptResponse;
import org.ohmage.domain.campaign.response.MultiChoiceCustomPromptResponse;
import org.ohmage.domain.campaign.response.PhotoPromptResponse;
import org.ohmage.domain.campaign.response.VideoPromptResponse;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ISurveyUploadQuery;
import org.ohmage.service.MediaServices;
import org.ohmage.util.DateTimeUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Persists a survey upload (potentially containing many surveys) into the db.
 * 
 * @author Joshua Selsky
 */
public class SurveyUploadQuery extends AbstractUploadQuery implements ISurveyUploadQuery {
	
	public static final String IMAGE_STORE_FORMAT = "jpg";
	public static final String IMAGE_SCALED_EXTENSION = "-s";
	
	private static final Logger LOGGER = 
		Logger.getLogger(SurveyUploadQuery.class);
	
	private static final String SQL_INSERT_SURVEY_RESPONSE =
		"INSERT into survey_response " +
		"SET uuid = ?, " +
		"user_id = (SELECT id from user where username = ?), " +
		"campaign_id = (SELECT id from campaign where urn = ?), " +
		"epoch_millis = ?, " +
		"phone_timezone = ?, " +
		"location_status = ?, " +
		"location = ?, " +
		"survey_id = ?, " +
		"survey = ?, " +
		"client = ?, " +
		"upload_timestamp = ?, " +
		"launch_context = ?, " +
		"privacy_state_id = (SELECT id FROM survey_response_privacy_state WHERE privacy_state = ?)";
		
	private static final String SQL_INSERT_PROMPT_RESPONSE =
		"INSERT into prompt_response " +
        "(survey_response_id, repeatable_set_id, repeatable_set_iteration," +
        "prompt_type, prompt_id, response) " +
        "VALUES (?,?,?,?,?,?)";
	
	// Inserts an images/media information into the url_based_resource table.
	private static final String SQL_INSERT_IMAGE = 
		"INSERT INTO url_based_resource(user_id, client, uuid, url) " +
		"VALUES (" +
			"(" +	// user_id
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			")," +
			"?," +	// client
			"?," +	// uuid
			"?" +	// url
		")";
	
	// Inserts an images/media information into the url_based_resource table.
	private static final String SQL_INSERT_MEDIA = 
		"INSERT INTO url_based_resource(user_id, client, uuid, url, metadata) " +
		"VALUES (" +
			"(" +	// user_id
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), " +
			"?, " +	// client
			"?, " +	// uuid
			"?, " +	// url
			"?" +   // metadata
		")";

	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private SurveyUploadQuery(DataSource dataSource) {
		super(dataSource);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ISurveyUploadQuery#insertSurveys(java.lang.String, java.lang.String, java.lang.String, java.util.List, java.util.Map, java.util.Map)
	 */
	@Override
	public List<Integer> insertSurveys(
			final String username,
			final String client,
			final String campaignUrn,
			final List<SurveyResponse> surveyUploadList,
			final Map<UUID, Image> bufferedImageMap,
			final Map<UUID, Video> videoContentsMap,
			final Map<UUID, Audio> audioContentsMap,
			final Map<UUID, Media> documentContentsMap)
			throws DataAccessException {
		
		List<Integer> duplicateIndexList = new ArrayList<Integer>();
		int numberOfSurveys = surveyUploadList.size();
		
		// The following variables are used in logging messages when errors occur
		SurveyResponse currentSurveyResponse = null;
		PromptResponse currentPromptResponse = null;
		String currentSql = null;

		List<File> fileList = new LinkedList<File>();  // keep track of files created along the process
		
		// Wrap all of the inserts in a transaction 
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("survey upload");
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		// Use a savepoint to handle nested rollbacks if duplicates are found
		// Object savepoint = status.createSavepoint();
		
		try { // handle TransactionExceptions
			
			for(int surveyIndex = 0; surveyIndex < numberOfSurveys; surveyIndex++) { 
				
				 try { // handle DataAccessExceptions
					
					final SurveyResponse surveyUpload = surveyUploadList.get(surveyIndex);
					currentSurveyResponse = surveyUpload; 
					currentSql = SQL_INSERT_SURVEY_RESPONSE;
			
					KeyHolder idKeyHolder = new GeneratedKeyHolder();
					
					// HT: Don't need this. If one fails, all fail. Rollback completely. 
					// savepoint = status.createSavepoint();  
					
					// First, insert the survey
					getJdbcTemplate().update(
						new PreparedStatementCreator() {
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps 
									= connection.prepareStatement(SQL_INSERT_SURVEY_RESPONSE, Statement.RETURN_GENERATED_KEYS);
								
								String locationString = null;
								Location location = surveyUpload.getLocation();
								if(location != null) {
									try {
										locationString = 
												location.toJson(false, LocationColumnKey.ALL_COLUMNS).toString();
									}
									catch(JSONException e) {
										throw new SQLException(e);
									}
									catch(DomainException e) {
										throw new SQLException(e);
									}
								}
								
								ps.setString(1, surveyUpload.getSurveyResponseId().toString());
								ps.setString(2, username);
								ps.setString(3, campaignUrn);
								ps.setLong(4, surveyUpload.getTime());
								ps.setString(5, surveyUpload.getTimezone().getID());
								ps.setString(6, surveyUpload.getLocationStatus().toString());
								ps.setString(7, locationString);
								ps.setString(8, surveyUpload.getSurvey().getId());
								try {
									ps.setString(9, surveyUpload.toJson(false, false, false, false, true, true, true, true, true, false, false, true, true, true, true, false, false).toString());
								}
								catch(JSONException e) {
									throw new SQLException(
											"Couldn't create the JSON.",
											e);
								}
								catch(DomainException e) {
									throw new SQLException(
											"Couldn't create the JSON.",
											e);
								}
								ps.setString(10, client);
								ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
								try {
									ps.setString(12, surveyUpload.getLaunchContext().toJson(true).toString());
								}
								catch(JSONException e) {
									throw new SQLException(
											"Couldn't create the JSON.",
											e);
								}
								try {
									ps.setString(13, PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE));
								} catch (CacheMissException e) {
									throw new SQLException(
											"Error reading from the cache.", 
											e);
								}
								return ps;
							}
						},
						idKeyHolder
					);
					
					
					final Number surveyResponseId = idKeyHolder.getKey(); // the primary key on the survey_response table for the 
					                                                      // just-inserted survey
					currentSql = SQL_INSERT_PROMPT_RESPONSE;
					
					// Now insert each prompt response from the survey
					Collection<Response> promptUploadList = surveyUpload.getResponses().values();
					
					createPromptResponse(
						username,
						client,
						surveyResponseId,
						fileList,
						promptUploadList,
						null,
						bufferedImageMap,
						videoContentsMap,
						audioContentsMap,
						documentContentsMap,
						transactionManager,
						status);
					
				} catch (DataIntegrityViolationException dive) { // a unique index exists only on the survey_response table
					
					if(isDuplicate(dive)) {
						 
						LOGGER.debug("Found a duplicate survey upload message for user " + username);				
						duplicateIndexList.add(surveyIndex);  // assume successful upload
						// status.rollbackToSavepoint(savepoint);
						
					} 
					else {
					
						// Some other integrity violation occurred - bad!! All 
						// of the data to be inserted must be validated before 
						// this query runs so there is either missing validation 
						// or somehow an auto_incremented key has been duplicated.
						
						LOGGER.error("Caught DataAccessException", dive);
						logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
						for(File f : fileList) {
							f.delete();
						}
						rollback(transactionManager, status);
						throw new DataAccessException(dive);
					}
						
				} catch (org.springframework.dao.DataAccessException|
						DataAccessException dae) { 
					// Some other database problem happened that prevented
                    // the SQL from completing normally. 
					// Or something is wrong with createPromptResponse e.g. duplicate UUID	
					LOGGER.error("caught DataAccessException", dae);
					logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
					for(File f : fileList) {
						f.delete();
					}
					rollback(transactionManager, status);
					throw new DataAccessException(dae);
				}
				
			} // for surveyIndex
			
			// Finally, commit the transaction
			transactionManager.commit(status);
			LOGGER.info("Completed survey message persistence");
		} 
		
		catch (TransactionException te) { 
			
			LOGGER.error("failed to commit survey upload transaction, attempting to rollback", te);
			rollback(transactionManager, status);
			for(File f : fileList) {
				f.delete();
			}
			logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
			throw new DataAccessException(te);
		}
		
		LOGGER.info("Finished inserting survey responses and any associated images into the database and the filesystem.");
		return duplicateIndexList;
	}
	
	/**
	 * Attempts to rollback a transaction. 
	 */
	private void rollback(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus) 
		throws DataAccessException {
		
		try {
			
			LOGGER.error("rolling back a failed survey upload transaction");
			transactionManager.rollback(transactionStatus);
			
		} catch (TransactionException te) {
			
			LOGGER.error("failed to rollback survey upload transaction", te);
			throw new DataAccessException(te);
		}
	}
	
	private void logErrorDetails(SurveyResponse surveyResponse, PromptResponse promptResponse, String sql, String username,
			String campaignUrn) {
	
		StringBuilder error = new StringBuilder();
		error.append("\nAn error occurred when attempting to insert survey responses for user ");
		error.append(username);
		error.append(" in campaign ");
		error.append(campaignUrn);
		error.append(".\n");
		error.append("The SQL statement at hand was ");
		error.append(sql);
		error.append("\n The survey response at hand was ");
		error.append(surveyResponse);
		error.append("\n The prompt response at hand was ");
		error.append(promptResponse);
		
		LOGGER.error(error.toString());
	}
	
	/**
	 * Creates the prompt response entry in the corresponding table and saves
	 * any attached files, images, videos, etc..
	 * 
	 * @param username
	 *        The username of the user saving this prompt response.
	 * 
	 * @param client
	 *        The name of the device used to generate the response.
	 * 
	 * @param surveyResponseId
	 *        The unique identifier for this survey response.
	 * 
	 * @param fileList
	 *        The list of files saved to the disk, which should be a reference
	 *        to a list that will be populated by this function.
	 * 
	 * @param promptUploadList
	 *        The collection of prompt responses to store.
	 * 
	 * @param repeatableSetIteration
	 *        If these prompt responses were part of a repeatable set, this is
	 *        the iteration of that repeatable set; otherwise, null.
	 * 
	 * @param bufferedImageMap
	 *        The map of image IDs to their contents.
	 * 
	 * @param videoContentsMap
	 *        The map of video IDs to their contents.
	 * 
	 * @param transactionManager
	 *        The manager for this transaction.
	 * 
	 * @param status
	 *        The status of this transaction.
	 * 
	 * @throws DataAccessException
	 *         There was an error saving the information.
	 */
	private void createPromptResponse(
			final String username, final String client,
			final Number surveyResponseId,
			final List<File> fileList,
			final Collection<Response> promptUploadList,
			final Integer repeatableSetIteration,
            final Map<UUID, Image> bufferedImageMap,
            final Map<UUID, Video> videoContentsMap, 
            final Map<UUID, Audio> audioContentsMap, 
            final Map<UUID, Media> documentContentsMap,
            final DataSourceTransactionManager transactionManager,
            final TransactionStatus status) 
			throws DataAccessException {
		
		for(Response response : promptUploadList) {
			if(response instanceof RepeatableSetResponse) {
				Map<Integer, Map<Integer, Response>> iterationToResponse =
					((RepeatableSetResponse) response).getResponseGroups();
				
				for(Integer iteration : iterationToResponse.keySet()) {
					createPromptResponse(
						username,
						client,
						surveyResponseId,
						fileList,
						iterationToResponse.get(iteration).values(),
						iteration,
						bufferedImageMap,
						videoContentsMap,
						audioContentsMap,
						documentContentsMap,
						transactionManager,
						status);
				}
				continue;
			}
			final PromptResponse promptResponse = (PromptResponse) response;
			
			getJdbcTemplate().update(
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps 
							= connection.prepareStatement(SQL_INSERT_PROMPT_RESPONSE);
						ps.setLong(1, surveyResponseId.longValue());
						
						RepeatableSet parent = promptResponse.getPrompt().getParent();
						if(parent == null) {
							ps.setNull(2, java.sql.Types.NULL);
							ps.setNull(3, java.sql.Types.NULL);
						}
						else {
							ps.setString(2, parent.getId());
							ps.setInt(3, repeatableSetIteration);
						}
						ps.setString(4, promptResponse.getPrompt().getType().toString());
						ps.setString(5, promptResponse.getPrompt().getId());
						
						Object response = promptResponse.getResponse();
						if(response instanceof DateTime) {
							ps.setString(
								6,
								DateTimeUtils
									.getW3cIso8601DateString(
										(DateTime) response,
										true));
						}
						else if((promptResponse instanceof MultiChoiceCustomPromptResponse) && (response instanceof Collection)) {
							JSONArray json = new JSONArray();
							
							for(Object currResponse : (Collection<?>) response) {
								json.put(currResponse);
							}
							
							ps.setString(6, json.toString());
						}
						else {
							ps.setString(6, response.toString());
						}
						
						return ps;
					}
				}
			);
			
			/*
			if(promptResponse instanceof PhotoPromptResponse) {
				// Grab the associated image and save it
				String imageId = promptResponse.getResponse().toString();
				UUID id = UUID.fromString(imageId);
				
				// If it wasn't skipped and it was displayed, save the
				// associated images.
				if(! JsonInputKeys.PROMPT_SKIPPED.equals(imageId) && 
					! JsonInputKeys.PROMPT_NOT_DISPLAYED.equals(imageId) &&
					! JsonInputKeys.IMAGE_NOT_UPLOADED.equals(imageId)) {
					

					try {
						MediaServices.instance().verifyMediaExistance(id, false);	
					} catch (ServiceException e) {
						LOGGER.debug("HT: The image UUID already exist" + imageId);
						throw new DataAccessException(e);
					}

					// Get the directory to save the image and save it.
					File originalFile = null;
					Image image = bufferedImageMap.get(id);
					try {					
						//replace getDirectory(). 
						originalFile = image.writeContent(MediaDirectoryCache.getImageDirectory());  
						fileList.add(originalFile); // store file reference. 
					}
					catch(DomainException e) {
						throw new DataAccessException(
									"Error saving the images.",
									e);
					}
					
					String metadata = image.getContentInfo().toMetadata();
					
					// Get the image's URL.
					String url = "file://" + originalFile.getAbsolutePath();
					// Insert the image URL into the database.
					try {
						getJdbcTemplate().update(
								SQL_INSERT_MEDIA, 
								new Object[] { username, client, imageId, url, metadata }
							);
					}
					catch(org.springframework.dao.DataAccessException e) {
						throw new DataAccessException(
									"Error executing SQL '" + SQL_INSERT_MEDIA + 
									"' with parameters: " + username + ", " + 
									client + ", " + imageId + ", " + url + ", " + metadata,
							e);
					}
				}
			}
			

			// HT: shorten the code
			else
			*/			
				// Save other media files.
			if( (promptResponse instanceof PhotoPromptResponse) ||
				(promptResponse instanceof AudioPromptResponse) ||
				(promptResponse instanceof VideoPromptResponse) ||
				(promptResponse instanceof DocumentPromptResponse)	 ) {
				LOGGER.debug("HT: Processing a media response");	
					
				// Make sure the response contains an actual media response. 
				// Can also check this against JsonInputKeys
				Object responseValue = promptResponse.getResponse();
				if(! (responseValue instanceof NoResponse)) {	
						
					// Attempt to write it to the file system.
					try {
						// Get the media ID.
						String mediaId = responseValue.toString();
						UUID id = UUID.fromString(mediaId);
						IMedia media = null;
						
						try {
							MediaServices.instance().verifyMediaExistance(id, false);	
						} catch (ServiceException e) {
							LOGGER.debug("HT: The media UUID already exist" + mediaId);
							throw new DataAccessException(e);
						}
						
						LOGGER.debug("HT: before getting Directory");
						// Get the current media directory.
						File currMediaDirectory = null;
						if (promptResponse instanceof PhotoPromptResponse) {
							currMediaDirectory = MediaDirectoryCache.getImageDirectory();
							media = bufferedImageMap.get(id);	
						} else if (promptResponse instanceof AudioPromptResponse) {
							currMediaDirectory = MediaDirectoryCache.getAudioDirectory();
							media = audioContentsMap.get(id);		
						} else if (promptResponse instanceof VideoPromptResponse) {							
							currMediaDirectory = MediaDirectoryCache.getVideoDirectory();
							media = videoContentsMap.get(id);	
						} else if (promptResponse instanceof DocumentPromptResponse) {
							currMediaDirectory = MediaDirectoryCache.getDocumentpDirectory();
							media = documentContentsMap.get(id);	
						} else if (promptResponse instanceof PhotoPromptResponse) {
							currMediaDirectory = MediaDirectoryCache.getImageDirectory();
							media = bufferedImageMap.get(id);	
						} 
							
						LOGGER.debug("HT: currMediaDirectory: " + currMediaDirectory);
											
						// Get the file. Only use UUID to store file since all detail should 
						// be stored in the db. 
						// File mediaFile = new File(currMediaDirectory.getAbsolutePath() + "/" + mediaId);
						// LOGGER.debug("HT: mediaFile: " + mediaFile.getAbsolutePath());
						
						File mediaFile = media.writeContent(currMediaDirectory);  // write the media content to mediaFile
						fileList.add(mediaFile);	// Store the file reference. 
			
						// Get the media URL.
						String url = "file://" + mediaFile.getAbsolutePath();
						LOGGER.debug("HT: Media file: " + url);
						LOGGER.debug("HT: Prompt type: " + promptResponse.getPrompt().getType());
							
						// Get the contentInfo
						String metadata = media.getContentInfo().toMetadata();
						
						// Insert the media URL into the database.
						try {
							getJdbcTemplate().update(
									SQL_INSERT_MEDIA, 
									new Object[] { username, client, mediaId, url, metadata }
									);
						}
						catch(org.springframework.dao.DataAccessException e) {
							// transactionManager.rollback(status);
							throw new DataAccessException(
									"Error executing SQL '" + SQL_INSERT_MEDIA + 
									"' with parameters: " + username + ", " + 
									client + ", " + mediaId + ", " + url + ", " + metadata,
									e);
						}
					}
						// If it fails, roll back the transaction.
					catch(DomainException e) {
						// transactionManager.rollback(status);
						throw new DataAccessException(
								"Could not get or write to the media directory.",
								e);
					} 
				}
			} // end if
			
		} // end for loop
	}
	
}