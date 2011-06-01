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
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;


/**
 * For now, does nothing but migrate the value across maps.
 * 
 * TODO: Have this validator ensure that the name doesn't contain any
 * inappropriate words and, potentially, have it reject certain file
 * extensions.
 * 
 * @author John Jenkins
 */
public class GenericNameValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(GenericNameValidator.class);
	
	private String _key;
	private boolean _required;
	
	/**
	 * Builds the validator to reply on error with 'annotator', to use 'key'
	 * to retrieve the name value from the maps, and with a required flag,
	 * 'required'.
	 * 
	 * @param annotator The annotator to reply with should the validation
	 * 					fail.
	 * 
	 * @param key The key to use to get the name value from the maps.
	 * 
	 * @param required Whether or not the name is required.
	 * 
	 * @throws IllegalArgumentException Thrown if the key is null or an empty
	 * 									string.
	 */
	public GenericNameValidator(AwRequestAnnotator annotator, String key, boolean required) throws IllegalArgumentException {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key must be a non-null, non-empy string.");
		}
		
		_key = key;
		_required = required;
	}
	
	/**
	 * Migrates the '_key's name value to the toProcess map.
	 * 
	 * @throws ValidatorException Thrown if the value for '_key' is missing in
	 * 							  both maps.
	 */
	@Override
	public boolean validate(AwRequest awRequest) throws ValidatorException {
		String name;
		try {
			name = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				name = (String) awRequest.getToValidateValue(_key);
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("Missing required key '" + _key + "'.");
				}
				else {
					return true;
				}
			}
		}
		_logger.info("Validating name with key '" + _key + "'.");
		
		// TODO: There really aren't any existing constraints on what a name
		// can be, but maybe there should be. Don't allow inappropriate
		// language or certain file extensions; although, the file extension
		// filter would be superficial.
		
		awRequest.addToProcess(_key, name, true);
		return true;
	}

}
