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
package org.ohmage.jee.servlet.validator;

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

	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException;
	
}
