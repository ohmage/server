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
package org.ohmage.exception;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.request.Request;

/**
 * Abstract base class for checked Exceptions that may occur in various
 * application workflows. 
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public abstract class WorkflowException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final Annotator annotator;

	/**
	 * Creates a new service exception with only a message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 */
	public WorkflowException(final String message) {
		super(message);
		
		annotator = null;
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
	public WorkflowException(final String message, final Throwable cause) {
		super(message, cause);
		
		if(cause instanceof WorkflowException) {
			annotator = ((WorkflowException) cause).annotator;
		}
		else {
			annotator = null;
		}
	}
	
	/**
	 * Creates a new service exception from a previously thrown Throwable.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public WorkflowException(final Throwable cause) {
		super(cause);
		
		if(cause instanceof WorkflowException) {
			annotator = ((WorkflowException) cause).annotator;
		}
		else {
			annotator = null;
		}
	}
	
	/**
	 * Creates an exception with an error code and error text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 */
	public WorkflowException(final ErrorCode errorCode, 
			final String errorText) {
		
		super(errorText);
		
		annotator = new Annotator(errorCode, errorText);
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
	public WorkflowException(final ErrorCode errorCode,
			final String errorText, final String message) {
		
		super(message);
		
		annotator = new Annotator(errorCode, errorText);
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
	public WorkflowException(final ErrorCode errorCode, 
			final String errorText, final String message, 
			final Throwable cause) {
		
		super(message, cause);
		
		annotator = new Annotator(errorCode, errorText);
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
	public WorkflowException(final ErrorCode errorCode,
			final String errorText, final Throwable cause) {
		
		super(cause);
		
		annotator = new Annotator(errorCode, errorText);
	}
	
	/**
	 * Sets a request as failed based on the error code and error text in this
	 * exception if any is present. If not, it will fail it with whatever the
	 * request's general failure message is.
	 * 
	 * @param request The request to fail.
	 */
	public void failRequest(final Request request) {
		if(annotator == null) {
			request.setFailed();
		}
		else {
			request.setFailed(annotator.getErrorCode(), annotator.getErrorText());
		}
	}
	
	/**
	 * Logs this exception into some 'logger'. This is preferable to passing 
	 * the exception to the logger as this will keep track of the seriousness
	 * of the exception and will limit what is output to the log when it is not
	 * a serious exception.
	 * 
	 * @param logger The Logger to which this exception should be output.
	 */
	public void logException(final Logger logger) {
		Throwable cause = this.getCause();
		if(cause == null) {
			logger.info(toString());
		}
		else {
			logger.error(toString(), this);
		}
	}
}