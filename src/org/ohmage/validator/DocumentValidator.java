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


/**
 * Validates the document.
 * 
 * Currently, there are no requirements on the document, so this just migrates
 * the value across maps.
 * 
 * @author John Jenkins
 */
public class DocumentValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DocumentValidator.class);
	
	private boolean _required;
	
	/**
	 * Sets up this validator with an annotator to use if validation fails and
	 * a flag as to whether the value is required.
	 * 
	 * @param annotator The annotator to respond with if the validation fails.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public DocumentValidator(AwRequestAnnotator annotator, boolean required) {
		super(annotator);
		
		_required = required;
	}

	/**
	 * There is no validation for the document, so we just migrate it to the
	 * toProcess map in case it wasn't already there.
	 * 
	 * @throws ValidatorException Thrown if the document is missing and is
	 * 							  required.
	 */
	@Override
	public boolean validate(AwRequest awRequest) throws ValidatorException {
		byte[] document;
		try {
			document = (byte[]) awRequest.getToProcessValue(InputKeys.DOCUMENT);
		}
		catch(IllegalArgumentException outerException) {
			try {
				document = (byte[]) awRequest.getToValidateValue(InputKeys.DOCUMENT);
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("Missing required value with key '" + InputKeys.DOCUMENT + "'.");
				}
				else {
					return true;
				}
			}
		}
		_logger.info("Validating the document.");
		
		// There is no validation to be done, so we just migrate it to the
		// toProcess map in case it wasn't already there.
		
		awRequest.addToProcess(InputKeys.DOCUMENT, document, true);
		return true;
	}

}
