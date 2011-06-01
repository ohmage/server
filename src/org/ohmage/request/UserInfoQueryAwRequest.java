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
 * Request class for user info queries.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryAwRequest extends ResultListAwRequest {
	public static final String RESULT = "user_info_query_result";
	
	private String _usernames;
	
	/**
	 * Default constructor.
	 */
	public UserInfoQueryAwRequest() {
		super();
	}
	
	/**
	 * Outputs this object and then calls its super's toString().
	 */
	@Override
	public String toString() {
		return("UserInfoQueryAwRequest [_usernames = " + _usernames +
			   "] super = " + super.toString());
	}
}
