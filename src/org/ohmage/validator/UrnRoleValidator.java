/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.cache.Cache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


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
			try {
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
			catch(IllegalArgumentException e) {
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
			String[] urnRole = urnRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
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
