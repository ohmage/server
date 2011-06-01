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
package org.ohmage.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.ohmage.domain.User;
import org.ohmage.domain.UserTime;


/**
 * User storage. User objects are mapped to unique ids. Avoids dependencies on JEE session management. The lifetime param set on 
 * construction controls how long User objects stay active. 
 * 
 * TODO private locking?
 * 
 * @author Joshua Selsky
 */
public class UserBin extends TimerTask {
	private static Logger _logger = Logger.getLogger(UserBin.class);
	private Map<String, UserTime> _users;
	private int _lifetime;
	private Timer _executioner;
		
	/**
	 * @param lifetime controls the number of milliseconds a User object will be permitted to be resident in the bin
	 * @param executionPeriod controls how often the bin is checked for expired users
	 * 
	 * TODO Enforce a max lifetime?
	 * TODO Enforce period relative to lifetime?
	 */
	public UserBin(int lifetime, int executionPeriod) {
		_logger.info("Users will live for " + lifetime + " milliseconds and the executioner will run every " + executionPeriod 
			+ " milliseconds");
		_lifetime = lifetime;
		_users = new ConcurrentHashMap<String, UserTime> ();
		_executioner = new Timer("user bin user expiration process", true);
		_executioner.schedule(this, executionPeriod * 2, executionPeriod);
	}
	
	/**
	 * Adds a user to the bin and returns an id (token) representing that user. If the user is already resident in the bin, their
	 * old token is removed and a new one is generated and returned. 
	 */
	public synchronized String addUser(User user) {
		if(_logger.isDebugEnabled()) {
			_logger.debug("adding user to bin");
		}
		
		String id = findIdForUser(user); 
		if(null != id) { // user already exists in the bin
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("Removing a user that already existed in the bin before adding them in again (login attempt for an "
					+ "already logged in user)");
			}
			
			_users.remove(id);
		}
		
		String uuid = UUID.randomUUID().toString();
		UserTime ut = new UserTime(user, System.currentTimeMillis());
		_users.put(uuid, ut);
		return uuid;
	}
	
	/**
	 * Returns the User bound to the provided id or null if id does not exist in the bin. 
	 */
	public synchronized User getUser(String id) {
		UserTime ut = _users.get(id);
		if(null != ut) { 
			User u = ut.getUser();
			if(null != u) {
				ut.setTime(System.currentTimeMillis()); // refresh the time 
				return new User(u);
			}
		}
		return null;
	}
	
	/**
	 * Background thread for purging expired Users.
	 */
	@Override
	public void run() {
		expire();
	}
	
	/**
	 * Checks every bin location and removes Users whose tokens have expired.
	 */
	private synchronized void expire() {
		if(_logger.isDebugEnabled()) {
			_logger.debug("Beginning user expiration process");
		}
		
		Set<String> keySet = _users.keySet();
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("Number of users before expiration: " + keySet.size());
		}
		
		long currentTime = System.currentTimeMillis();
		
		for(String key : keySet) {
			UserTime ut = _users.get(key);
			if(currentTime - ut.getTime() > _lifetime) {
			    	
				if(_logger.isDebugEnabled()) {
					_logger.debug("Removing user with id " + key);
				}
				
				_users.remove(key);
			}
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("Number of users after expiration: " + _users.size());
		}
	}
	
	/**
	 * Returns the id for the provided user.
	 */
	private synchronized String findIdForUser(User user) {
		Iterator<String> iterator = _users.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			UserTime userTime = _users.get(key);
			if(userTime.getUser().equals(user)) {
				return key;
			}
		}
		return null;
	}
}
