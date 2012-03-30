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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.joda.time.DateTimeZone;
import org.ohmage.domain.Annotation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.IAnnotationQueries;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
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
		"(uuid, epoch_millis, timezone, client, annotation, creation_timestamp) " +
		"VALUES (?, ?, ?, ?, ?, now())";
	
	private static final String SQL_INSERT_SURVEY_RESPONSE_ANNOTATION =
		"INSERT into survey_response_annotation " +
		"(survey_response_id, annotation_id) " +
		"VALUES ((SELECT id from survey_response where uuid = ?), ?)";
	
	private static final String SQL_INSERT_PROMPT_RESPONSE_ANNOTATION =
		"INSERT into prompt_response_annotation " +
		"(prompt_response_id, annotation_id) " +
		"VALUES (?, ?)";
	
	private static final String SQL_READ_SURVEY_RESPONSE_ANNOTATION = 
		"SELECT a.uuid, a.annotation, a.epoch_millis, a.timezone " +
		"FROM survey_response sr, survey_response_annotation sra, annotation a " +
		"WHERE sr.uuid = ? " +
		"AND sr.id = sra.survey_response_id " +
		"AND sra.annotation_id = a.id";

	private static final String SQL_READ_PROMPT_RESPONSE_ANNOTATION = 
		"SELECT a.uuid, a.annotation, a.epoch_millis, a.timezone " +
		"FROM prompt_response pr, survey_response sr, prompt_response_annotation pra, annotation a " +
		"WHERE sr.uuid = ? " +
		"AND pr.survey_response_id = sr.id " +
		"AND pr.prompt_id = ? " +
		"AND pr.id = pra.prompt_response_id " +
		"AND pra.annotation_id = a.id";
	
	private static final String SQL_READ_PROMPT_RESPONSE_ANNOTATION_AND_REPEATABLE_SET =
		" AND repeatable_set_id = ? " +
		"AND repeatable_set_iteration = ?";
	
	private static final String SQL_ANNOTATION_EXISTS_FOR_USER = 
		"SELECT EXISTS" +
		" (SELECT id FROM annotation" +
		" WHERE user_id = (SELECT id from user WHERE username = ?)" +
		" AND uuid = ?)";

	private static final String SQL_UPDATE_ANNOTATION = 
		"UPDATE annotation " +
		"SET epoch_millis = ?, timezone = ?, client = ?, annotation = ? " +
		"WHERE uuid = ?";
	
	private static final String SQL_DELETE_ANNOTATION =
		"DELETE from annotation WHERE uuid = ?";
	
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private AnnotationQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void createSurveyResponseAnnotation(final UUID annotationId,
		final String client, final Long time, final DateTimeZone timezone, final String annotationText, final UUID surveyId)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new survey response annotation.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			long id = 0;
				
			try {
				id = insertAnnotation(annotationId, time, timezone, client, annotationText);
			}
			catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_ANNOTATION + "' with parameters: " +
							annotationId + ", " + 
						    time + ", " + 
							timezone.getID() + ", " + 
							client + ", " + 
							((annotationText.length() > 25) ? annotationText.substring(0, 25) + "..." : annotationText),
						e);
			}
			
			try {
				// Insert the link between the survey_response and its annotation
				getJdbcTemplate().update(SQL_INSERT_SURVEY_RESPONSE_ANNOTATION, surveyId.toString(), id);
				
			} 
			catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_SURVEY_RESPONSE_ANNOTATION + "' with parameters: " +
						    surveyId.toString() + ", " + 
						    id,
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
	public List<Annotation> readSurveyResponseAnnotations(final UUID surveyId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(SQL_READ_SURVEY_RESPONSE_ANNOTATION, new Object[] {surveyId.toString() }, 
				new RowMapper<Annotation>() {
					public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException {
						try {
							return new Annotation(
								rs.getString(1),
								rs.getString(2),
								rs.getLong(3),
								rs.getString(4)
							);
						}
						catch(DomainException e) {
							throw new SQLException("Error creating an annotation object.", e);
						}
					}
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("An error occurred when running the following SQL: '" 
				+ SQL_READ_SURVEY_RESPONSE_ANNOTATION + " with the parameter " + surveyId, e);
		}
	}
	
	@Override
	public void createPromptResponseAnnotation(final UUID annotationId, final String client, final Long time,
		final DateTimeZone timezone, final String annotationText, Integer promptResponseId)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new prompt response annotation.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			long id = 0;
			
			try {
				id = insertAnnotation(annotationId, time, timezone, client, annotationText);
			}
			catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_ANNOTATION + "' with parameters: " +
							annotationId + ", " + 
						    time + ", " + 
							timezone.getID() + ", " + 
							client + ", " + 
							((annotationText.length() > 25) ? annotationText.substring(0, 25) + "..." : annotationText),
						e);
			}
			try {
				// Insert the link between the prompt_response and its annotation
				getJdbcTemplate().update(SQL_INSERT_PROMPT_RESPONSE_ANNOTATION, promptResponseId, id);
				
			}
			catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_PROMPT_RESPONSE_ANNOTATION + "' with parameters: " +
						    promptResponseId + ", " + 
						    id,
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
	public List<Annotation> readPromptResponseAnnotations(final UUID surveyId, final String promptId, final String repeatableSetId,
		final Integer repeatableSetIteration)
			throws DataAccessException {
		
		StringBuilder sql = new StringBuilder(SQL_READ_PROMPT_RESPONSE_ANNOTATION);
		List<Object> args = new ArrayList<Object>();
		args.add(surveyId.toString());
		args.add(promptId);
		
		if(repeatableSetId != null && repeatableSetIteration != null) {
			sql.append(SQL_READ_PROMPT_RESPONSE_ANNOTATION_AND_REPEATABLE_SET);
			args.add(repeatableSetId);
			args.add(repeatableSetIteration);
		}
		
		try {			
			return getJdbcTemplate().query(sql.toString(), args.toArray(), 
				new RowMapper<Annotation>() {
					public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException {
						try {
							return new Annotation(
								rs.getString(1),
								rs.getString(2),
								rs.getLong(3),
								rs.getString(4)
							);
						}
						catch(DomainException e) {
							throw new SQLException("Error creating an annotation object.", e);
						}
					}
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("An error occurred when running the following SQL: '" 
				+ sql.toString() + " with the parameters " + args, e);
		}
	}
	
	@Override
	public boolean userOwnsAnnotation(String username, UUID annotationId) 
			throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_ANNOTATION_EXISTS_FOR_USER, new Object[] { username, annotationId.toString() }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_ANNOTATION_EXISTS_FOR_USER + "' with parameters: " 
					                      + username + ", " + annotationId, e);
		}
	}
	
	/**
	 * Helper method to insert an annotation and allow the other methods in
	 * this class to do the work of linking the annotation to the appropriate
	 * entity.
	 * 
	 * @param annotationId a UUID to uniquely identify this annotation
	 * @param time the epoch millis at which the annotation was created
	 * @param timezone the timezone in which the annotation was created
	 * @param client the software client that generated the annotation request
	 * @param text the annotation text
	 * @return the primary key of the newly created annotation
	 * @throws org.springframework.dao.DataAccessException if an error occurs
	 */
	private long insertAnnotation(final UUID annotationId, final Long time, final DateTimeZone timezone, 
		final String client, final String annotationText)
			throws org.springframework.dao.DataAccessException {
		
		final KeyHolder annotationIdKeyHolder = new GeneratedKeyHolder();
		
		getJdbcTemplate().update(
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(SQL_INSERT_ANNOTATION, new String[] {"id"});
					ps.setString(1, annotationId.toString());
					ps.setLong(2, time);
					ps.setString(3, timezone.getID());
					ps.setString(4, client);
					ps.setString(5, annotationText);
					return ps;
				}
			},
			annotationIdKeyHolder
		);
		
		return annotationIdKeyHolder.getKey().longValue();
	}
	
	@Override
	public void updateAnnotation(final UUID annotationId, final String annotationText, final String client, 
		final long time, final DateTimeZone timezone) 
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating an annotation.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(
					SQL_UPDATE_ANNOTATION, 
					time, timezone.getID(), client, annotationText, annotationId.toString() 
				);
			}
			catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + SQL_UPDATE_ANNOTATION + "' with parameters: " +  
						time + ", " + timezone.getID() + ", " + client + ", " + annotationText + ", " + annotationId.toString(), 
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
	public void deleteAnnotation(final UUID annotationId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting an annotation.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(
					SQL_DELETE_ANNOTATION, annotationId.toString() 
				);
			}
			catch(org.springframework.dao.DataAccessException e) {
				
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + SQL_DELETE_ANNOTATION + "' with parameter: " + annotationId.toString(), 
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
