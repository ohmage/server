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
 * Immutable wrapper for prompt values and labels. 
 * 
 * @author Joshua Selsky
 */
public class SingleChoicePromptValueAndLabel {
	
	private Object _value;
	private String _label;
	
	public SingleChoicePromptValueAndLabel(Object value, String label) {
		_value = value;
		_label = label;
	}
	
	public Object getValue() {
		return _value;
	}
	
	public String getLabel() {
		return _label;
	}

	@Override
	public String toString() {
		return "SingleChoicePromptValueAndLabel [_value=" + _value
				+ ", _label=" + _label + "]";
	}
}
