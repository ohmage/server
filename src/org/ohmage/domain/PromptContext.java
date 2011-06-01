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
package org.ohmage.domain;

import java.util.Map;


/**
 * Bag of prompt info for data point/export API.
 * 
 * @author selsky
 */
public class PromptContext {
	
	private String _id;
	private String _type;
	private String _displayLabel;
	private String _displayType;
	private String _unit;
	private String _text;
	private Map<String, PromptProperty> _choiceGlossary; // if single_choice or multi_choice
	
	public String getText() {
		return _text;
	}
	
	public void setText(String text) {
		_text = text;
	}
	
	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	public String getDisplayLabel() {
		return _displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		_displayLabel = displayLabel;
	}

	public String getDisplayType() {
		return _displayType;
	}

	public void setDisplayType(String displayType) {
		_displayType = displayType;
	}

	public String getUnit() {
		return _unit;
	}

	public void setUnit(String unit) {
		_unit = unit;
	}
	
	public Map<String, PromptProperty> getChoiceGlossary() {
		return _choiceGlossary;
	}

	public void setChoiceGlossary(Map<String, PromptProperty> choiceGlossary) {
		_choiceGlossary = choiceGlossary;
	}

	@Override
	public String toString() {
		return "PromptContext [_choiceGlossary=" + _choiceGlossary
				+ ", _displayLabel=" + _displayLabel + ", _displayType="
				+ _displayType + ", _id=" + _id + ", _type=" + _type
				+ ", _unit=" + _unit + "]";
	}
}
