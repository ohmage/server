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
package org.ohmage.controller;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.service.Service;
import org.ohmage.service.ServiceException;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.Validator;
import org.ohmage.validator.ValidatorException;
import org.ohmage.validator.json.FailedJsonRequestAnnotator;


/**
 * The default Controller implementation.
 * 
 * @author selsky
 */
public class ControllerImpl implements Controller {
	private static Logger _logger = Logger.getLogger(ControllerImpl.class);
	
	private Validator[] _validators;
	private Service[]   _services;
	private Service _postProcessingService;
	private String _featureName; // used for logging messages
	private FailedJsonRequestAnnotator _failedRequestAnnotator; // annotate the request in the case of Validator or Service exceptions
	
	/**
	 * Instantiates this class with the passed-in Validator and Service arrays. An array of Services is required for this 
	 * class to function properly.
	 * 
	 * @throws IllegalArgumentException if an empty or null array of Services is passed in.  
	 */
	public ControllerImpl(Service[] services, FailedJsonRequestAnnotator failedRequestAnnotator) {
		if(null == services || services.length == 0) {
			throw new IllegalArgumentException("a null or zero-length array of Services is not allowed");
		}
		if(null == failedRequestAnnotator) {
			throw new IllegalArgumentException("an errorResponse is required");
		}
		_services = services;
		_failedRequestAnnotator = failedRequestAnnotator;
	}
	
	/**
	 * Executes main logic flow for various features. Validates incoming data (if the local instance array of Validators exists) 
	 * and if the validation is successful, dispatches to the local instance array of Services. Runs a post-processing Service 
	 * if the local instance exists. 
	 * 
	 * @throws ControllerException if any ValidationException or ServiceException occurs, generally in the case of unrecoverable
	 * logic or system errors in the respective application layers.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("Handling request for feature: " + _featureName);
		
		try {
			boolean continueProcessing = true;
			
			if(null != _validators) { // for some requests input validation is optional
				
				_logger.info("Validating request for feature: " + _featureName);
				
				for(Validator validator : _validators) {
				
					if(! validator.validate(awRequest)) {
						
						_logger.info("Aborting request processing because top-level validation found an invalid request");
						awRequest.setFailedRequest(true);
						continueProcessing = false;
						break;
					}
				}	
			}
			
			if(continueProcessing) {
				
				_logger.info("Servicing request for feature: " + _featureName);
				
				for(Service service : _services) {
					
					service.execute(awRequest);
					
				    if(awRequest.isFailedRequest()) { // bail out because the request could not be completed successfully
						
				    	_logger.info("Aborting request because service-level validation found an invalid request");
						break;
					}
				}
			}
		}
		
		catch(ValidatorException ve) { // an unrecoverable logical error has occurred
			awRequest.setFailedRequest(true);
			_failedRequestAnnotator.annotate(awRequest, ve.getMessage());
			throw new ControllerException(ve);	
			
		}
		
		catch (ServiceException se) { // an unrecoverable logical or system-level error has occurred
			awRequest.setFailedRequest(true);
			_failedRequestAnnotator.annotate(awRequest, se.getMessage());
			throw new ControllerException(se);	
		}
		
		finally {
			// Because post-processing Services can run in exceptional conditions, they
			// must be coded very defensively. 
			
			if(_postProcessingService != null) {
				
				_logger.info("Post-processing request for feature: " + _featureName);
				
				_postProcessingService.execute(awRequest);

			}
		}
	}

	public void setValidators(Validator[] validators) {
		if(validators == null || validators.length == 0) {
			throw new IllegalArgumentException("setValidators invoked with a null or zero-length array");
		}
		
		_validators = validators;
	}

	public void setPostProcessingService(Service postProcessingService) {
		if(null == postProcessingService) {
			throw new IllegalStateException("setPostProcessingService invoked with a null Service");
		}
		_postProcessingService = postProcessingService;
	}
	
	public void setFeatureName(String featureName) {
		if(StringUtils.isEmptyOrWhitespaceOnly(featureName)) {
			throw new IllegalArgumentException("feature name cannot be null or empty");
		}
		_featureName = featureName;
	}	
}
