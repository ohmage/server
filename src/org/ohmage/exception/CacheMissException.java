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
 * Checked cache exception. Thrown when a cache miss occurs.
 * 
 * @author John Jenkins
 */
public class CacheMissException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sets the message of the exception.
	 * 
	 * @param message The message being conveyed when this exception occurs.
	 */
	public CacheMissException(String message) {
		super(message);
	}
	
	/**
	 * Sets the message of the exception and the Throwable that caused this
	 * exception to be reached.
	 * 
	 * @param message The message being conveyed when this exception occurs.
	 * 
	 * @param cause The Throwable that caused this exception to be reached.
	 */
	public CacheMissException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Sets the Throwable that caused this exception to be reached.
	 * 
	 * @param cause The Throwable that caused this exception to be reached.
	 */
	public CacheMissException(Throwable cause) {
		super(cause);
	}
}
