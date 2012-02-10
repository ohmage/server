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
package org.ohmage.query;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.impl.AuthenticationQuery.UserInformation;
import org.ohmage.request.UserRequest;

/**
 * 
 * @author joshua
 *
 */
public interface IAuthenticationQuery {

	/**
	 * Gathers the information about the user that is attempting to be 
	 * authenticated.
	 * 
	 * @param userRequest The request that contains the specific information
	 * 					  about the user.
	 * 
	 * @return A UserSummary object that gives specific login information
	 * 		   about the user, or null if the user isn't found or the password
	 * 		   isn't correct.
	 */
	UserInformation execute(UserRequest userRequest) throws DataAccessException;

}
