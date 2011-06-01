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
package org.ohmage.request;

/**
 * Keys that are put into the toReturn map to be returned to the requester.
 * 
 * @author John Jenkins
 */
public class ReturnKeys {
	/**
	 * Default constructor that is private such that no instance can ever be
	 * made.
	 */
	private ReturnKeys() {
		// Do nothing.
	}
	
	// General response keys.
	public static final String RESULT = "result";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String DATA = "data";
	
	// Document-specific response keys.
	public static final String DOCUMENT_ID = "document_id";
}
