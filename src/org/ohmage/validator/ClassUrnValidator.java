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
 * Validates a class URN.
 * 
 * @author John Jenkins
 * 
 * @deprecated The generic URN validator should be used instead.
 */
public class ClassUrnValidator extends AbstractAnnotatingValidator {
	private static final Logger _logger = Logger.getLogger(ClassUrnValidator.class);
	
	/**
	 * Sets up the annotator to reply to the user with if the URN is invalid.
	 * 
	 * @param annotator The information that will be returned to the user if
	 * 					the class URN is invalid.
	 */
	public ClassUrnValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * Checks that the URN begins with "urn:".
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating a class URN.");
		
		String classUrn = (String) awRequest.getToValidate().get(InputKeys.CLASS_URN);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(classUrn)) {
			_logger.error("Missing the class URN. This should have been caught previously.");
			throw new ValidatorException("Missing class URN.");
		}
		
		if(! classUrn.startsWith("urn:")) {
			getAnnotator().annotate(awRequest, "The class URN must begin with 'urn:'.");
			awRequest.setFailedRequest(true);
			return false;
		}

		awRequest.addToProcess(InputKeys.CLASS_URN, classUrn, true);
		return true;
	}

}
