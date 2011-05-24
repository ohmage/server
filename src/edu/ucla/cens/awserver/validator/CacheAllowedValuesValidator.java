package edu.ucla.cens.awserver.validator;

import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.Cache;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Checks that the value in the request with a specified key exists according
 * to the specified cache.
 * 
 * @author John Jenkins
 */
public class CacheAllowedValuesValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CacheAllowedValuesValidator.class);
	
	private Cache _cache;
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
	public CacheAllowedValuesValidator(AwRequestAnnotator annotator, Cache cache, String requestKey, boolean required) {
		super(annotator);
		
		if(cache == null) {
			throw new IllegalArgumentException("Unknown or null cacheKey.");
		}
		else if(requestKey == null) {
			throw new IllegalArgumentException("The requestKey cannot be null.");
		}
		
		_cache = cache;
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
		// Attempt to get the value if it's required.
		String value;
		try {
			value = (String) awRequest.getToProcessValue(_requestKey);
		}
		catch(IllegalArgumentException outerException) {
			value = (String) awRequest.getToValidate().get(_requestKey);
			
			if(value == null) {
				if(_required) {
					throw new ValidatorException("The required parameter " + _requestKey + " wasn't found in either the toProcess or toValidate maps.");
				}
				// If it's not required, then mention that and pass
				// validation.
				else {
					return true;
				}
			}
		}
		_logger.info("Validating the value of '" + _requestKey + "' against the cache's known values.");

		// Get the set of known values.
		Set<String> knownKeys = _cache.getKeys();
		
		// If it exists in the set of known values, then it is acceptable.
		if(knownKeys.contains(value)) {
			awRequest.addToProcess(_requestKey, value, true);
			return true;
		}
		else {
			getAnnotator().annotate(awRequest, "Unknown value '" + value + "' for cache.");
			awRequest.setFailedRequest(true);
			return false;
		}
	}
}