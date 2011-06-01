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

import org.ohmage.util.StringUtils;

/**
 * Wrapper for error code data.
 * 
 * @author selsky
 */
public class ErrorResponse {
	private String _code;
	private String _text;
	
	public String getCode() {
		return _code;
	}
	
	public void setCode(String code) {
		if(StringUtils.isEmptyOrWhitespaceOnly(code)) {
			throw new IllegalArgumentException("code must not be empty");
		}
		_code = code;
	}
	
	public String getText() {
		return _text;
	}
	
	public void setText(String text) {
		if(StringUtils.isEmptyOrWhitespaceOnly(text)) {
			throw new IllegalArgumentException("text must not be empty");
		}
		_text = text;
	}
}
