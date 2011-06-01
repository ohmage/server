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

public class PromptResponseDataPacket implements DataPacket {
	private String _promptId;   
	private String _repeatableSetId; 
	private Integer _repeatableSetIteration;
	private String _value;
	private String _type;
	
	public PromptResponseDataPacket() { }
	
	public String getPromptId() {
		return _promptId;
	}
	
	public void setPromptId(String promptId) {
		_promptId = promptId;
	}
	
	public void setRepeatableSetId(String repeatableSetId) {
		_repeatableSetId = repeatableSetId;
	}
	
	public String getRepeatableSetId() {
		return _repeatableSetId;
	}

	public void setRepeatableSetIteration(Integer repeatableSetIteration) {
		_repeatableSetIteration = repeatableSetIteration;
	}
	
	public Integer getRepeatableSetIteration() {
		return _repeatableSetIteration;
	}

	public String getValue() {
		return _value;
	}
	
	public void setValue(String value) {
		_value = value;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public String toString() {
		return "PromptResponseDataPacket [_promptId=" + _promptId
				+ ", _repeatableSetId=" + _repeatableSetId
				+ ", _repeatableSetIteration=" + _repeatableSetIteration
				+ ", _type=" + _type + ", _value=" + _value + "]";
	}
}
