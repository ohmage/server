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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IAnnotationQueries;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Default implementation of annotation queries that talk to our MySQL db.
 * 
 * @author Joshua Selsky
 */
public class AnnotationQueries extends Query implements IAnnotationQueries {

	private static final String SQL_INSERT_ANNOTATION =
		"INSERT into annotation " +
		"(uuid, epoch_millis, timezone, client, annotation) " +
		"VALUES (?, ?, ?, ?, ?)";
	
	private static final String SQL_INSERT_SURVEY_RESPONSE_ANNOTATION =
		"INSERT into survey_response_annotation " +
		"(survey_response_id, annotation_id) " +
		"VALUES ((SELECT id from survey_response where uuid = ?), ?)";
	
	private static final String SQL_INSERT_PROMPT_RESPONSE_ANNOTATION =
		"INSERT into prompt_response_annotation " +
		"(prompt_response_id, annotation_id) " +
		"VALUES (?, ?)";

	
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private AnnotationQueries(DataSource dataSource) {
		super(dataSource);
	}

	
	@Override
	public void createSurveyResponseAnnotation(final UUID annotationUuid,
		final String client, final Long time, final TimeZone timezone, final String annotationText, final UUID surveyId)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new survey response annotation.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			final KeyHolder annotationIdKeyHolder = new GeneratedKeyHolder();
			
			try {
				// Insert the annotation
				getJdbcTemplate().update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(SQL_INSERT_ANNOTATION, new String[] {"id"});
							ps.setString(1, annotationUuid.toString());
							ps.setLong(2, time);
							ps.setString(3, timezone.getID());
							ps.setString(4, client);
							ps.setString(5, annotationText);
							return ps;
						}
					},
					annotationIdKeyHolder
				);
				
				
			} catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_ANNOTATION + "' with parameters: " +
							annotationUuid + ", " + 
						    time + ", " + 
							timezone.getID() + ", " + 
							client + ", " + 
							((annotationText.length() > 25) ? annotationText.substring(0, 25) + "..." : annotationText),
						e);
			}
			
			
			try {
				
				// Insert the link between survey_response and annotation
				getJdbcTemplate().update(
					SQL_INSERT_SURVEY_RESPONSE_ANNOTATION, surveyId.toString(), annotationIdKeyHolder.getKey().longValue()
				);
				
			} catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_SURVEY_RESPONSE_ANNOTATION + "' with parameters: " +
						    surveyId.toString() + ", " + 
						    annotationIdKeyHolder.getKey().longValue(),
						e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}

		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	@Override
	public void createPromptResponseAnnotation(final UUID annotationUuid, final String client, final Long time,
		final TimeZone timezone, final String annotationText, Integer promptResponseId)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new prompt response annotation.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			final KeyHolder annotationIdKeyHolder = new GeneratedKeyHolder();
			
			try {
				
				// FIXME -- move this to it's own method
				
				// Insert the annotation
				getJdbcTemplate().update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(SQL_INSERT_ANNOTATION, new String[] {"id"});
							ps.setString(1, annotationUuid.toString());
							ps.setLong(2, time);
							ps.setString(3, timezone.getID());
							ps.setString(4, client);
							ps.setString(5, annotationText);
							return ps;
						}
					},
					annotationIdKeyHolder
				);
				
				
			} catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_ANNOTATION + "' with parameters: " +
							annotationUuid + ", " + 
						    time + ", " + 
							timezone.getID() + ", " + 
							client + ", " + 
							((annotationText.length() > 25) ? annotationText.substring(0, 25) + "..." : annotationText),
						e);
			}
			
			
			try {
				
				// Insert the link between survey_response and annotation
				getJdbcTemplate().update(
					SQL_INSERT_PROMPT_RESPONSE_ANNOTATION, promptResponseId, annotationIdKeyHolder.getKey().longValue()
				);
				
			} catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_PROMPT_RESPONSE_ANNOTATION + "' with parameters: " +
						    promptResponseId + ", " + 
						    annotationIdKeyHolder.getKey().longValue(),
						e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}

		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}

}
