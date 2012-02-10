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
package org.ohmage.domain;

import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

/**
 * An internal representation of a user. The username and password must be set,
 * but the map of campaigns to which the user belongs and their roles in those 
 * campaigns and the list of maps to which the user belong and their roles in 
 * those classes may not be set. Those must be explicitly built in the 
 * workflow.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class User {
	private final String username;
	private String password;
	private boolean hashPassword;
	
	private String token;
	
	private boolean loggedIn;
	
	/**
	 * Creates a new User object.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param password The password, plaintext or hashed, of the user.
	 * 
	 * @param hashPassword Whether or not the password should be hashed before
	 * 					   being used.
	 * 
	 * @throws DomainException Thrown if the username or password are null or 
	 * 						   whitespace only.
	 */
	public User(
			final String username, 
			final String password, 
			final boolean hashPassword) 
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new DomainException(
					"The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			throw new DomainException(
					"The password cannot be null or whitespace only.");
		}
		
		this.username = username;
		this.password = password;
		this.hashPassword = hashPassword;
		
		token = null;
		
		loggedIn = false;
	}
	
	/**
	 * Copy constructor. Performs a deep copy in the sense that Strings are
	 * immutable, so, while this new object will point to the same String  
	 * objects as the old User object did, they cannot change.
	 * 
	 * @throws DomainException The user is null.
	 */
	public User(final User user) throws DomainException{
		if(null == user) {
			throw new DomainException("Cannot copy a null object.");
		}
		
		username = user.username;
		password = user.password;
		hashPassword = user.hashPassword;
		
		token = user.token;
		
		loggedIn = user.loggedIn;
	}
	
	/**
	 * Returns the token which is null if it hasn't yet been set.
	 * 
	 * @return The token which is null if it hasn't yet been set.
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * Sets the token.
	 * 
	 * @param token The token for this user.
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * Returns the username of this user.
	 * 
	 * @return The username of this user.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Returns the password of this user.
	 * 
	 * @return The password of this user.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Updates the user's password with a hashed version of the password.
	 * 
	 * @param hashedPassword The hashed version of the user's password.
	 */
	public void setHashedPassword(String hashedPassword) {
		hashPassword = false;
		password = hashedPassword;
	}
	
	/**
	 * Returns whether or not to hash this password.
	 * 
	 * @return Whether or not to hash this password.
	 */
	public boolean hashPassword() {
		return hashPassword;
	}
	
	/**
	 * Returns whether or not this user is logged in.
	 * 
	 * @return Whether or not this user is logged in.
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	/**
	 * Sets whether or not this user is logged in.
	 * 
	 * @param loggedIn Whether or not this user is logged in.
	 */
	public void isLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	/**
	 * Returns a String dump of this user.
	 */
	@Override
	public String toString() {
		return "User [username=" + username + ", password=omitted"
				+ ", loggedIn=" + loggedIn + "]";
	}

	/**
	 * Generates a hash code of this instance of this class.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		final int booleanTruePrime = 1231;
		final int booleanFalsePrime = 1237;
		int result = 1;
		result = prime * result + (loggedIn ? booleanTruePrime : booleanFalsePrime);
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * Compares this object against another Object to determine if their 
	 * contents are identical.
	 */
	@Override
	public boolean equals(Object object) {
		// If they point to the same object, they are the same.
		if (this == object) {
			return true;
		}
		
		// If the other object is null, they can't be the same.
		if (object == null) {
			return false;
		}
		
		// If the other object isn't a User, they can't be the same.
		if (getClass() != object.getClass()) {
			return false;
		}
		
		User other = (User) object;
		
		// Ensure that the logged in statuses are equal.
		if (loggedIn != other.loggedIn) {
			return false;
		}
		
		// Ensure that the passwords are equal.
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		
		// Ensure that the usernames are equal.
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		
		return true;
	}
}
