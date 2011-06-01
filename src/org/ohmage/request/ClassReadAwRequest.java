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
 * The internal representation of a class read request.
 * 
 * @author John Jenkins
 */
public class ClassReadAwRequest extends ResultListAwRequest {
	public static final String RETURN = "class_read_request_class_information";
	
	/**
	 * Populates a class read request with the list of classes.
	 * 
	 * @param classList A comma-separated list of class URNs that
	 */
	public ClassReadAwRequest(String classList) {
		if(classList == null) {
			throw new IllegalArgumentException("Cannot have a null class list.");
		}

		addToValidate(InputKeys.CLASS_URN_LIST, classList, true);
	}
}
