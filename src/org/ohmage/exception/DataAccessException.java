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
package org.ohmage.exception;

import org.ohmage.annotator.Annotator.ErrorCode;

/**
 * Simple wrapper for RuntimeExceptions that are thrown from the query package. 
 * 
 * @author Joshua Selsky
 */
public class DataAccessException extends WorkflowException {
	private static final long serialVersionUID = 4L;

	/**
	 * Creates a new service exception with only a message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 */
	public DataAccessException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new service exception with a message as to why it's being 
	 * thrown and another Throwable that may have caused this exception to be
	 * thrown.
	 * 
	 * @param message A String describing why this exception is being thrown.
	 * 
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new service exception with an errorCode and message as to why it's being 
	 * thrown and another Throwable that may have caused this exception to be
	 * thrown.
	 * 
	 * @param errorCode  The error code.
	 * 
	 * @param errorText  The error text.
	 * 
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public DataAccessException(final ErrorCode errorCode, final String errorText, final Throwable cause) {
		super(errorCode, errorText, cause);
	}
	
	/**
	 * Creates a new service exception from a previously thrown Throwable.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public DataAccessException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates a new exception with an error code, error text, and cause.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public DataAccessException(
		final ErrorCode errorCode, 
		final String errorText,
		final Throwable cause) {
		
		super(errorCode, errorText, cause);
	}
}
