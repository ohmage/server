package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

/**
 * Performs pre-validation checks on incoming parameters in the HttpRequest. The idea is to deny requests that are missing 
 * parameters or that have parameter values that are way out of range. The reason to do validation in both the HTTP layer and the 
 * actual application layer (within the Validators that run as part of ControllerImpl) is to follow best security practices: if some
 * entity is doing something malicious or attemting to figure out what parameters a particular URL accepts, a good rule of thumb
 * is to simply reject the request with a generic error instead of giving away information about how the application works.
 * 
 * @author selsky
 */
public interface HttpServletRequestValidator {

	public boolean validate(HttpServletRequest httpRequest);
	
}
