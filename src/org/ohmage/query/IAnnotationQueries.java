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
package org.ohmage.query;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTimeZone;
import org.ohmage.domain.Annotation;
import org.ohmage.exception.DataAccessException;

/**
 * Interface to facilitate mocking concrete implementations for test cases. 
 * 
 * @author Joshua Selsky
 */
public interface IAnnotationQueries {
	/**
	 * Creates a survey response annotation.
	 * 
	 * @param annotationUuid the id used to uniquely identify the annotation
	 * @param client the software client making the request
	 * @param time the milliseconds since the UNIX epoch
	 * @param timezone the timezone of the system making the request
	 * @param annotationText the annotation text
	 * @param surveyId the UUID of the survey response
	 * 
	 * @throws DataAccessException if an error occurs
	 */
	void createSurveyResponseAnnotation(
		UUID annotationUuid,
		String client,
		Long time,
		DateTimeZone timezone,
		String annotationText,
		UUID surveyId
	) throws DataAccessException;

	/**
	 * Retrieves annotations for the provided survey id.
	 * 
	 * @param surveyId the survey id
	 * @return a list of annotations bound to the survey
	 * @throws DataAccessException if an error occurs
	 */
	List<Annotation> readSurveyResponseAnnotations(UUID surveyId) throws DataAccessException;
	
	/**
	 * Creates a prompt response annotation.
	 * 
	 * @param annotationUuid the id used to uniquely identify the annotation
	 * @param client the software client making the request
	 * @param time the milliseconds since the UNIX epoch
	 * @param timezone the timezone of the system making the request
	 * @param annotationText the annotation text
	 * @param promptResponseId the id of the prompt response to which the
	 * annotation should be attached
	 * 
	 * @throws DataAccessException if an error occurs
	 */
	void createPromptResponseAnnotation(
		UUID annotationUuid,
		String client,
		Long time,
		DateTimeZone timezone,
		String annotationText,
		Integer promptResponseId
	) throws DataAccessException;
	
	/**
	 * Retrieves annotations for the provided prompt response information.
	 * 
	 * @param surveyId the survey id
	 * @return a list of annotations bound to the survey
	 * @throws DataAccessException if an error occurs
	 */
	List<Annotation> readPromptResponseAnnotations(
		UUID surveyId,
		String promptId,
		String repeatableSetId,
		Integer repeatableSetIteration
	) throws DataAccessException;
	
	/**
	 * @param username      The user who ostensibly owns the annotation.  
	 * @param annotationId  The id of the annotation.
	 * @return  true if the user is the owner of the annotation, false
	 *          otherwise
	 * @throws DataAccessException if an error occurs
	 */
	boolean userOwnsAnnotation(
		String username,
		UUID annotationId
	) throws DataAccessException;
	
	/**
	 * @param annotationId     The id of the annotation to update.
	 * @param annotationText   The updated annotation text.
	 * @param client           The updated client.
	 * @param time             The milliseconds since the epoch.
	 * @param timezone         The timezone for the above milliseconds value.              
	 * @throws DataAccessException if an error occurs
	 */
	void updateAnnotation(
	    UUID annotationId,
	    String annotationText,
	    String client,
	    long time,
	    DateTimeZone timezone
	) throws DataAccessException;
	
	
}
