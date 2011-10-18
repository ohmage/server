package org.ohmage.service;

import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.ServerConfig;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

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
	 * @param request The Request using this service.
	 * 
	 * @return A ServerConfig object representing the server's configuration.
	 * 
	 * @throws ServiceException Thrown if there is an error reading from the
	 * 							cache or decoding a value from it.
	 */
	public static ServerConfig readServerConfiguration(final Request request) 
			throws ServiceException {
		
		PreferenceCache instance = PreferenceCache.instance();

		String appName;
		try {
			appName = instance.lookup(PreferenceCache.KEY_APPLICATION_NAME); 
		}
		catch(CacheMissException e) {
			request.setFailed();
			throw new ServiceException("The application name was unknown.", e);
		}
		
		String appVersion;
		try {
			appVersion = instance.lookup(PreferenceCache.KEY_APPLICATION_VERSION); 
		}
		catch(CacheMissException e) {
			request.setFailed();
			throw new ServiceException("The application version was unknown.", e);
		}
		
		String appBuild;
		try {
			appBuild = instance.lookup(PreferenceCache.KEY_APPLICATION_BUILD); 
		}
		catch(CacheMissException e) {
			request.setFailed();
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
			request.setFailed();
			throw new ServiceException("The application name was unknown.", e);
		}
		catch(IllegalArgumentException e) {
			request.setFailed();
			throw new ServiceException("The survey response privacy state is not a valid survey response privacy state.");
		}
		
		SurveyResponse.PrivacyState[] surveyResponsePrivacyStates =
			SurveyResponse.PrivacyState.getPrivacyStates();
		
		return new ServerConfig(appName, appVersion, appBuild,
				defaultSurveyResponsePrivacyState, surveyResponsePrivacyStates
			);
	}
}