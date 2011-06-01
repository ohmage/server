/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.DataPacket;
import org.ohmage.domain.PromptResponseDataPacket;
import org.ohmage.domain.SurveyDataPacket;
import org.ohmage.request.AwRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * Persist surveys to the db: entire surveys are persisted as JSON in the survey_response table and each prompt response from a 
 * survey is persisted to its own row in the prompt_response table.
 * 
 * @author selsky
 */
public class SurveyUploadDao extends AbstractUploadDao {
	private static Logger _logger = Logger.getLogger(SurveyUploadDao.class);
	
	private final String _selectCampaignId = "SELECT id FROM campaign WHERE urn = ?";
	
	private final String _insertSurveyResponse = "INSERT into survey_response" +
								           		 " (user_id, campaign_id, msg_timestamp, epoch_millis," +
								           		 " phone_timezone, location_status, location, survey_id, survey," +
								           		 " client, upload_timestamp, launch_context, privacy_state_id) " +
										         " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private final String _insertPromptResponse = "INSERT into prompt_response" +
	                                             " (survey_response_id, repeatable_set_id, repeatable_set_iteration," +
	                                             " prompt_type, prompt_id, response)" +
	                                             " VALUES (?,?,?,?,?,?)";
	
	public SurveyUploadDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Inserts surveys and prompts into the survey_response and prompt_response tables. Expects the surveys to be in the form of
	 * DataPackets where each survey contains a list of prompt response DataPackets. The entire survey upload is treated as one
	 * transaction. Savepoints are used to gracefully skip over duplicate survey uploads.
	 * 
     * @throws DataAccessException if any Spring TransactionException or DataAccessException (except for a 
	 *         DataIntegrityViolationException denoting duplicates) occurs 
	 * @throws IllegalArgumentException if a List of DataPackets is not present as an attribute on the AwRequest	 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Beginning inserting responses into the database.");
		
		final int campaignId;
		
		// Get the campaign.id for linking a survey_response to the correct configuration
		// TODO this step could be omitted if the primary key was stored in the Configuration object
		try {
			campaignId = getJdbcTemplate().queryForInt(_selectCampaignId, new Object[] {awRequest.getCampaignUrn()});
		}
		catch (IncorrectResultSizeDataAccessException irsdae) { // this means that no rows were returned on the SQL returned more 
			                                                    // than one column -- either way, there is a logical error
			_logger.error("cannot retrieve campaign.id -- SQL [" + _selectCampaignId 
				+ "] returned no rows or multiple columns", irsdae);
			throw new DataAccessException(irsdae);
		}
		catch(org.springframework.dao.DataAccessException dae) {
		
			_logger.error("error running SQL: " + _selectCampaignId, dae);
			throw new DataAccessException(dae); 
		}
		
		// Prep for insert
		List<DataPacket> surveys = awRequest.getDataPackets(); // each survey is JSON stored as a String
		if(null == surveys) {
			throw new IllegalArgumentException("missing survey DataPackets in AwRequest");
		}
		final int userId = awRequest.getUser().getId();
		final String client = awRequest.getClient();
		int numberOfSurveys = surveys.size();
		int surveyIndex = 0;
		
		// The following variables are used in logging messages when errors occur
		SurveyDataPacket currentSurveyDataPacket = null;
		PromptResponseDataPacket currentPromptResponseDataPacket = null;
		int currentSurveyResponseId = -1;
		
		// Wrap all of the inserts in a transaction 
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("survey upload");
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		// Use a savepoint to handle nested rollbacks if duplicates are found
		Object savepoint = status.createSavepoint();
		
