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
package org.ohmage.service;

import java.util.List;

import org.ohmage.cache.PreferenceCache;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.ServerConfig;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.util.StringUtils;

/**
 * This class contains the services that pertain to the server's configuration.
 * 
 * @author John Jenkins
 */
public class ConfigServices {
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private ConfigServices() {}

	/**
	 * Creates a server configuration object to hold the server's 
	 * configuration.
	 * 
	 * @return A ServerConfig object representing the server's configuration.
	 * 
	 * @throws ServiceException Thrown if there is an error reading from the
	 * 							cache or decoding a value from it.
	 */
	public static ServerConfig readServerConfiguration() 
			throws ServiceException {
		
		PreferenceCache instance = PreferenceCache.instance();

		String appName;
		try {
			appName = instance.lookup(PreferenceCache.KEY_APPLICATION_NAME); 
		}
		catch(CacheMissException e) {
			throw new ServiceException("The application name was unknown.", e);
		}
		
		String appVersion;
		try {
			appVersion = instance.lookup(PreferenceCache.KEY_APPLICATION_VERSION); 
		}
		catch(CacheMissException e) {
			throw new ServiceException("The application version was unknown.", e);
		}
		
		String appBuild;
		try {
			appBuild = instance.lookup(PreferenceCache.KEY_APPLICATION_BUILD); 
		}
		catch(CacheMissException e) {
			throw new ServiceException("The application build was unknown.", e);
		}
		
		SurveyResponse.PrivacyState defaultSurveyResponsePrivacyState;
		try {
			defaultSurveyResponsePrivacyState = 
				SurveyResponse.PrivacyState.getValue(
						instance.lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE)
					); 
		}
		catch(CacheMissException e) {
			throw new ServiceException("The application name was unknown.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ServiceException("The survey response privacy state is not a valid survey response privacy state.");
		}
		
		List<SurveyResponse.PrivacyState> surveyResponsePrivacyStates =
				SurveyResponseServices.instance().getSurveyResponsePrivacyStates();
		
		boolean defaultCampaignCreationPrivilege;
		try {
			defaultCampaignCreationPrivilege = 
				Boolean.valueOf(
						instance.lookup(PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE)
					);
		}
		catch(CacheMissException e) {
			throw new ServiceException("The default campaign creation privilege was unknown.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ServiceException("The default campaign creation privilege was not a valid boolean.", e);
		}
		
		boolean mobilityEnabled;
		try {
			mobilityEnabled = 
					StringUtils.decodeBoolean(
							PreferenceCache.instance().lookup(
									PreferenceCache.KEY_MOBILITY_ENABLED));
		}
		catch(CacheMissException e) {
			throw new ServiceException("Whether or not Mobility is enabled is missing from the database.", e);
		}
		
		String recaptchaPublicKey;
		try {
			recaptchaPublicKey =
					PreferenceCache.instance().lookup(
							PreferenceCache.KEY_RECAPTACH_KEY_PUBLIC);
		}
		catch(CacheMissException e) {
			throw new ServiceException("The ReCaptcha public key is missing from the database.", e);
		}
		
		boolean selfRegistrationAllowed;
		try {
			selfRegistrationAllowed =
					StringUtils.decodeBoolean(
							PreferenceCache.instance().lookup(
									PreferenceCache.KEY_ALLOW_SELF_REGISTRATOIN));
		}
		catch(CacheMissException e) {
			throw new ServiceException("Whether or not self registration is allowed is missing from the database.", e);
		}
		
		try {
			return new ServerConfig(appName, appVersion, appBuild,
					defaultSurveyResponsePrivacyState, surveyResponsePrivacyStates,
					defaultCampaignCreationPrivilege, mobilityEnabled,
					UserBin.LIFETIME, 1024*1024*5*5, 1024*1024*5, 
					recaptchaPublicKey, selfRegistrationAllowed
				);
		} 
		catch(DomainException e) {
			throw new ServiceException(e);
		}
	}
}
