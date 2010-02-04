package edu.ucla.cens.awserver.controller;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.service.Service;
import edu.ucla.cens.awserver.service.ServiceException;
import edu.ucla.cens.awserver.validator.Validator;
import edu.ucla.cens.awserver.validator.ValidatorException;

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
	
	/**
	 * Instantiates this class with the passed-in Validator and Service arrays. An array of Services is required for this 
	 * class to function properly.
	 * 
	 * @throws IllegalArgumentException if an empty or null array of Services is passed in.  
	 */
	public ControllerImpl(Service[] services) {
		if(null == services || services.length == 0) {
			throw new IllegalArgumentException("a null or zero-length array of Services is not allowed");
		}
		_services = services;
	}
	
	/**
	 * 
	 */
	public void execute(AwRequest awRequest) {
		try {
			boolean continueProcessing = true;
			
			if(null != _validators) { // for some requests input validation is optional
				
				_logger.info("validating request");
				
				for(Validator validator : _validators) {
				
					if(! validator.validate(awRequest)) {
						
						continueProcessing = false;
						break;
					}
				}	
			}
			
			if(continueProcessing) {
				
				_logger.info("servicing request");
				
				for(Service service : _services) {
					
					service.execute(awRequest);
					
				    if(awRequest.isFailedRequest()) { // bail out because the request could not be completed successfully
						
						break;
					}
				}
			}
		}
		
		catch(ValidatorException ve) { // an unrecoverable logical error has occurred

			throw new ControllerException(ve);	
			
		}
		
		catch (ServiceException se) { // an unrecoverable logical or system-level error has occurred
		
			throw new ControllerException(se);	
		}
		
		finally {
			// Because post-processing Services can run in exceptional conditions, they
			// must be coded very defensively. 
			
			if(_postProcessingService != null) {
				
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
	
}
