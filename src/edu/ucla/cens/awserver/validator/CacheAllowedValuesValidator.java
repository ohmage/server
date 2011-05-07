package edu.ucla.cens.awserver.validator;

import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CampaignPrivacyStateCache;
import edu.ucla.cens.awserver.cache.CampaignRoleCache;
import edu.ucla.cens.awserver.cache.CampaignRunningStateCache;
import edu.ucla.cens.awserver.cache.ClassRoleCache;
import edu.ucla.cens.awserver.cache.MobilityPrivacyStateCache;
import edu.ucla.cens.awserver.cache.SurveyResponsePrivacyStateCache;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Checks that the value in the request with a specified key exists according
 * to the specified cache.
 * 
 * @author John Jenkins
 */
public class CacheAllowedValuesValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CacheAllowedValuesValidator.class);
	
	private String _cacheKey;
	private String _requestKey;
	
	private boolean _required;
	
	/**
	 * Sets up the validator with an annotator to use if the validation ever
	 * fails and the cacheKey to use to get the cache and the plausible
	 * keysets for that cache.
	 * 
	 * @param annotator The annotator to use if validation ever fails.
	 * 
	 * @param cacheKey The key to use to lookup which cache to get the keyset
	 * 				   from.
	 * 
	 * @param requestKey The key used to lookup the value that will be
	 * 					 validated from the request.
	 * 
	 * @param required Whether or not this validation is required.
	 * 
	 * @throws IllegalArgumentException Thrown if either of the keys are null.
	 * 
	 * @throws IllegalStateException Thrown if the cache key is unknown to
	 * 								 this object.
	 */
	public CacheAllowedValuesValidator(AwRequestAnnotator annotator, String cacheKey, String requestKey, boolean required) {
		super(annotator);
		
		if(cacheKeyValid(cacheKey)) {
			throw new IllegalArgumentException("Unknown or null cacheKey.");
		}
		else if(requestKey == null) {
			throw new IllegalArgumentException("The requestKey cannot be null.");
		}
		
		_cacheKey = cacheKey;
		_requestKey = requestKey;
		
		_required = required;
	}

	/**
	 * Retrieves the current list of known values from the cache this
	 * validator was configured with, retrieves the value stored in the
	 * request under the 'requestKey', and checks that the stored value is in
	 * the set of known cache values.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the value of " + _requestKey + " against the cache " + _cacheKey + "'s known values.");
		
		// Attempt to get the value if it's required.
		String value;
		try {
			value = (String) awRequest.getToProcessValue(_requestKey);
		}
		catch(IllegalArgumentException e) {
			// It wasn't in the toProcess map, so check the toValidate map.
			value = (String) awRequest.getToValidate().get(_requestKey);
			
			// If it wasn't there either,
			if(value == null) {
				// If it's required, then this should have been caught long
				// before and is a logical error in the system.
				if(_required) {
					throw new ValidatorException("The required parameter " + _requestKey + " wasn't found in either the toProcess or toValidate maps.");
				}
				// If it's not required, then mention that and pass
				// validation.
				else {
					_logger.info("No " + _requestKey + " value found, and it is not required.");
					return true;
				}
			}
		}

		// Get the set of known values.
		Set<String> knownValues = getCacheKeyset();
		
		// If it exists in the set of known values, then it is acceptable.
		if(knownValues.contains(value)) {
			awRequest.addToProcess(_requestKey, value, true);
			return true;
		}
		else {
			getAnnotator().annotate(awRequest, "Unknown value " + value + " for cache " + _cacheKey + ".");
			awRequest.setFailedRequest(true);
			return false;
		}
	}

	/**
	 * Checks which cache this validator was configured with and retrieves the
	 * current list of known states or roles from that cache.
	 * 
	 * @return The current list of known states or roles based on which cache
	 * 		   this validator was configured with.
	 * 
	 * @throws IllegalStateException Thrown if the local '_cacheKey' doesn't
	 * 								 match any known cache's key.
	 */
	private Set<String> getCacheKeyset() {
		if(CampaignPrivacyStateCache.CACHE_KEY.equals(_cacheKey)) {
			return CampaignPrivacyStateCache.getStates();
		}
		else if(CampaignRoleCache.CACHE_KEY.equals(_cacheKey)) {
			return CampaignRoleCache.getRoles();
		}
		else if(CampaignRunningStateCache.CACHE_KEY.equals(_cacheKey)) {
			return CampaignRunningStateCache.getStates();
		}
		else if(ClassRoleCache.CACHE_KEY.equals(_cacheKey)) {
			return ClassRoleCache.getRoles();
		}
		else if(MobilityPrivacyStateCache.CACHE_KEY.equals(_cacheKey)) {
			return MobilityPrivacyStateCache.getStates();
		}
		else if(SurveyResponsePrivacyStateCache.CACHE_KEY.equals(_cacheKey)) {
			return SurveyResponsePrivacyStateCache.getStates();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Checks that the key is one we know about. This must be updated with the
	 * getCacheKeyset() function in order to provide consistency which is :(.
	 * There needs to be a better way to do this, but simply calling 
	 * getCacheKeyset() will cause an exception in the cache if the key is
	 * valid because there is no guarantee that the DataSource is setup yet.
	 * This will cause the server to fail to startup with a giant message, 
	 * that should be thrown at runtime if the error occurs but should be 
	 * caught at build-time. The issue is, I don't want to catch
	 * Spring-specific messages in a file like this as they don't belong here.
	 * 
	 * @param key The key to check to ensure that it is not null and that it
	 * 			  is one of the known cache keys.
	 * 
	 * @return Returns true if the key is not null an known; false, otherwise.
	 */
	private boolean cacheKeyValid(String key) {
		if(key == null) {
			return false;
		}
		else if(CampaignPrivacyStateCache.CACHE_KEY.equals(_cacheKey)) {
			return true;
		}
		else if(CampaignRoleCache.CACHE_KEY.equals(_cacheKey)) {
			return true;
		}
		else if(CampaignRunningStateCache.CACHE_KEY.equals(_cacheKey)) {
			return true;
		}
		else if(ClassRoleCache.CACHE_KEY.equals(_cacheKey)) {
			return true;
		}
		else if(MobilityPrivacyStateCache.CACHE_KEY.equals(_cacheKey)) {
			return true;
		}
		else if(SurveyResponsePrivacyStateCache.CACHE_KEY.equals(_cacheKey)) {
			return true;
		}
		else {
			return false;
		}
	}
}