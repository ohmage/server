package edu.ucla.cens.awserver.controller;

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
	private Validator[] _validators;
	private Service[]   _services;
	
	/**
	 * Instantiates this class with the passed-in Validator and Service arrays. An array of Services is required for this 
	 * class to function properly.
	 * 
	 * @throws IllegalArgumentException if an empty or null array of Services is passed in.  
	 */
	public ControllerImpl(Validator[] validators, Service[] services) {
		if(null == services || services.length == 0) {
			throw new IllegalArgumentException("a null or zero-length array of Services is not allowed");
		}
		
		_validators = validators; // TODO validators should be setter-injected if they are not required
		_services = services;
	}
	
	public void execute(AwRequest awRequest) {
		try {
		
			if(null != _validators) {
				
				for(Validator validator : _validators) {
				
					if(! validator.validate(awRequest)) {
						
						return; // exit due to validation failure 
						
					}
				}	
			}
			
			for(Service service : _services) {
				
				service.execute(awRequest);
			}
			
		}
		
		catch(ValidatorException ve) { // an unrecoverable logical or system-level error has occurred

			throw new ControllerException(ve);	
			
		}
		
		catch (ServiceException se) { // an unrecoverable logical or system-level error has occurred
		
			throw new ControllerException(se);	
		}
	}
}
