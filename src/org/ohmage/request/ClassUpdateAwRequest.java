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

import org.ohmage.util.StringUtils;

/**
 * Request for handling the update of a class.
 * 
 * @author John Jenkins
 */
public class ClassUpdateAwRequest extends ResultListAwRequest {
	/**
	 * Populates the toValidate map with the applicable paramters.
	 * 
	 * @param classUrn The class that is being updated. The only parameter
	 * 				   that must be non-null.
	 * 
	 * @param description The new description of the class.
	 *  
	 * @param userListAdd A comma-separated list of users to be added to the
	 * 					  class.
	 * 
	 * @param userListRemove A comma-separated list of users to remove from
	 * 						 the class.
	 */
	public ClassUpdateAwRequest(String classUrn, String name, String description, String userListAdd, String userListRemove, String privilegedUserListAdd) {
		addToValidate(InputKeys.CLASS_URN, classUrn, true);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(name)) {
			addToValidate(InputKeys.CLASS_NAME, name, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(description)) {
			addToValidate(InputKeys.DESCRIPTION, description, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListAdd)) {
			addToValidate(InputKeys.USER_LIST_ADD, userListAdd, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListRemove)) {
			addToValidate(InputKeys.USER_LIST_REMOVE, userListRemove, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(privilegedUserListAdd)) {
			addToValidate(InputKeys.PRIVILEGED_USER_LIST_ADD, privilegedUserListAdd, true);
		}
	}
}
