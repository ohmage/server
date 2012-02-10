/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.lib.exception;

import org.ohmage.annotator.Annotator.ErrorCode;

public class RequestErrorException extends ApiException {
	private static final long serialVersionUID = 1L;
	
	private final ErrorCode errorCode;
	private final String errorText;
	
	public RequestErrorException(final ErrorCode errorCode, String errorText) {
		super("The request returned an error.");
		
		this.errorCode = errorCode;
		this.errorText = errorText;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	
	public String getErrorText() {
		return errorText;
	}
	
	@Override
	public String toString() {
		return getErrorCode().toString() + ": " + getErrorText();
	}
}
