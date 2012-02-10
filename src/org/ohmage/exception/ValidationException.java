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
 * Namespace-style exception for exceptions that can be thrown during an
 * arbitrary validation workflow. 
 * 
 * @author Joshua Selsky
 */
public class ValidationException extends WorkflowException {
	private static final long serialVersionUID = 2L;

	/**
	 * Creates a new exception with only a message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 */
	public ValidationException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with a message as to why it's being thrown and 
	 * another Throwable that may have caused this exception to be thrown.
	 * 
	 * @param message A String describing why this exception is being thrown.
	 * 
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Creates a new exception from a previously thrown Throwable.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public ValidationException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates a new exception with an error code and error text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 */
	public ValidationException(final ErrorCode errorCode, 
			final String errorText) {
		
		super(errorCode, errorText);
	}
	
	/**
	 * Creates an exception with an error code, error text, and a custom 
	 * message that will be printed to the log instead of the error text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param message The message for the server log.
	 */
	public ValidationException(final ErrorCode errorCode,
			final String errorText, final String message) {
		
		super(errorCode, errorText, message);
	}
	
	/**
	 * Creates an exception with an error code, error text, a custom message 
	 * that will be printed to the log instead of the error text, and another
	 * Throwable that caused this exception.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param message The message for the server log.
	 * 
	 * @param cause The Throwable that caused this exception.
	 */
	public ValidationException(final ErrorCode errorCode, 
			final String errorText, final String message, 
			final Throwable cause) {
		
		super(errorCode, errorText, message, cause);
	}
	
	/**
	 * Creates an exception with an error code, error text, and another 
	 * Throwable that caused this exception.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param cause The Throwable that caused this exception.
	 */
	public ValidationException(final ErrorCode errorCode,
			final String errorText, final Throwable cause) {
		
		super(errorCode, errorText, cause);
	}
}
