/*******************************************************************************
 * Copyright 2016 The Regents of the University of California
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

import org.ohmage.exception.ServiceException;
import org.ohmage.exception.DomainException;
import org.ohmage.service.UserServices;
import org.ohmage.domain.KeycloakUser;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.cache.KeycloakCache;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.apache.log4j.Logger;
import org.jose4j.jwt.MalformedClaimException;


/**
 * This class contains the services pertaining to management
 * of users coming from a keycloak idm server.
 * 
 * @author Steve Nolen
 */
public final class KeycloakServices {
	private static final Logger LOGGER = Logger.getLogger(KeycloakServices.class);

	/*
	 * Key used for username when unpacking valid JWT
	 */
	private static final String KEY_CLAIM_USERNAME = "preferred_username";

	/*
	 * Allowable clock skew for JWT expiration validation.
	 */
	private static final Integer JWT_ALLOW_CLOCK_SKEW_SECONDS = 10;
  
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private KeycloakServices() {}
	
	/*
	 * Inspects a bearer token for username and returns a new KeycloakUser object
	 * for user.
	 */
	public static KeycloakUser getUser(
			final String bearerToken) 
					throws ServiceException {

		JwtConsumer consumer = new JwtConsumerBuilder()
				.setRequireExpirationTime()
				.setSkipDefaultAudienceValidation()
				.setAllowedClockSkewInSeconds(JWT_ALLOW_CLOCK_SKEW_SECONDS)
				.setVerificationKey(KeycloakCache.getPublicKey())
				.build(); // create the JwtConsumer instance

		try {
			JwtContext jwtContext = consumer.process(bearerToken);
			try {
				String username = jwtContext.getJwtClaims().getClaimValue(KEY_CLAIM_USERNAME, String.class);
				return new KeycloakUser(username, jwtContext);
			}
			catch(MalformedClaimException e){
				throw new ServiceException("Unabled to handle keycloak user request. "
						+ "Bearer token has no claim for " 
						+ KEY_CLAIM_USERNAME,
						e);
			}
			catch(DomainException e) {
				throw new ServiceException("Unable to handle keycloak user request", e);
			}
		}
		catch (InvalidJwtException e) {
			throw new ServiceException("Bearer token is invalid or expired.", e);
		}
	}

	public static void createUser(
			final KeycloakUser user)
					throws ServiceException{
		try {
			UserServices.instance().createUser(
					user.getUsername(), 
					user.getPassword(),
					user.getEmail(), 
					false, // admin
					true, // enabled
					false, // newAccount
					null, //campaignCreation, pass null to use default state.
					false, //storeInitial
					true, //externalAccount
					user.getPersonalInfo());
		} catch (ServiceException e) {
			throw new ServiceException("Unable to create keycloak user", e);
		}
	}
	
	/**
	 * Updates a keycloak user's info.
	 * 
	 * @param user A keycloak user object to test for changes.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void updateUser(
			final KeycloakUser user)
					throws ServiceException{
		try {
			Boolean updateEmail = false;
			Boolean updatePersonalInfo = false;
			Boolean deletePersonalInfo = false;
			String tEmail = null;
			String tFirstName = null;
			String tLastName = null;
			String tOrganization = null;
			String tPersonalId = null;

			// check if email matches, if not, update
			String currentEmail = UserServices.instance().getUserEmail(user.getUsername());
			if (!currentEmail.equals(user.getEmail())) {
				tEmail = user.getEmail();
				updateEmail = true;
			}

			// check if userpersonal matches, if not, update.
			UserPersonal currentPersonalInfo = null;
			if (user.getPersonalInfo() != null) {
				currentPersonalInfo = UserServices.instance().getUserPersonalInformation(user.getUsername());
				if (!user.getPersonalInfo().equals(currentPersonalInfo)) {
					tFirstName = user.getPersonalInfo().getFirstName();
					tLastName = user.getPersonalInfo().getLastName();
					tOrganization = user.getPersonalInfo().getOrganization();
					tPersonalId = user.getPersonalInfo().getPersonalId();
					updatePersonalInfo = true;
				}
			}
			// if current user info is empty, delete any personal info we have.
			else {
				deletePersonalInfo = true;
			}

			// updates only if something is changed. 
			if (updateEmail || updatePersonalInfo || deletePersonalInfo) {
				LOGGER.info("Updating user info for keycloak user: " +
						user.getUsername());
				UserServices.instance().updateUser(
						user.getUsername(), 
						tEmail, 
						null, //admin
						null,  //enabled
						null, //new acct
						null, //campaigncreation 
						null, //classcreation
						null, //usersetup
						tFirstName, //firstname
						tLastName,  //lastname
						tOrganization, //org
						tPersonalId,  //pid
						deletePersonalInfo); //delete personal info				
			}

			// check if userpersonal info
			//   if so, check if matches?
			//    if not matches, update
		}
		catch (ServiceException e) {
			throw new ServiceException("Unable to update keycloak user details", e);
		}
	}
}