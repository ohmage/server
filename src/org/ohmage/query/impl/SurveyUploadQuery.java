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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ohmage.cache.AudioDirectoryCache;
import org.ohmage.cache.DocumentPDirectoryCache;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.cache.VideoDirectoryCache;
import org.ohmage.domain.Audio;
import org.ohmage.domain.DocumentP;
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
import org.ohmage.query.ISurveyUploadQuery;
import org.ohmage.request.JsonInputKeys;
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
	// The current directory to which the next image should be saved.
	private static File imageLeafDirectory;
	
	private static final Pattern IMAGE_DIRECTORY_PATTERN = 
		Pattern.compile("[0-9]+");
	
	public static final String IMAGE_STORE_FORMAT = "jpg";
	public static final String IMAGE_SCALED_EXTENSION = "-s";
	
	/**
	 * Filters the sub-directories in a directory to only return those that
	 * match the regular expression matcher for directories.
	 * 
	 * @author Joshua Selsky
	 */
	private static final class DirectoryFilter implements FilenameFilter {
		/**
		 * Returns true iff the filename is appropriate for the regular
		 * expression. 
		 */
		public boolean accept(File f, String name) {
			return IMAGE_DIRECTORY_PATTERN.matcher(name).matches();
		}
	}

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
	
	// Inserts an images information into the url_based_resource table.
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
			final Map<String, Video> videoContentsMap,
			final Map<String, Audio> audioContentsMap,
			final Map<String, DocumentP> documentContentsMap)
			throws DataAccessException {
		
		List<Integer> duplicateIndexList = new ArrayList<Integer>();
		int numberOfSurveys = surveyUploadList.size();
		
		// The following variables are used in logging messages when errors occur
		SurveyResponse currentSurveyResponse = null;
		PromptResponse currentPromptResponse = null;
		String currentSql = null;

		List<File> fileList = new LinkedList<File>();
		
		// Wrap all of the inserts in a transaction 
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("survey upload");
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		// Use a savepoint to handle nested rollbacks if duplicates are found
		Object savepoint = status.createSavepoint();
		
		try { // handle TransactionExceptions
			
			for(int surveyIndex = 0; surveyIndex < numberOfSurveys; surveyIndex++) { 
				
				 try { // handle DataAccessExceptions
					
					final SurveyResponse surveyUpload = surveyUploadList.get(surveyIndex);
					currentSurveyResponse = surveyUpload; 
					currentSql = SQL_INSERT_SURVEY_RESPONSE;
			
					KeyHolder idKeyHolder = new GeneratedKeyHolder();
					
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
					
					savepoint = status.createSavepoint();
					
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
						
						duplicateIndexList.add(surveyIndex);
						status.rollbackToSavepoint(savepoint);
						
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
						
				} catch (org.springframework.dao.DataAccessException dae) { 
					
					// Some other database problem happened that prevented
                    // the SQL from completing normally.
					
					LOGGER.error("caught DataAccessException", dae);
					logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
					for(File f : fileList) {
						f.delete();
					}
					rollback(transactionManager, status);
					throw new DataAccessException(dae);
				} 
				
			}
			
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
            final Map<String, Video> videoContentsMap, 
            final Map<String, Audio> audioContentsMap, 
            final Map<String, DocumentP> documentContentsMap,
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
			
			if(promptResponse instanceof PhotoPromptResponse) {
				// Grab the associated image and save it
				String imageId = promptResponse.getResponse().toString();
				
				// If it wasn't skipped and it was displayed, save the
				// associated images.
				if(! JsonInputKeys.PROMPT_SKIPPED.equals(imageId) && 
					! JsonInputKeys.PROMPT_NOT_DISPLAYED.equals(imageId) &&
					! JsonInputKeys.IMAGE_NOT_UPLOADED.equals(imageId)) {
					
					// Get the directory to save the image and save it.
					File originalFile;
					try {
						originalFile =
							bufferedImageMap
								.get(UUID.fromString(imageId))
								.saveImage(getDirectory());
					}
					catch(DomainException e) {
						rollback(transactionManager, status);
						throw
							new DataAccessException(
								"Error saving the images.",
								e);
					}
					
					// Get the image's URL.
					String url = "file://" + originalFile.getAbsolutePath();
					// Insert the image URL into the database.
					try {
						getJdbcTemplate().update(
								SQL_INSERT_IMAGE, 
								new Object[] { username, client, imageId, url }
							);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Error executing SQL '" + 
								SQL_INSERT_IMAGE + 
								"' with parameters: " +
								username + ", " + 
								client + ", " + 
								imageId + ", " + 
								url,
							e);
					}
				}
			}
			// Save other media files.
			// HT: shorten the code
			else if( (promptResponse instanceof AudioPromptResponse) ||
					 (promptResponse instanceof VideoPromptResponse) ||
					 (promptResponse instanceof DocumentPromptResponse)) {
				LOGGER.debug("HT: Processing a media response");	
					
				// Make sure the response contains an actual media response.
					Object responseValue = promptResponse.getResponse();
					if(! 
						((responseValue instanceof NoResponse) || 
						(responseValue instanceof NoResponseMedia) )) {		
						
					// Attempt to write it to the file system.
					try {
						// Get the media ID.
						Media media = null;
						String responseValueString = responseValue.toString();
						
						LOGGER.debug("HT: befire getting Directory");
						// Get the current media directory.
						File currMediaDirectory = null;
						if (promptResponse instanceof AudioPromptResponse) {
							currMediaDirectory = AudioDirectoryCache.getDirectory();
							media = audioContentsMap.get(responseValueString);		
						} else if (promptResponse instanceof VideoPromptResponse) {
							currMediaDirectory = VideoDirectoryCache.getDirectory();
							media = videoContentsMap.get(responseValueString);	
						} else if (promptResponse instanceof DocumentPromptResponse) {
							currMediaDirectory = DocumentPDirectoryCache.getDirectory();
							media = documentContentsMap.get(responseValueString);	
						}
							
						LOGGER.debug("HT: currMediaDirectory: " + currMediaDirectory);
											
							// Get the file.
						File mediaFile = 
								new File(
									currMediaDirectory.getAbsolutePath() +
									"/" +
									responseValueString +
									"." +
									media.getType());
						LOGGER.debug("HT: mediaFile: " + mediaFile.getAbsolutePath());
							
							// Get the video contents.
						InputStream content = media.getContentStream();
						if(content == null) {
							LOGGER.debug("HT: There is no " + media.getClass().getSimpleName() + " content");
							transactionManager.rollback(status);
								throw new DataAccessException(
										"The media[" + media.getClass().getSimpleName() + "] contents did not exist in the map.");
						}
							
							// Write the media contents to disk.
						FileOutputStream fos = new FileOutputStream(mediaFile);
							
							// Write the content to the output stream.
						int bytesRead;
						byte[] buffer = new byte[4096];
						while((bytesRead = content.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
						fos.close();

							// Store the file reference in the file list.
						fileList.add(mediaFile);
							
							// Get the video's URL.
						String url = "file://" + mediaFile.getAbsolutePath();
						LOGGER.debug("HT: Media file: " + url);
						LOGGER.debug("HT: Prompt type: " + promptResponse.getPrompt().getType());
							
							// Insert the media URL into the database.
						try {
							getJdbcTemplate().update(
									SQL_INSERT_IMAGE, 
									new Object[] { 
											username, 
											client, 
											responseValueString,
											url }
									);
						}
						catch(org.springframework.dao.DataAccessException e) {
							mediaFile.delete();
							transactionManager.rollback(status);
							throw new DataAccessException(
									"Error executing SQL '" + 
										SQL_INSERT_IMAGE + 
										"' with parameters: " +
										username + ", " + 
										client + ", " + 
										responseValueString + ", " + 
										url, 
									e);
						}
					}
						// If it fails, roll back the transaction.
					catch(DomainException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
								"Could not get the media directory.",
								e);
					}
					catch(IOException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
								"Could not write the file.",
								e);
					}
				}
			}
			
			/*
			else if(promptResponse instanceof VideoPromptResponse) {
				// Make sure the response contains an actual video response.
				Object responseValue = promptResponse.getResponse();
				if(! 
					(	(responseValue instanceof NoResponse) || 
						(responseValue instanceof NoResponseMedia)
					)) {
					
					// Attempt to write it to the file system.
					try {
						// Get the current video directory.
						File currVideoDirectory = 
							VideoDirectoryCache.getDirectory();

						// Get the video ID.
						String responseValueString = responseValue.toString();
						
						// Get the video object.
						Video video = 
							videoContentsMap.get(responseValueString);
						
						// Get the file.
						File videoFile = 
							new File(
								currVideoDirectory.getAbsolutePath() +
								"/" +
								responseValueString +
								"." +
								video.getType());
						
						// Get the video contents.
						InputStream content = video.getContentStream();
						if(content == null) {
							transactionManager.rollback(status);
							throw new DataAccessException(
								"The video contents did not exist in the map.");
						}
						
						// Write the video contents to disk.
						FileOutputStream fos = new FileOutputStream(videoFile);
						
						// Write the content to the output stream.
						int bytesRead;
						byte[] buffer = new byte[4096];
						while((bytesRead = content.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
						fos.close();

						// Store the file reference in the video list.
						fileList.add(videoFile);
						
						// Get the video's URL.
						String url = "file://" + videoFile.getAbsolutePath();
						
						// Insert the video URL into the database.
						try {
							getJdbcTemplate().update(
									SQL_INSERT_IMAGE, 
									new Object[] { 
										username, 
										client, 
										responseValueString,
										url }
								);
						}
						catch(org.springframework.dao.DataAccessException e) {
							videoFile.delete();
							transactionManager.rollback(status);
							throw new DataAccessException(
								"Error executing SQL '" + 
									SQL_INSERT_IMAGE + 
									"' with parameters: " +
									username + ", " + 
									client + ", " + 
									responseValueString + ", " + 
									url, 
								e);
						}
					}
					// If it fails, roll back the transaction.
					catch(DomainException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Could not get the video directory.",
							e);
					}
					catch(IOException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Could not write the file.",
							e);
					}
				}
			}
			else if(promptResponse instanceof AudioPromptResponse) {
				// Make sure the response contains an actual audio response.
				Object responseValue = promptResponse.getResponse();
				if(! 
					(	(responseValue instanceof NoResponse) || 
						(responseValue instanceof NoResponseMedia)
					)) {
					
					// Attempt to write it to the file system.
					try {
						// Get the current audio directory.
						File currAudioDirectory = 
							AudioDirectoryCache.getDirectory();

						// Get the audio ID.
						String responseValueString = responseValue.toString();
						
						// Get the audio object.
						Audio audio = 
							audioContentsMap.get(responseValueString);
						
						// Get the file.
						File audioFile = 
							new File(
								currAudioDirectory.getAbsolutePath() +
								"/" +
								responseValueString +
								"." +
								audio.getType());
						
						// Get the video contents.
						InputStream content = audio.getContentStream();
						if(content == null) {
							transactionManager.rollback(status);
							throw new DataAccessException(
								"The audio contents did not exist in the map.");
						}
						
						// Write the video contents to disk.
						FileOutputStream fos = new FileOutputStream(audioFile);
						
						// Write the content to the output stream.
						int bytesRead;
						byte[] buffer = new byte[4096];
						while((bytesRead = content.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
						fos.close();

						// Store the file reference in the video list.
						fileList.add(audioFile);
						
						// Get the video's URL.
						String url = "file://" + audioFile.getAbsolutePath();
						
						// Insert the video URL into the database.
						try {
							getJdbcTemplate().update(
									SQL_INSERT_IMAGE, 
									new Object[] { 
										username, 
										client, 
										responseValueString,
										url }
								);
						}
						catch(org.springframework.dao.DataAccessException e) {
							audioFile.delete();
							transactionManager.rollback(status);
							throw new DataAccessException(
								"Error executing SQL '" + 
									SQL_INSERT_IMAGE + 
									"' with parameters: " +
									username + ", " + 
									client + ", " + 
									responseValueString + ", " + 
									url, 
								e);
						}
					}
					// If it fails, roll back the transaction.
					catch(DomainException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Could not get the audio directory.",
							e);
					}
					catch(IOException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Could not write the file.",
							e);
					}
				}
			}
			else if (promptResponse instanceof DocumentPromptResponse){				
				// Make sure the response contains an actual audio response.
				Object responseValue = promptResponse.getResponse();
				if(! 
					(	(responseValue instanceof NoResponse) || 
						(responseValue instanceof NoResponseMedia)
					)) {
					
					// Attempt to write it to the file system.
					try {
						// Get the current audio directory.
						File currDocumentDirectory = 
							DocumentPDirectoryCache.getDirectory();

						// Get the audio ID.
						String responseValueString = responseValue.toString();
						
						// Get the audio object.
						DocumentP document = 
							documentContentsMap.get(responseValueString);
						
						// Get the file.
						File documentFile = 
							new File(
								currDocumentDirectory.getAbsolutePath() +
								"/" +
								responseValueString +
								"." +
								document.getType());
						
						// Get the document contents.
						InputStream content = document.getContentStream();
						if(content == null) {
							transactionManager.rollback(status);
							throw new DataAccessException(
								"The document contents did not exist in the map.");
						}
						
						// Write the video contents to disk.
						FileOutputStream fos = new FileOutputStream(documentFile);
						
						// Write the content to the output stream.
						int bytesRead;
						byte[] buffer = new byte[4096];
						while((bytesRead = content.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
						fos.close();

						// Store the file reference in the video list.
						fileList.add(documentFile);
						
						// Get the video's URL.
						String url = "file://" + documentFile.getAbsolutePath();
						
						// Insert the video URL into the database.
						try {
							getJdbcTemplate().update(
									SQL_INSERT_IMAGE, 
									new Object[] { 
										username, 
										client, 
										responseValueString,
										url }
								);
						}
						catch(org.springframework.dao.DataAccessException e) {
							documentFile.delete();
							transactionManager.rollback(status);
							throw new DataAccessException(
								"Error executing SQL '" + 
									SQL_INSERT_IMAGE + 
									"' with parameters: " +
									username + ", " + 
									client + ", " + 
									responseValueString + ", " + 
									url, 
								e);
						}
					}
					// If it fails, roll back the transaction.
					catch(DomainException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Could not get the document directory.",
							e);
					}
					catch(IOException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Could not write the file.",
							e);
					}
				}
			}
			*/
		}
	}
	


	/**
	 * Copied directly from ImageQueries.
	 * 
	 * Gets the directory to which a image should be saved. This should be used
	 * instead of accessing the class-level variable directly as it handles the
	 * creation of new folders and the checking that the current
	 * folder is not full.
	 * 
	 * @return A File object for where an image should be written.
	 */
	private File getDirectory() throws DataAccessException {
		// Get the maximum number of items in a directory.
		int numFilesPerDirectory;
		try {
			numFilesPerDirectory = 
				Integer.decode(
					PreferenceCache.instance().lookup(
						PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY));
		}
		catch(CacheMissException e) {
			throw new DataAccessException(
				"Preference cache doesn't know about 'known' key: " + 
					PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY,
				e);
		}
		catch(NumberFormatException e) {
			throw new DataAccessException(
				"Stored value for key '" + 
					PreferenceCache.KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY +
					"' is not decodable as a number.",
				e);
		}
		
		// If the leaf directory was never initialized, then we should do
		// that. Note that the initialization is dumb in that it will get to
		// the end of the structure and not check to see if the leaf node is
		// full.
		if(imageLeafDirectory == null) {
			init(numFilesPerDirectory);
		}
		
		File[] documents = imageLeafDirectory.listFiles();
		// If the 'imageLeafDirectory' directory is full, traverse the tree and
		// find a new directory.
		if(documents.length >= numFilesPerDirectory) {
			getNewDirectory(numFilesPerDirectory);
		}
		
		return imageLeafDirectory;
	}
	
	/**
	 * Initializes the directory structure by drilling down to the leaf
	 * directory with each step choosing the directory with the largest
	 * integer value.
	 */
	private synchronized void init(int numFilesPerDirectory) throws DataAccessException {
		try {
			// If the current leaf directory has been set, we weren't the
			// first to call init(), so we can just back out.
			if(imageLeafDirectory != null) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(PreferenceCache.KEY_IMAGE_DIRECTORY);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_IMAGE_DIRECTORY, e);
			}
			File rootDirectory = new File(rootFile);
			if(! rootDirectory.exists()) {
				throw new DataAccessException("The root file doesn't exist suggesting an incomplete installation: " + rootFile);
			}
			else if(! rootDirectory.isDirectory()) {
				throw new DataAccessException("The root file isn't a directory.");
			}
			
			// Get the number of folders deep that documents are stored.
			int fileDepth;
			try {
				fileDepth = Integer.decode(PreferenceCache.instance().lookup(PreferenceCache.KEY_FILE_HIERARCHY_DEPTH));
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_FILE_HIERARCHY_DEPTH, e);
			}
			catch(NumberFormatException e) {
				throw new DataAccessException("Stored value for key '" + PreferenceCache.KEY_FILE_HIERARCHY_DEPTH + "' is not decodable as a number.", e);
			}
			
			DirectoryFilter directoryFilter = new DirectoryFilter();
			File currDirectory = rootDirectory;
			for(int currDepth = 0; currDepth < fileDepth; currDepth++) {
				// Get the list of directories in the current directory.
				File[] currDirectories = currDirectory.listFiles(directoryFilter);
				
				// If there aren't any, create the first subdirectory in this
				// directory.
				if(currDirectories.length == 0) {
					String newFolderName = directoryNameBuilder(0, numFilesPerDirectory);
					currDirectory = new File(currDirectory.getAbsolutePath() + "/" + newFolderName);
					currDirectory.mkdir();
				}
				// If the directory is overly full, step back up in the
				// structure. This should never happen, as it indicates that
				// there is an overflow in the structure.
				else if(currDirectories.length > numFilesPerDirectory) {
					LOGGER.warn("Too many subdirectories in: " + currDirectory.getAbsolutePath());
					
					// Take a step back in our depth.
					currDepth--;
					
					// If, while backing up the tree, we back out of the root
					// directory, we have filled up the space.
					if(currDepth < 0) {
						LOGGER.error("Image directory structure full!");
						throw new DataAccessException("Image directory structure full!");
					}

					// Get the next parent and the current directory to it.
					int nextDirectoryNumber = Integer.decode(currDirectory.getName()) + 1;
					currDirectory = new File(currDirectory.getParent() + "/" + nextDirectoryNumber);
					
					// If the directory already exists, then there is either a
					// concurrency issue or someone else is adding files.
					// Either way, this shouldn't happen.
					if(currDirectory.exists()) {
						LOGGER.error("Somehow the 'new' directory already exists. This should be looked into: " + currDirectory.getAbsolutePath());
					}
					// Otherwise, create the directory.
					else {
						currDirectory.mkdir();
					}
				}
				// Drill down to the directory with the largest, numeric value.
				else {
					currDirectory = getLargestSubfolder(currDirectories);
				}
			}
			
			// After we have found a suitable directory, set it.
			imageLeafDirectory = currDirectory;
		}
		catch(SecurityException e) {
			throw new DataAccessException("The current process doesn't have sufficient permiossions to create new directories.", e);
		}
	}
	
	/**
	 * Checks again that the current leaf directory is full. If it is not, then
	 * it will just back out under the impression someone else made the change.
	 * If it is, it will go up and down the directory tree structure to find a
	 * new leaf node in which to store new files.
	 * 
	 * @param numFilesPerDirectory The maximum allowed number of files in a
	 * 							   leaf directory and the maximum allowed
	 * 							   number of directories in the branches.
	 */
	private synchronized void getNewDirectory(int numFilesPerDirectory) throws DataAccessException {
		try {
			// Make sure that this hasn't changed because another thread may
			// have preempted us and already changed the current leaf
			// directory.
			File[] files = imageLeafDirectory.listFiles();
			if(files.length < numFilesPerDirectory) {
				return;
			}
			
			// Get the root directory from the preference cache based on the
			// key.
			String rootFile;
			try {
				rootFile = PreferenceCache.instance().lookup(PreferenceCache.KEY_IMAGE_DIRECTORY);
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Preference cache doesn't know about 'known' key: " + PreferenceCache.KEY_IMAGE_DIRECTORY, e);
			}
			File rootDirectory = new File(rootFile);
			if(! rootDirectory.exists()) {
				throw new DataAccessException("The root file doesn't exist suggesting an incomplete installation: " + rootFile);
			}
			else if(! rootDirectory.isDirectory()) {
				throw new DataAccessException("The root file isn't a directory.");
			}
			String absoluteRootDirectory = rootDirectory.getAbsolutePath();
			
			// A filter when listing a set of directories for a file.
			DirectoryFilter directoryFilter = new DirectoryFilter();
			
			// A local File to use while we are searching to not confuse other
			// threads.
			File newDirectory = imageLeafDirectory;
			
			// A flag to indicate when we are done looking for a directory.
			boolean lookingForDirectory = true;
			
			// The number of times we stepped up in the hierarchy.
			int depth = 0;
			
			// While we are still looking for a suitable directory,
			while(lookingForDirectory) {
				// Get the current directory's name which should be a Long
				// value.
				long currDirectoryName;
				try {
					String dirName = newDirectory.getName();
					while(dirName.startsWith("0")) {
						dirName = dirName.substring(1);
					}
					if("".equals(dirName)) {
						currDirectoryName = 0;
					}
					else {
						currDirectoryName = Long.decode(dirName);
					}
				}
				catch(NumberFormatException e) {
					if(newDirectory.getAbsolutePath().equals(absoluteRootDirectory)) {
						throw new DataAccessException("Document structure full!", e);
					}
					else {
						throw new DataAccessException("Potential breach of document structure.", e);
					}
				}
				
				// Move the pointer up a directory.
				newDirectory = new File(newDirectory.getParent());
				// Get the list of files in the parent.
				File[] parentDirectoryFiles = newDirectory.listFiles(directoryFilter);
				
				// If this directory has room for a new subdirectory,
				if(parentDirectoryFiles.length < numFilesPerDirectory) {
					// Increment the name for the next subfolder.
					currDirectoryName++;
					
					// Create the new subfolder.
					newDirectory = new File(newDirectory.getAbsolutePath() + "/" + directoryNameBuilder(currDirectoryName, numFilesPerDirectory));
					newDirectory.mkdir();
					
					// Continue drilling down to reach an appropriate leaf
					// node.
					while(depth > 0) {
						newDirectory = new File(newDirectory.getAbsolutePath() + "/" + directoryNameBuilder(0, numFilesPerDirectory));
						newDirectory.mkdir();
						
						depth--;
					}
					
					lookingForDirectory = false;
				}
				// If the parent is full as well, increment the depth unless
				// we are already at the parent. If we are at the parent, then
				// we cannot go up any further and have exhausted the
				// directory structure.
				else
				{
					if(newDirectory.getAbsoluteFile().equals(absoluteRootDirectory)) {
						throw new DataAccessException("Document structure full!");
					}
					else {
						depth++;
					}
				}
			}
			
			imageLeafDirectory = newDirectory;
		}
		catch(NumberFormatException e) {
			throw new DataAccessException("Could not decode a directory name as an integer.", e);
		}
	}
	
	/**
	 * Builds the name of a folder by prepending zeroes where necessary and
	 * converting the name into a String.
	 * 
	 * @param name The name of the file as an integer.
	 * 
	 * @param numFilesPerDirectory The maximum number of files allowed in the
	 * 							   directory used to determine how many zeroes
	 * 							   to prepend.
	 * 
	 * @return A String representing the directory name based on the
	 * 		   parameters.
	 */
	private String directoryNameBuilder(long name, int numFilesPerDirectory) {
		int nameLength = String.valueOf(name).length();
		int maxLength = new Double(Math.log10(numFilesPerDirectory)).intValue();
		int numberOfZeros = maxLength - nameLength;
		
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < numberOfZeros; i++) {
			builder.append("0");
		}
		builder.append(String.valueOf(name));
		
		return builder.toString();
	}
	
	/**
	 * Sorts the directories and returns the one whose alphanumeric value is
	 * the greatest.
	 * 
	 * This will work with any naming for directories, so it is the caller's
	 * responsibility to ensure that the list of directories are what they
	 * want them to be.
	 *  
	 * @param directories The list of directories whose largest alphanumeric
	 * 					  value is desired.
	 * 
	 * @return Returns the File whose path and name has the largest
	 * 		   alphanumeric value.
	 */
	private File getLargestSubfolder(File[] directories) {
		Arrays.sort(directories);
		
		return directories[directories.length - 1];
	}
}