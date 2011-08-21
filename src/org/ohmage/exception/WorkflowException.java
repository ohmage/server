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

/**
 * Abstract base class for checked Exceptions that may occur in various
 * application workflows. 
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public abstract class WorkflowException extends Exception {
	private static final long serialVersionUID = 1L;
	
	// Keeps track of the seriousness of the exception. If it is created with a
	// message then it is not considered serious as this is how services report
	// that they have failed, but not that there is an issue. If it created 
	// with a Throwable, then there is a more pressing exception being thrown
	// and that makes it serious.
	private final boolean isSerious;

	/**
	 * Creates a new service exception with only a message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 */
	public WorkflowException(String message) {
		super(message);
		
		isSerious = false;
	}
	
	/**
	 * Creates a new service exception with a message and an indicator
	 * of the seriousness of the message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 * @param isSerious A boolean, that if true, marks this as an exception
	 *                  that will be logged with the level ERROR.
	 */
	public WorkflowException(String message, boolean isSerious) {
		super(message);
		
		this.isSerious = isSerious;
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
	public WorkflowException(String message, Throwable cause) {
		super(message, cause);
		
		isSerious = true;
	}
	
	/**
	 * Creates a new service exception from a previously thrown Throwable.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public WorkflowException(Throwable cause) {
		super(cause);
		
		isSerious = true;
	}
	
	/**
	 * Logs this exception into some 'logger'. This is preferable to passing 
	 * the exception to the logger as this will keep track of the seriousness
	 * of the exception and will limit what is output to the log when it is not
	 * a serious exception.
	 * 
	 * @param logger The Logger to which this exception should be output.
	 */
	public void logException(Logger logger) {
		if(isSerious) {
			logger.error(toString(), this);
		}
		else {
			logger.info(toString());
		}
	}
}
