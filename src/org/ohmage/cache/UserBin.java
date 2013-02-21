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
package org.ohmage.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.ohmage.domain.User;
import org.ohmage.exception.DomainException;
import org.springframework.beans.factory.DisposableBean;

/**
 * User storage. User objects are mapped to unique ids. Avoids dependencies on JEE session management. The lifetime param set on 
 * construction controls how long User objects stay active.
 * 
 * @author Joshua Selsky
 */
public final class UserBin extends TimerTask implements DisposableBean {
	private static final Logger LOGGER = Logger.getLogger(UserBin.class);
	
	/**
	 * This is the length of an authentication token.
	 */
	public static final int LIFETIME = 1000 * 60 * 15;
	private static final int EXECUTION_PERIOD = 60000;
	
	/**
	 * A class for associating users to the time their token expires.
	 * 
	 * @author John Jenkins
	 */
	private static final class UserTime {
		private final User user;
		private long time;
		
		/**
		 * Convenience constructor.
		 * 
		 * @param user The user that is being stored in the cache.
		 * 
		 * @param time The last time this user was accessed in the cache.
		 */
		private UserTime(User user, long time) {
			this.user = user;
			this.time = time;
		}
	}
	
	// A map of tokens to USERS and the time that their token expires.
	private static final Map<String, UserTime> USERS = new ConcurrentHashMap<String, UserTime>();
	// An EXECUTIONER thread to purge those whose tokens have expired.
	private static final Timer EXECUTIONER = new Timer("UserBin - User expiration process.", true);
	
	// Whether or not the constructor has run which will bootstrap this 
	// Singleton class.
	private static boolean initialized = false;
		
	/**
	 * @param lifetime controls the number of milliseconds a User object will be permitted to be resident in the bin
	 * @param executionPeriod controls how often the bin is checked for expired USERS
	 * 
	 * TODO Enforce a max lifetime?
	 * TODO Enforce period relative to lifetime?
	 */
	private UserBin() {
		LOGGER.info("Users will live for " + LIFETIME + " milliseconds and the executioner will run every " + EXECUTION_PERIOD 
			+ " milliseconds");

		EXECUTIONER.schedule(this, EXECUTION_PERIOD * 2, EXECUTION_PERIOD);
		
		initialized = true;
	}
	
	@Override
	public void destroy() {
		EXECUTIONER.cancel();
	}
	
	/**
	 * Adds a user to the bin and returns an Id (token) representing that user. If the user is already resident in the bin, their
	 * old token is removed and a new one is generated and returned. 
	 */
	public static synchronized String addUser(User user) 
			throws DomainException {
		
		if(! initialized) {
			new UserBin();
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("adding user to bin");
		}
		
		String uuid = UUID.randomUUID().toString();
		UserTime ut = new UserTime(user, System.currentTimeMillis());
		user.setToken(uuid);
		if(USERS.put(uuid, ut) != null) {
			throw new DomainException("UUID collision: " + uuid);
		}
		
		return uuid;
	}
	
	/**
	 * Removes a user from the user bin.
	 * 
	 * @param authToken The authentication token to remove from the user bin.
	 */
	public static synchronized void expireUser(String authToken) {
		if(! initialized) {
			new UserBin();
		}
		
		if(authToken == null) {
			throw new IllegalArgumentException("The token cannot be null.");
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Removing user from bin.");
		}
		
		USERS.remove(authToken);
	}
	
	/**
	 * Removes all of the tokens for some user.
	 * 
	 * @param username The user's username.
	 */
	public static synchronized void removeUser(String username) {
		if(! initialized) {
			new UserBin();
		}
		
		if(username == null) {
			throw new IllegalArgumentException("The username cannot be null.");
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Removing the user from the bin.");
		}
		
		Set<String> userTokens = new HashSet<String>();
		for(String token : USERS.keySet()) {
			UserTime userTime = USERS.get(token);
			if(userTime.user.getUsername().equals(username)) {
				userTokens.add(token);
			}
		}
		
		for(String token : userTokens) {
			USERS.remove(token);
		}
	}
	
	/**
	 * Returns the User bound to the provided Id or null if Id does not exist in the bin. 
	 */
	public static synchronized User getUser(String id) {
		UserTime ut = USERS.get(id);
		if(null != ut) { 
			User u = ut.user;
			if(null != u) {
				ut.time = System.currentTimeMillis(); // refresh the time 
				try {
					return new User(u);
				} catch (DomainException e) {
					LOGGER.error("Error duplicating the user.", e);
					return null;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets the number of milliseconds until a token expires.
	 * 
	 * @param Id The token.
	 * 
	 * @return The number of milliseconds until 'Id' expires.
	 */
	public static synchronized long getTokenRemainingLifetimeInMillis(String id) {
		UserTime ut = USERS.get(id);
		if(ut == null) {
			return 0;
		}
		else {
			return Math.max((System.currentTimeMillis() + LIFETIME - ut.time), 0);
		}
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
	private static synchronized void expire() {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Beginning user expiration process");
		}
		
		Set<String> keySet = USERS.keySet();
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Number of users before expiration: " + keySet.size());
		}
		
		long currentTime = System.currentTimeMillis();
		
		for(String key : keySet) {
			UserTime ut = USERS.get(key);
			if(currentTime - ut.time > LIFETIME) {
			    	
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Removing user with Id " + key);
				}
				
				USERS.remove(key);
			}
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Number of users after expiration: " + USERS.size());
		}
	}
}
