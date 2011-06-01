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

/**
 * @author selsky
 */
public class DataPointFunctionQueryMetadata {
	private String _label;
	private String _type;
	private String _unit;
	
	public String getLabel() {
		return _label;
	}
	
	public void setLabel(String label) {
		_label = label;
	}
	
	public String getType() {
		return _type;
	}
	
	public void setType(String type) {
		_type = type;
	}
	
	public String getUnit() {
		return _unit;
	}
	
	public void setUnit(String unit) {
		_unit = unit;
	}
	
	public boolean isEmpty() {
		return (_label == null && _type == null && _unit == null);
	}
}