		try { // handle TransactionExceptions
			
			for(; surveyIndex < numberOfSurveys; surveyIndex++) { 
				
				 try { // handle DataAccessExceptions
					
					final SurveyDataPacket surveyDataPacket = (SurveyDataPacket) surveys.get(surveyIndex);
					currentSurveyDataPacket = surveyDataPacket; // this is annoying, but the currentSurveyDataPacket is used for 
					                                            // logging purposes outside of the try/catch because the locally final 
					                                            // surveyDataPacket cannot be defined as final outside of the loop's 
					                                            // scope. this "current" variable strategy is also used below for the
					                                            // same reason.
					
					KeyHolder idKeyHolder = new GeneratedKeyHolder();
					
					// First, insert the survey
					
					getJdbcTemplate().update(
						new PreparedStatementCreator() {
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps 
									= connection.prepareStatement(_insertSurveyResponse, Statement.RETURN_GENERATED_KEYS);
								ps.setInt(1, userId);
								ps.setInt(2, campaignId);
								ps.setTimestamp(3, Timestamp.valueOf(surveyDataPacket.getDate()));
								ps.setLong(4, surveyDataPacket.getEpochTime());
								ps.setString(5, surveyDataPacket.getTimezone());
								ps.setString(6, surveyDataPacket.getLocationStatus());
								ps.setString(7, surveyDataPacket.getLocation());
								ps.setString(8, surveyDataPacket.getSurveyId());
								ps.setString(9, surveyDataPacket.getSurvey());
								ps.setString(10, client);
								ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
								ps.setString(12, surveyDataPacket.getLaunchContext());
								try {
									ps.setInt(13, SurveyResponsePrivacyStateCache.instance().lookup(PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE)));
								} catch (CacheMissException e) {
									_logger.error("Error reading from the cache.", e);
									throw new SQLException(e);
								}
								return ps;
							}
						},
						idKeyHolder
					);
					
					savepoint = status.createSavepoint();
					
					final Number surveyResponseId = idKeyHolder.getKey(); // the primary key on the survey_response table for the 
					                                                      // just-inserted survey
					currentSurveyResponseId = surveyResponseId.intValue();
					
					// Now insert each prompt response from the survey
					
					List<PromptResponseDataPacket> promptResponseDataPackets = surveyDataPacket.getResponses();
					
					for(int i = 0; i < promptResponseDataPackets.size(); i++) {
						final PromptResponseDataPacket prdp = promptResponseDataPackets.get(i);	
						currentPromptResponseDataPacket = prdp;
						
						getJdbcTemplate().update(
							new PreparedStatementCreator() {
								public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
									PreparedStatement ps 
										= connection.prepareStatement(_insertPromptResponse);
									ps.setInt(1, surveyResponseId.intValue());
									ps.setString(2, prdp.getRepeatableSetId());
									if(null != prdp.getRepeatableSetIteration()) {
										ps.setInt(3, prdp.getRepeatableSetIteration());
									} else {
										ps.setNull(3, java.sql.Types.NULL);
									}
									ps.setString(4, prdp.getType());
									ps.setString(5, prdp.getPromptId());
									ps.setString(6, prdp.getValue());
									
									return ps;
								}
							}
						);
					}
				} catch (DataIntegrityViolationException dive) { // a unique index exists only on the survey_response table
					
					if(isDuplicate(dive)) {
						
						_logger.info("found a duplicate survey upload message");
						
						handleDuplicate(awRequest, surveyIndex);
						status.rollbackToSavepoint(savepoint);
						
					} else {
					
						// some other integrity violation occurred - bad!! All of the data to be inserted must be validated
						// before this DAO runs so there is either missing validation or somehow an auto_incremented key
						// has been duplicated
						
						_logger.error("caught DataAccessException", dive);
						logErrorDetails(currentSurveyDataPacket, userId, campaignId, currentSurveyResponseId, client);
						rollback(transactionManager, status, currentSurveyDataPacket, userId, campaignId, currentSurveyResponseId, client);
						throw new DataAccessException(dive);
					}
						
				} catch (org.springframework.dao.DataAccessException dae) { // some other database problem happened that prevented
					                                                        // the SQL from completing normally
					
					_logger.error("caught DataAccessException", dae);
					logErrorDetails(currentPromptResponseDataPacket, userId, campaignId, currentSurveyResponseId, client);
					rollback(transactionManager, status, currentSurveyDataPacket, userId, campaignId, currentSurveyResponseId, client);
					throw new DataAccessException(dae);
				} 
				
			}
			
			// Finally, commit the transaction
			transactionManager.commit(status);
			_logger.info("completed survey message persistence");
		} 
		
		catch (TransactionException te) { 
			
			_logger.error("failed to commit survey upload transaction, attempting to rollback", te);
			rollback(transactionManager, status, currentSurveyDataPacket, userId, campaignId, currentSurveyResponseId, client);
			logErrorDetails(currentSurveyDataPacket, userId, campaignId, currentSurveyResponseId, client);
			throw new DataAccessException(te);
		}
		
		_logger.info("Finished inserting responses into the database.");
	}
	
	/**
	 * Attempts to rollback a transaction. 
	 */
	private void rollback(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus, 
			DataPacket dp, int userId, int campaignConfigurationId, int surveyResponseId, String client) {
		
		try {
			
			_logger.error("rolling back a failed survey upload transaction");
			transactionManager.rollback(transactionStatus);
			
		} catch (TransactionException te) {
			
			_logger.error("failed to rollback survey upload transaction", te);
			logErrorDetails(dp, userId, campaignConfigurationId, surveyResponseId, client);
			throw new DataAccessException(te);
		}
	}
	
	private void logErrorDetails(DataPacket dp, int userId, int campaignConfigurationId, int surveyResponseId, String client) {
		if(dp == null) {
			_logger.error("An error occurred while running SQL. The DataPacket is null.");
			return;
		}
		else if(dp instanceof SurveyDataPacket) {
			
			SurveyDataPacket sdp = (SurveyDataPacket) dp;
			
			try {
				_logger.error("an error occurred when atempting to run this SQL '" + _insertSurveyResponse + "' with the following "
					+ "parameters: " + userId + ", " + campaignConfigurationId + ", " + sdp.getDate() + " , " + sdp.getEpochTime()
					+  ", " + sdp.getTimezone() + ", " + sdp.getLocationStatus() + ", " + sdp.getLocation() + ", " + sdp.getSurveyId() 
					+ ", " + sdp.getSurvey() + ", " + client + ", " + new Timestamp(System.currentTimeMillis()) + ", " + sdp.getLaunchContext()
					+ ", " + SurveyResponsePrivacyStateCache.instance().lookup(PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE)));
			} catch (CacheMissException e) {
				_logger.error("Cache miss while trying to print debugging information.", e);
				throw new DataAccessException(e);
			}
			
		} else {
			
			PromptResponseDataPacket prdp = (PromptResponseDataPacket) dp;
			
			_logger.error("an error occurred when atempting to run this SQL '" + _insertPromptResponse + "' with the following "
				+ "parameters: " + userId + ", " + surveyResponseId + ", " + prdp.getRepeatableSetId() + ", " + prdp.getType() 
				+ ", " + prdp.getPromptId() + ", " + prdp.getValue());
		}
	}
}
