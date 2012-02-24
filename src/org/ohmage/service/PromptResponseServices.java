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
package org.ohmage.service;

import java.util.UUID;

import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IPromptResponseQueries;

/**
 * Services for prompt responses.
 * 
 * @author Joshua Selsky
 */
public final class PromptResponseServices {
	
//	private static final Logger LOGGER = Logger.getLogger(PromptResponseServices.class);
	private static PromptResponseServices instance;
	
	private IPromptResponseQueries promptResponseQueries;
	
	private PromptResponseServices(IPromptResponseQueries promptResponseQueries) {
		if(instance != null) {
			throw new IllegalStateException("Only one instance of this class is allowed.");
		}
		if(promptResponseQueries == null) {
			throw new IllegalArgumentException("An instance of IPromptResponseQueries is required.");
		}
		
		this.promptResponseQueries = promptResponseQueries; 
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static PromptResponseServices instance() {
		return instance;
	}
	
	/**
	 * Finds the prompt response id for the provided parameters.
	 * 
	 * @param surveyResponseId
	 * @param promptId
	 * @param repeatableSetId
	 * @param repeatableSetIteration
	 * @return
	 * @throws ServiceException if an error occurs
	 */
	public Integer findPromptResponseIdFor(UUID surveyResponseId, String promptId, String repeatableSetId, Integer repeatableSetIteration) 
	    throws ServiceException {
		
		try {
			return promptResponseQueries.retrievePromptResponseIdFor(surveyResponseId, promptId, repeatableSetId, repeatableSetIteration);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}				
	}
}
