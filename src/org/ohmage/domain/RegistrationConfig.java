package org.ohmage.domain;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.exception.DomainException;

/**
 * This class contains the information pertaining to the server's registration
 * configuration. To determine if self-registration is allowed, please see 
 * {@link org.ohmage.request.ConfigReadRequest here}.
 *
 * @author John Jenkins
 * 
 * @see org.ohmage.request.ConfigReadRequest
 */
public class RegistrationConfig {
	/**
	 * The public key for ReCaptcha.
	 */
	public static final String JSON_KEY_RECAPTCHA_KEY_PUBLIC = 
			"recaptcha_public_key";
	/**
	 * The server's terms of service.
	 */
	public static final String JSON_KEY_TERMS_OF_SERVICE = "terms_of_service";
	
	private final String recaptchaPublicKey;
	private final String termsOfService;
	
	/**
	 * Creates a new registration configuration.
	 * 
	 * @param recaptchaPublicKey The public ReCaptcha key.
	 * 
	 * @param termsOfService The system's terms of service.
	 * 
	 * @throws DomainException The key and/or terms were null.
	 */
	public RegistrationConfig(
			final String recaptchaPublicKey, 
			final String termsOfService) 
			throws DomainException {
		
		if(recaptchaPublicKey == null) {
			throw new DomainException("The ReCaptcha public key is null.");
		}
		else if(termsOfService == null) {
			throw new DomainException("The terms of service is null.");
		}
		
		this.recaptchaPublicKey = recaptchaPublicKey;
		this.termsOfService = termsOfService;
	}
	
	/**
	 * Returns the public key for ReCaptcha.
	 * 
	 * @return The public key for ReCaptcha.
	 */
	public final String getRecaptchaPublicKey() {
		return recaptchaPublicKey;
	}
	
	/**
	 * Returns this object as a JSON object for serialization.
	 * 
	 * @return A JSON object.
	 * 
	 * @throws JSONException There was an error creating the JSON.
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_RECAPTCHA_KEY_PUBLIC, recaptchaPublicKey);
		result.put(JSON_KEY_TERMS_OF_SERVICE, termsOfService);
		
		return result;
	}
}