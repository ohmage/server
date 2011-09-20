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
 * Bean-style wrapper for an immutable prompt property. A prompt property
 * defines bounds or options on a prompt type.
 * 
 * @author Joshua Selsky
 */
public class PromptProperty {
	private String key;   
	private String value; // an optional visualization-specific value for range-bound properties 
	private String label; // a required label describing a selection or bound
	
	public PromptProperty(String key, String value, String label) {
		this.key = key;
		this.value = value;
		this.label = label;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "PromptProperty [key=" + key + ", label=" + label
				+ ", value=" + value + "]";
	}
}
