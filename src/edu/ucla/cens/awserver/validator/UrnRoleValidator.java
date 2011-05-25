package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.Cache;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Takes a list of URNs and roles and ensures that the URNs are valid and that
 * the roles are known.
 * 
 * @author John Jenkins
 */
public class UrnRoleValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(UrnRoleValidator.class);
	
	String _key;
	Cache _roleCache;
	
	boolean _required;
	
	/**
	 * Sets up this validator.
	 * 
	 * @param annotator The annotator to reply with should the validation fail.
	 * 
	 * @param key The key to use to lookup the values in the maps.
	 * 
	 * @param roleCache The Cache that contains the roles.
	 * 
	 * @param pairSeparator The separator for the URN and the role for each
	 * 						pair in the list.
	 * 
	 * @param listSeparator The separator used to each pair in the list.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public UrnRoleValidator(AwRequestAnnotator annotator, String key, Cache roleCache, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The 'key' cannot be null or any empty string.");
		}
		else if(roleCache == null) {
			throw new IllegalArgumentException("The 'roleCache' cannot be null.");
		}
		
		_key = key;
		_roleCache = roleCache;
		
		_required = required;
	}

	/**
	 * Validates that the list exists if required. If it exists, required or
	 * not, it will check that it is a list that can be parsed with the local
	 * list separator String and that each element can be parsed with the local
	 * pair separator String. Furthermore, it will ensure that the first
	 * element in the pair is a valid URN and that the second element in the 
	 * pair is in the cache.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		// Get the list from one of the two maps.
		String urnRoleList;
		try {
			urnRoleList = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			urnRoleList = (String) awRequest.getToValidateValue(_key);
			
			if(urnRoleList == null) {
				if(_required) {
					throw new ValidatorException("Missing required value for key '" + _key + "'. This should have been caught earlier.");
				}
				else {
					// It isn't present nor is it required.
					return true;
				}
			}
		}
		
		_logger.info("Validating the list for key '" + _key + "' against the cache '" + _roleCache.getName() + "'.");
		
		// Split the list to get each of the pairs.
		String[] urnRoleArray = urnRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < urnRoleArray.length; i++) {
			String[] urnRole = urnRoleArray[i].split(InputKeys.URN_ROLE_SEPARATOR);
			
			// Make sure there is exactly two objects in the pair.
			if(urnRole.length != 2) {
				getAnnotator().annotate(awRequest, "Invalid URN-role pair found: " + urnRoleArray[i]);
				awRequest.setFailedRequest(true);
				return false;
			}
			
			// Make sure the first object is a valid URN.
			if(! StringUtils.isValidUrn(urnRole[0])) {
				getAnnotator().annotate(awRequest, "The URN is invalid at index " + i + ": " + urnRole[0]);
				awRequest.setFailedRequest(true);
				return false;
			}
			
			// Make sure the second object is a known role.
			if(! _roleCache.getKeys().contains(urnRole[1])) {
				getAnnotator().annotate(awRequest, "Unknown role for cache '" + _roleCache.getName() + "': " + urnRole[1]);
				awRequest.setFailedRequest(true);
				return false;
			}
		}
		
		awRequest.addToProcess(_key, urnRoleList, true);
		return true;
	}
}