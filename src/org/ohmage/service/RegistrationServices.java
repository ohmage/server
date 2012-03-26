package org.ohmage.service;

import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.RegistrationConfig;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;

/**
 * This class is responsible for servicing common functionality for 
 * registration requests.
 *
 * @author John Jenkins
 */
public class RegistrationServices {
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private RegistrationServices() {}
	
	/**
	 * Generates and returns the registration information.
	 * 
	 * @return The registration configuration.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public static RegistrationConfig getRegistrationConfig() 
			throws ServiceException {
		
		String recaptchaPublicKey;
		try {
			recaptchaPublicKey =
					PreferenceCache.instance().lookup(
							PreferenceCache.KEY_RECAPTACH_KEY_PUBLIC);
		}
		catch(CacheMissException e) {
			throw new ServiceException("The ReCaptcha public key is missing from the database.", e);
		}
		
		String termsOfService;
		try {
			termsOfService =
					PreferenceCache.instance().lookup(
							PreferenceCache.KEY_TERMS_OF_SERVICE);
		}
		catch(CacheMissException e) {
			throw new ServiceException("The terms of service is missing from the database.", e);
		}
		
		try {
			return new RegistrationConfig(recaptchaPublicKey, termsOfService);
		}
		catch(DomainException e) {
			throw new ServiceException(e);
		}
	}
}
