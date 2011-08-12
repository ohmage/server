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

/**
 * Namespace-style exception for exceptions that can be thrown during an
 * arbitrary validation workflow. 
 * 
 * @author Joshua Selsky
 */
public class ValidationException extends WorkflowException {
	private static final long serialVersionUID = 2L;

	/**
	 * Creates a new service exception with only a message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 */
	public ValidationException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new service exception with a message and an indicator
	 * of the seriousness of the message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 * @param isSerious A boolean, that if true, marks this as an exception
	 *                  that will be logged with the level ERROR.
	 */
	public ValidationException(String message, boolean isSerious) {
		super(message, isSerious);
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
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Creates a new service exception from a previously thrown Throwable.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public ValidationException(Throwable cause) {
		super(cause);
	}
}
