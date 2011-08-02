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

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


/**
 * Validates a comma-delimited list of URNs.
 * 
 * @author selsky
 */
public class UrnListValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(UrnListValidator.class);
	private String _propertyName;
	private boolean _required;
	
	public UrnListValidator(AwRequestAnnotator awRequestAnnotator, String propertyName, boolean required) {
		super(awRequestAnnotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(propertyName)) {
			throw new IllegalArgumentException("propertyName must not be null or empty");
		}
		
		_propertyName = propertyName;
		_required = required;
	}
	
	public boolean validate(AwRequest awRequest) {
		String urnList;
		try {
			urnList = (String) awRequest.getToProcessValue(_propertyName);
		}
		catch(IllegalArgumentException outerException) {
			try {
				urnList = (String) awRequest.getToValidateValue(_propertyName);
				
				if(urnList == null) {
					if(_required) {
						throw new IllegalArgumentException("The required URN list is missing: " + _propertyName);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new IllegalArgumentException("The required URN list is missing: " + _propertyName);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating a URN list using property name " + _propertyName);
		
		String[] urns = urnList.split(InputKeys.LIST_ITEM_SEPARATOR);
		List<String> result = new LinkedList<String>();
		for(String urn : urns) {
			if(! StringUtils.isEmptyOrWhitespaceOnly(urn)) {
				if(! StringUtils.isValidUrn(urn)) {
					getAnnotator().annotate(awRequest, "found invalid urn: " + urn);
					return false;
				}
				else {
					result.add(urn);
				}
			}
		}
		
		awRequest.addToProcess(_propertyName, StringUtils.collectionToDelimitedString(result, InputKeys.LIST_ITEM_SEPARATOR), true);
		return true;
	}	
}
