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
package org.ohmage.service;

import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.UserInfo;
import org.ohmage.request.AwRequest;
import org.ohmage.request.UserReadRequest;


/**
 * Gets the information for all the users in all the campaigns and classes in
 * their respective lists and creates a JSONObject that is returned in the
 * request to be returned to the user.
 * 
 * @author John Jenkins
 */
public class UserReadService extends AbstractDaoService {
	public static Logger _logger = Logger.getLogger(UserReadService.class);
	
	/**
	 * Creates this service.
	 * 
	 * @param dao DAO to run to get the list of user information.
	 */
	public UserReadService(Dao dao) {
		super(dao);
	}
	
	/**
	 * Runs the DAO to get the list of UserInfo objects, then adds them all to
	 * a JSONObject and places the resulting JSONObject in the request to be
	 * returned to the user.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Getting the information about all the users in the campaigns and/or classes.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		JSONObject result = new JSONObject();
		ListIterator<?> userInfoListIter = awRequest.getResultList().listIterator();
		while(userInfoListIter.hasNext()) {
			UserInfo userInfo = (UserInfo) userInfoListIter.next();
			
			try {
				result.put(userInfo.getUsername(), userInfo.toJsonObject(false));
			}
			catch(JSONException e) {
				_logger.warn("Error while building JSONObject for a user's information.", e);
			}
		}
		
		awRequest.addToReturn(UserReadRequest.RESULT, result, true);
	}

}
