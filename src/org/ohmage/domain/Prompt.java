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

import java.util.Collections;
import java.util.Map;

/**
 * A survey prompt configuration.
 * 
 * @author selsky
 */
public class Prompt extends AbstractSurveyItem {
	private String _displayType;
	private String _type;
	private Map<String, PromptProperty> _properties;
	private boolean _skippable;
	private String _displayLabel;
	private String _unit;
	private String _text;
	private int _index;

	public Prompt(String id, String displayType, String type, Map<String, PromptProperty> props, boolean skippable, 
			String displayLabel, String unit, String text, int index) {
		
		super(id);
		_displayType = displayType;
		_type = type;
		_properties = props; // TODO really need a deep copy here
		_skippable = skippable;
		_displayLabel = displayLabel;
		_unit = unit;
		_text = text;
		_index = index;
	}

	public String getText() {
		return _text;
	}
	
	public String getDisplayType() {
		return _displayType;
	}

	public String getType() {
		return _type;
	}
	
	public String getUnit() {
		return _unit;
	}

	public String getDisplayLabel() {
		return _displayLabel;
	}
	
	public Map<String, PromptProperty> getProperties() {
		return Collections.unmodifiableMap(_properties);
	}

	public boolean isSkippable() {
		return _skippable;
	}
	
	public int getIndex() {
		return _index;
	}

	@Override
	public String toString() {
		return "Prompt [_displayType=" + _displayType + ", _type=" + _type
				+ ", _properties=" + _properties + ", _skippable=" + _skippable
				+ ", _displayLabel=" + _displayLabel + ", _unit=" + _unit
				+ ", _text=" + _text + ", _index=" + _index + "]";
	}
}
