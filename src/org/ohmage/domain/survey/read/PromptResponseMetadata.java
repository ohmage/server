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
package org.ohmage.domain.survey.read;

/**
 * Bean-style wrapper for metadata associated with a prompt response.
 * 
 * @author Joshua Selsky
 */
public class PromptResponseMetadata {
	private String _displayLabel;
	private String _displayType;
	private String _promptText;
	private String _promptType;
	private String _unit;
	
	public PromptResponseMetadata(String displayLabel, String displayType, String promptText, String promptType, String unit) {
		_displayLabel = displayLabel;
		_displayType = displayType;
		_promptText = promptText;
		_promptType = promptType;
		_unit = unit;
	}
	
	public String getPromptText() {
		return _promptText;
	}

	public String getDisplayType() {
		return _displayType;
	}

	public String getPromptType() {
		return _promptType;
	}
	
	public String getUnit() {
		return _unit;
	}

	public String getDisplayLabel() {
		return _displayLabel;
	}

	@Override
	public String toString() {
		return "PromptResponseMetadata [_displayLabel=" + _displayLabel
				+ ", _displayType=" + _displayType + ", _promptText="
				+ _promptText + ", _promptType=" + _promptType + ", _unit="
				+ _unit + "]";
	}
}
