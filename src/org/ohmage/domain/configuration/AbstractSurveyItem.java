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
package org.ohmage.domain.configuration;

/**
 * Abstract base class for survey items that use IDs: surveys, repeatable_sets,
 * and prompts.
 * 
 * @author Joshua Selsky
 */
public abstract class AbstractSurveyItem implements SurveyItem {
	private String id;
	
	public AbstractSurveyItem(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}
}
