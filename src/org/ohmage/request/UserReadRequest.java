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
 * A request for getting information about a user.
 * 
 * @author John Jenkins
 */
public class UserReadRequest extends ResultListAwRequest {
	public static final String RESULT = "user_read_request_user_information";
	
	/**
	 * Builds a request for reading information about a user.
	 * 
	 * @param campaignIdList A list of campaigns whose users' information is
	 * 						 desired.
	 * 
	 * @param classIdList A list of classes whose users' information is 
	 * 					  desired.
	 */
	public UserReadRequest(String campaignIdList, String classIdList) {
		super();
		
		addToValidate(InputKeys.CAMPAIGN_URN_LIST, campaignIdList, true);
		addToValidate(InputKeys.CLASS_URN_LIST, classIdList, true);
	}
}
