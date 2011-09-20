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

import java.util.Collections;
import java.util.Map;


/**
 * Representation of prompt configuration data.
 * 
 * @author Joshua Selsky
 */
public class Prompt extends AbstractSurveyItem {
	private String displayType;
	private String type;
	private Map<String, PromptProperty> properties;
	private boolean skippable;
	private String displayLabel;
	private String unit;
	private String text;
	private int index;

	public Prompt(String id, String displayType, String type, Map<String, PromptProperty> props, boolean skippable, 
			String displayLabel, String unit, String text, int index) {
		
		super(id);
		this.displayType = displayType;
		this.type = type;
		properties = props; // TODO really need a deep copy here
		this.skippable = skippable;
		this.displayLabel = displayLabel;
		this.unit = unit;
		this.text = text;
		this.index = index;
	}

	public String getText() {
		return text;
	}
	
	public String getDisplayType() {
		return displayType;
	}

	public String getType() {
		return type;
	}
	
	public String getUnit() {
		return unit;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}
	
	public Map<String, PromptProperty> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public boolean isSkippable() {
		return skippable;
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "Prompt [displayType=" + displayType + ", type=" + type
				+ ", properties=" + properties + ", skippable=" + skippable
				+ ", displayLabel=" + displayLabel + ", unit=" + unit
				+ ", text=" + text + ", index=" + index + "]";
	}
}
