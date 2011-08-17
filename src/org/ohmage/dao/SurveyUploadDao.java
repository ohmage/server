package org.ohmage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.User;
import org.ohmage.domain.upload.PromptResponse;
import org.ohmage.domain.upload.SurveyResponse;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.ohmage.request.Request;
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
public class SurveyUploadDao extends AbstractUploadDao {
	
	private static SurveyUploadDao instance;
	
	private static final Logger LOGGER = Logger.getLogger(SurveyUploadDao.class);
	
	private static final String SQL_INSERT_SURVEY_RESPONSE =
		"INSERT into survey_response " +
		"SET user_id = (SELECT id from user where username = ?), " +
		"campaign_id = (SELECT id from campaign where urn = ?), " +
		"msg_timestamp = ?, " +
		"epoch_millis = ?, " +
		"phone_timezone = ?, " +
		"location_status = ?, " +
		"location = ?, " +
		"survey_id = ?, " +
		"survey = ?, " +
		"client = ?, " +
		"upload_timestamp = ?, " +
		"launch_context = ?, " +
		"privacy_state_id = ?";
		
	private static final String SQL_INSERT_PROMPT_RESPONSE =
		"INSERT into prompt_response " +
        "(survey_response_id, repeatable_set_id, repeatable_set_iteration," +
        "prompt_type, prompt_id, response) " +
        "VALUES (?,?,?,?,?,?)";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private SurveyUploadDao(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * 
	 * @param request
	 * @param campaignUrn
	 * @param surveyUploadList
	 * @throws DataAccessException
	 */
	public static List<Integer> insertSurveys(Request request, final User user, final String client, final String campaignUrn, final List<SurveyResponse> surveyUploadList) 
		throws DataAccessException {
		
		List<Integer> duplicateIndexList = new ArrayList<Integer>();
		final String username = user.getUsername();
		int numberOfSurveys = surveyUploadList.size();
		int surveyIndex = 0;
		
		// The following variables are used in logging messages when errors occur
		SurveyResponse currentSurveyResponse = null;
		PromptResponse currentPromptResponse = null;
		String currentSql = null;
		
		// Wrap all of the inserts in a transaction 
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("survey upload");
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		// Use a savepoint to handle nested rollbacks if duplicates are found
		Object savepoint = status.createSavepoint();
		
		try { // handle TransactionExceptions
			
			for(; surveyIndex < numberOfSurveys; surveyIndex++) { 
				
				 try { // handle DataAccessExceptions
					
					final SurveyResponse surveyUpload = surveyUploadList.get(surveyIndex);
					currentSurveyResponse = surveyUpload; 
					currentSql = SQL_INSERT_SURVEY_RESPONSE;
			
					KeyHolder idKeyHolder = new GeneratedKeyHolder();
					
					// First, insert the survey
					
					instance.jdbcTemplate.update(
						new PreparedStatementCreator() {
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps 
									= connection.prepareStatement(SQL_INSERT_SURVEY_RESPONSE, Statement.RETURN_GENERATED_KEYS);
								ps.setString(1, username);
								ps.setString(2, campaignUrn);
								ps.setTimestamp(3, Timestamp.valueOf(surveyUpload.getDate()));
								ps.setLong(4, surveyUpload.getEpochTime());
								ps.setString(5, surveyUpload.getTimezone());
								ps.setString(6, surveyUpload.getLocationStatus());
								ps.setString(7, surveyUpload.getLocation());
								ps.setString(8, surveyUpload.getSurveyId());
								ps.setString(9, surveyUpload.getSurvey());
								ps.setString(10, client);
								ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
								ps.setString(12, surveyUpload.getLaunchContext());
								try {
									ps.setInt(13, SurveyResponsePrivacyStateCache.instance().lookup(PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE)));
								} catch (CacheMissException e) {
									LOGGER.error("Error reading from the cache.", e);
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
					currentSql = SQL_INSERT_PROMPT_RESPONSE;
					
					// Now insert each prompt response from the survey
					
					List<PromptResponse> promptUploadList = surveyUpload.getPromptResponses();
					
					for(int i = 0; i < promptUploadList.size(); i++) {
						final PromptResponse promptUpload = promptUploadList.get(i);	
						currentPromptResponse = promptUpload;
						
						instance.jdbcTemplate.update(
							new PreparedStatementCreator() {
								public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
									PreparedStatement ps 
										= connection.prepareStatement(SQL_INSERT_PROMPT_RESPONSE);
									ps.setInt(1, surveyResponseId.intValue());
									ps.setString(2, promptUpload.getRepeatableSetId());
									if(null != promptUpload.getRepeatableSetIteration()) {
										ps.setInt(3, Integer.parseInt(promptUpload.getRepeatableSetIteration()));
									} else {
										ps.setNull(3, java.sql.Types.NULL);
									}
									ps.setString(4, promptUpload.getType());
									ps.setString(5, promptUpload.getPromptId());
									ps.setString(6, promptUpload.getValue());
									
									return ps;
								}
							}
						);
					}
					
				} catch (DataIntegrityViolationException dive) { // a unique index exists only on the survey_response table
					
					if(instance.isDuplicate(dive)) {
						 
						LOGGER.debug("Found a duplicate survey upload message for user " + username);
						
						duplicateIndexList.add(surveyIndex);
						status.rollbackToSavepoint(savepoint);
						
					} 
					else {
					
						// Some other integrity violation occurred - bad!! All 
						// of the data to be inserted must be validated before 
						// this DAO runs so there is either missing validation 
						// or somehow an auto_incremented key has been duplicated.
						
						LOGGER.error("Caught DataAccessException", dive);
						logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
						rollback(transactionManager, status);
						throw new DataAccessException(dive);
					}
						
				} catch (org.springframework.dao.DataAccessException dae) { 
					
					// Some other database problem happened that prevented
                    // the SQL from completing normally.
					
					LOGGER.error("caught DataAccessException", dae);
					logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
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
			logErrorDetails(currentSurveyResponse, currentPromptResponse, currentSql, username, campaignUrn);
			throw new DataAccessException(te);
		}
		
		LOGGER.info("Finished inserting responses into the database.");
		return duplicateIndexList;
	}
	
	/**
	 * Attempts to rollback a transaction. 
	 */
	private static void rollback(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus) 
		throws DataAccessException {
		
		try {
			
			LOGGER.error("rolling back a failed survey upload transaction");
			transactionManager.rollback(transactionStatus);
			
		} catch (TransactionException te) {
			
			LOGGER.error("failed to rollback survey upload transaction", te);
			throw new DataAccessException(te);
		}
	}
	
	private static void logErrorDetails(SurveyResponse surveyResponse, PromptResponse promptResponse, String sql, String username,
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
}
