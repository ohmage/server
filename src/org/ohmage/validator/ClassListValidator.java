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
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


/**
 * Validates that the list of classes exist and is not empty, but it doesn't
 * do any actual validation as to if the classes are legitimate or not.
 * 
 * @author John Jenkins
 */
public class ClassListValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(ClassListValidator.class);
	
	private boolean _required;
	
	/**
	 * Creates a new validator for the list of classes.
	 * 
	 * @param annotator The annotator should something fail.
	 */
	public ClassListValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}
	
	/**
	 * Validates the list of classes. At this point, it simply validates that
	 * the list is not empty, and that each item in the list is a valid URN.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating a list of classes.");
		
		String classes = (String) awRequest.getToValidate().get(InputKeys.CLASS_URN_LIST);
		if(classes == null) {
			if(_required) {
				_logger.error("Missing required class list parameter. This should have been caught before this.");
				throw new ValidatorException("Missing required class list.");
			}
			else {
				return true;
			}
		}
		
		if("".equals(classes.trim())) {
			return true;
		}
		
		_logger.debug("Class list: " + classes);

		String[] classList = classes.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classList.length; i++) {
			if(! StringUtils.isValidUrn(classList[i])) {
				awRequest.setFailedRequest(true);
				getAnnotator().annotate(awRequest, "Invalid URN in class list: " + classList[i]);
				return false;
			}
		}
		
		awRequest.addToProcess(InputKeys.CLASS_URN_LIST, classes, true);
		return true;
	}

}
