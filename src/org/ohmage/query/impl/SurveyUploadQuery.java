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
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.MediaDirectoryCache;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.Audio;
import org.ohmage.domain.IMedia;
import org.ohmage.domain.Image;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.Video;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.RepeatableSetResponse;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.response.AudioPromptResponse;
import org.ohmage.domain.campaign.response.FilePromptResponse;
import org.ohmage.domain.campaign.response.MediaPromptResponse;
import org.ohmage.domain.campaign.response.MultiChoiceCustomPromptResponse;
import org.ohmage.domain.campaign.response.PhotoPromptResponse;
import org.ohmage.domain.campaign.response.VideoPromptResponse;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IMediaQueries;
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
	private IMediaQueries mediaQueries;

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
	private SurveyUploadQuery(DataSource dataSource, 
				IMediaQueries iMediaQueries) {
	    
		super(dataSource);
		if(iMediaQueries == null) {
			throw new IllegalArgumentException("An instance of IImageQueries is a required argument.");
		}
		this.mediaQueries = iMediaQueries;
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
			final Map<UUID, IMedia> documentContentsMap)
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
		final Map<UUID, IMedia> documentContentsMap,
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
			
			// Save other media files.
			if( (promptResponse instanceof MediaPromptResponse)	) {

			    // insert a new entry in the db and file system
			    try {
				insertMediaReseponse(
					username, client,
					promptResponse,
					bufferedImageMap,
					videoContentsMap, 
					audioContentsMap, 
					documentContentsMap,
					fileList);
			    } catch (DataAccessException e) {
				throw new DataAccessException("Can't insert a new entry in the url_based_resource", e);
			    }
/*
					
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
						} else if (promptResponse instanceof FilePromptResponse) {
							currMediaDirectory = MediaDirectoryCache.getFileDirectory();
							media = documentContentsMap.get(id);	
						} else if (promptResponse instanceof PhotoPromptResponse) {
							currMediaDirectory = MediaDirectoryCache.getImageDirectory();
							media = bufferedImageMap.get(id);	
						} 
							
											
						// Get the file. Only use UUID to store file since all detail should 
						// be stored in the db. 
						// File mediaFile = new File(currMediaDirectory.getAbsolutePath() + "/" + mediaId);
						// LOGGER.debug("HT: mediaFile: " + mediaFile.getAbsolutePath());
						
						File mediaFile = media.writeContent(currMediaDirectory);  // write the media content to mediaFile
						fileList.add(mediaFile);	// Store the file reference. 
			
						// Get the media URL.
						String url = "file://" + mediaFile.getAbsolutePath();
						// LOGGER.debug("HT: Media file: " + url);
						// LOGGER.debug("HT: Prompt type: " + promptResponse.getPrompt().getType());
							
						// Get the contentInfo
						String metadata = media.getMetadata();
						
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
*/
			} // end if
			
		} // end for loop
	}
	

	public void insertMediaReseponse(
		final String username, final String client,
		final PromptResponse promptResponse,
		final Map<UUID, Image> bufferedImageMap,
		final Map<UUID, Video> videoContentsMap, 
		final Map<UUID, Audio> audioContentsMap, 
		final Map<UUID, IMedia> documentContentsMap,
		final Collection<File> fileList
		) throws DataAccessException {
	    
	    if (! (promptResponse instanceof MediaPromptResponse)) {
		LOGGER.error("Attempting to update url_based_resource with non-media prompts: " + promptResponse.getId());
		throw new DataAccessException("Can't insert non_media prompts in the url_based_resource: " + promptResponse.getId());
	    }
	    
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
		    } else if (promptResponse instanceof FilePromptResponse) {
			currMediaDirectory = MediaDirectoryCache.getFileDirectory();
			media = documentContentsMap.get(id);	
		    } else if (promptResponse instanceof PhotoPromptResponse) {
			currMediaDirectory = MediaDirectoryCache.getImageDirectory();
			media = bufferedImageMap.get(id);	
		    } 
		    
		    
		    // Get the file. Only use UUID to store file since all detail should 
		    // be stored in the db. 
		    // File mediaFile = new File(currMediaDirectory.getAbsolutePath() + "/" + mediaId);
		    // LOGGER.debug("HT: mediaFile: " + mediaFile.getAbsolutePath());
		    
		    File mediaFile = media.writeContent(currMediaDirectory);  // write the media content to mediaFile
		    fileList.add(mediaFile);	// Store the file reference. 
		    
		    // Get the media URL.
		    String url = "file://" + mediaFile.getAbsolutePath();
		    // LOGGER.debug("HT: Media file: " + url);
		    // LOGGER.debug("HT: Prompt type: " + promptResponse.getPrompt().getType());
					
		    // Get the contentInfo
		    String metadata = media.getMetadata();
		    
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
	    } // end if
	} 	    

	public void updateSurveys(
		final String username,
		final String client,
		 final String campaignUrn,
		final List<SurveyResponse> surveyUploadList,
		final Map<UUID, Image> bufferedImageMap,
		final Map<UUID, Video> videoContentsMap,
		final Map<UUID, Audio> audioContentsMap,
		final Map<UUID, IMedia> documentContentsMap,
		final Map<UUID, SurveyResponse> existingResponseMap)
		throws DataAccessException {
	
	    
	    Set<File> newFileList = new HashSet<File>();  // keep track of files created along the process
	    Set<File> oldFileList = new HashSet<File>();

	    // need a list of media object UUID before it is updated.
	    Map<String, UUID> mediaResponseMap = new HashMap<String, UUID>();

	    // update the existing responses with the uploaded responses. 
	    for(SurveyResponse uploadSurvey : surveyUploadList) {
		SurveyResponse existingSurvey = existingResponseMap.get(uploadSurvey.getSurveyResponseId());
		
		// update the existing response with the new content
		 for (Integer index : uploadSurvey.getResponses().keySet()) {
		     Response response = uploadSurvey.getResponses().get(index);
		     Response existingResponse = existingSurvey.getResponses().get(index);
		     
		     if(response instanceof PromptResponse) {
			
			// keep track of previous uuid before updating the prompt
			if (response instanceof MediaPromptResponse){			    
			    try {
				mediaResponseMap.put(response.getId(), ((MediaPromptResponse) existingResponse).getUuid());
			    } catch (DomainException e) {
				// is NOT_DISPLAYED or SKIPPED, put null in the map.
				mediaResponseMap.put(response.getId(), null);
			    }
			}
			
			// Update the existing prompt or throw an exception
			try {
			    existingSurvey.addPromptResponse((PromptResponse)response);  
			} catch (DomainException e) {
			    // Can't update the prompt: throw an error
			    throw new DataAccessException(
				    ErrorCode.SURVEY_INVALID_RESPONSES, 
				    "Can't update the existing survey responses", e);
			}
		    }   // Repeatable set is not supported. Throw an error 
		    else if (response instanceof RepeatableSetResponse) {
			throw new DataAccessException("RepeatableSet responses are not supported.");
		    }
		}
	    }

	    int numberOfSurveys = surveyUploadList.size();
	    final String sqlUpdateSurveyResponse = "UPDATE survey_response " +
		    "SET " +
		    "epoch_millis = ?, " +
		    "phone_timezone = ?, " +
		    "location_status = ?, " +
		    "location = ?, " +
		    "survey_id = ?, " +
		    "survey = ?, " +
		    "client = ?, " +
		    "upload_timestamp = ?, " +
		    "launch_context = ?, " +
		    "privacy_state_id = (SELECT id FROM survey_response_privacy_state WHERE privacy_state = ?) " + 
		    "WHERE uuid = ? ";
	    
	    // The following variables are used in logging messages when errors occur
	    SurveyResponse currentSurveyResponse = null;
	    PromptResponse currentPromptResponse = null;
	   
	    // Wrap all of the inserts in a transaction 
	    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	    def.setName("survey upload---update");
	    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
	    TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
	
	    // Use a savepoint to handle nested rollbacks if something is wrong
	    // Object savepoint = status.createSavepoint();	

	    try { // handle TransactionExceptions

		for(int surveyIndex = 0; surveyIndex < numberOfSurveys; surveyIndex++) { 
			
		    try { // handle DataAccessExceptions
				
			final SurveyResponse uploadSurvey = surveyUploadList.get(surveyIndex);
			// need this to update the response json in the survey response entries
			final SurveyResponse existingSurveyResponse = existingResponseMap.get(uploadSurvey.getSurveyResponseId());
											
			// update the survey
			getJdbcTemplate().update(
				new PreparedStatementCreator() {
				    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sqlUpdateSurveyResponse);
							
					String locationString = null;
					Location location = uploadSurvey.getLocation();
					if(location != null) {
					    try {
						locationString = 
							location.toJson(false, LocationColumnKey.ALL_COLUMNS).toString();
					    }
					    catch(JSONException|DomainException e) {
						throw new SQLException(e);
					    }
					}
								
					ps.setLong(1, uploadSurvey.getTime());
					ps.setString(2, uploadSurvey.getTimezone().getID());
					ps.setString(3, uploadSurvey.getLocationStatus().toString());
					ps.setString(4, locationString);
					ps.setString(5, uploadSurvey.getSurvey().getId());
					try {
					    // used the updated response object for response array
					    ps.setString(6, existingSurveyResponse.toJson(false, false, false, false, true, true, true, true, true, false, false, true, true, true, true, false, false).toString());
					}
					catch(JSONException e) {
					    throw new SQLException("Couldn't create the response JSON.", e);
					}
					catch(DomainException e) {
					    throw new SQLException("Couldn't create the response JSON.", e);
					}
					ps.setString(7, client);
					ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
					try {
					    ps.setString(9, uploadSurvey.getLaunchContext().toJson(true).toString());
					}
					catch(JSONException e) {
					    throw new SQLException("Couldn't create the LaunchContext JSON.", e);
					}
					
					// TODO: use what's provided instead of cache
					try {
					    ps.setString(10, PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE));
					} catch (CacheMissException e) {
					    throw new SQLException("Error reading from the cache.", e);
					}
					
					ps.setString(11, uploadSurvey.getSurveyResponseId().toString());
					// ps.setString(2, username);
					// ps.setString(3, campaignUrn);
		
					return ps;
				    }
				}
				);
								
			final Number surveyResponseDbId = existingSurveyResponse.getSurveyResponseDbId();
			// if surveyResponseDbId is null, something is wrong
			if (existingSurveyResponse.getSurveyResponseDbId() == null) {
			    throw new DataAccessException("The existing response db id is null. Can't update prompt response");
			}
								
			// Now update each prompt response from the survey
			Collection<Response> promptUploadList = uploadSurvey.getResponses().values();
			for (Response uploadPromptResponse : promptUploadList) {
			    try {
				updatePromptResponse(
					username,
					client,
					surveyResponseDbId,
					uploadPromptResponse,
					null,
					bufferedImageMap,
					videoContentsMap,
					audioContentsMap,
					documentContentsMap,
					mediaResponseMap, 
					newFileList,
					oldFileList);
			    } catch (DataAccessException e) {
				LOGGER.error("Can't update prompt_response and/or url_based_resource" + username);	
				throw new DataAccessException(e);	    
			    }
			}
				
		    } catch (DataIntegrityViolationException dive) { 
			// Some other integrity violation occurred - bad!!
			LOGGER.error("Caught DataIntegrityViolationException", dive);
			logErrorDetails(currentSurveyResponse, currentPromptResponse, sqlUpdateSurveyResponse, username, campaignUrn);
			rollback(transactionManager, status);
			for(File f : newFileList) {
			    f.delete();
			}
			throw new DataAccessException(dive);
			
		    } catch (org.springframework.dao.DataAccessException|DataAccessException dae) { 
			// Some other database problem happened that prevented
			// the SQL from completing normally. 
			LOGGER.error("caught Spring DataAccessException", dae);
			logErrorDetails(currentSurveyResponse, currentPromptResponse, sqlUpdateSurveyResponse, username, campaignUrn);
			rollback(transactionManager, status);
			for(File f : newFileList) {
			    f.delete();
			}
			throw new DataAccessException(dae);
		    }
			
		} // for surveyIndex
		
		// Finally, commit the transaction
		transactionManager.commit(status);
		
		// Delete old files if the update is done successfully 
		for (File f : oldFileList) {
		    f.delete();
		    LOGGER.debug("File Deleted: " + f.getAbsolutePath());
		}
		
		LOGGER.info("Completed survey update persistence");
	} 
	catch (TransactionException te) { 		
	    LOGGER.error("failed to commit survey update transaction, attempting to rollback", te);
	    logErrorDetails(currentSurveyResponse, currentPromptResponse, sqlUpdateSurveyResponse, username, campaignUrn);
	    rollback(transactionManager, status);
	    for(File f : newFileList) {
		f.delete();
	    }
	    throw new DataAccessException(te);
	}
	    
	LOGGER.info("Finished updating survey responses and any associated media objects into the database and the filesystem.");
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
	 * @param surveyResponseDbId
	 *        The unique identifier for this survey response.
	 * 
	 * @param newFileList
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
	private void updatePromptResponse(
			final String username, final String client,
			final Number surveyResponseDbId,
			final Response uploadPromptResponse,
			final Integer repeatableSetIteration,
			final Map<UUID, Image> bufferedImageMap,
			final Map<UUID, Video> videoContentsMap, 
			final Map<UUID, Audio> audioContentsMap, 
			final Map<UUID, IMedia> documentContentsMap,
			final Map<String, UUID> mediaResponseMap, 
			final Collection<File> newFileList,
			final Collection<File> oldFileList
		) 
			throws DataAccessException {

	    // Throws exception. Currently repelatable set not supported. 
	    if(uploadPromptResponse instanceof RepeatableSetResponse) {
		// TODO: To support this, we need to delete and then recreate.
		// and need to make sure that the survey responses are complete.
		throw new DataAccessException("This API doesn't support repeatableSet");
	    }
		
	    final PromptResponse promptResponse = (PromptResponse) uploadPromptResponse;
	    final String sqlUpdateResponse = "UPDATE prompt_response SET response = ? WHERE survey_response_id = ? AND prompt_id = ?";
			
	    // In case of media prompts, extract the existing UUID to access the url_based_resource	
	    getJdbcTemplate().update(
		    new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			    PreparedStatement ps = connection.prepareStatement(sqlUpdateResponse);

			    Object response = promptResponse.getResponse();
			    String responseString = null;
				
			    if(response instanceof DateTime) {
				responseString = DateTimeUtils.getW3cIso8601DateString(
					(DateTime) response, true);
			    }
			    else if((promptResponse instanceof MultiChoiceCustomPromptResponse) && (response instanceof Collection)) {
				JSONArray json = new JSONArray();
				for(Object currResponse : (Collection<?>) response) {
				    json.put(currResponse);
				}
				responseString = json.toString();
			    }
			    else {
				responseString = response.toString();
			    }
				
			    ps.setString(1, responseString);	
			    ps.setLong(2, surveyResponseDbId.longValue());
			    ps.setString(3, promptResponse.getPrompt().getId());
						
			    return ps;
			}
		    }
		    );
			
	    // deal with media prompt response.
	    if( (promptResponse instanceof MediaPromptResponse)) {		
		    
		// deleting an existing entry in the DB and the file object
		UUID existingUuid = mediaResponseMap.get(promptResponse.getId());
		URL existingMediaUrl = null;

		if (existingUuid != null) {
		    LOGGER.debug("Existing media prompt info : " + promptResponse.getId() + ", " + existingUuid.toString());

		    // verify that the existing media exist and remove it
		    // from url_based_resource.
		    try {
			existingMediaUrl = mediaQueries.getMediaUrl(existingUuid);	
		    } catch (DataAccessException e) {
			LOGGER.error("The media URL doesn't exist: " + existingUuid.toString());
			throw new DataAccessException("The media URL doesn't exist or malformed"); 
		    }

		    if (existingMediaUrl == null) {
			LOGGER.error("This shouldn't happen!!! Media Url of this UUID is null: " + existingUuid.toString());
		    } else {
			LOGGER.debug("Attempt to dalete " + existingUuid.toString() + ", " + existingMediaUrl);
		    }
			
		    // delete the entry from the db
		    String sqlDeleteUrlBasedResource = 
			    "DELETE FROM url_based_resource WHERE uuid = ? ";
		    try {
			getJdbcTemplate().update(
				sqlDeleteUrlBasedResource, 
				new Object[] { existingUuid.toString() });
		    }
		    catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + sqlDeleteUrlBasedResource + 
				"' with parameter: " + existingUuid.toString(), 
				e);
		    }
			    
		    // Track all the old media files to be deleted at the end of transaction.
		    // If the operation files, the db entries can be recovered, but not 
		    // the deleted files.
		    try { 
			if (promptResponse instanceof PhotoPromptResponse) {	
			    // TODO: put this in the proper prompt response
			    for (Image.Size size : Image.getSizes()) {
				try {
				    URL imageUrl = Image.Size.getUrl(size, existingMediaUrl);
				    File file = new File(imageUrl.getFile());
				    if (file != null) {
					LOGGER.debug("Add the following files to delete: " + file.getAbsolutePath());
					oldFileList.add(file);
				    }
				} catch (DomainException e) {
				    LOGGER.error("Can't get the url for image size " + size.getName() + ". Will ignore");
				}
			    }
			} else {
			    File file = new File(existingMediaUrl.getFile());
			    if (file != null) {
				LOGGER.debug("Add the following files to delete: " + file.getAbsolutePath());
				oldFileList.add(file);
			    }
			}
		    } catch (Exception e) {
			throw new DataAccessException(
				"Can't delete an existing media file: " + existingUuid.toString(),
				e);
		    }
			
		}

		// insert a new entry in the db and file system
		try {
		    insertMediaReseponse(
			    username, client,
			    promptResponse,
			    bufferedImageMap,
			    videoContentsMap, 
			    audioContentsMap, 
			    documentContentsMap,
			    newFileList);
		} catch (DataAccessException e) {
		    throw new DataAccessException("Can't insert a new entry in the url_based_resource", e);
		}
	    }		
	}
	
}