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

import org.ohmage.request.AwRequest;

/**
 * Controllers are the interface shown to the "outside world" (e.g., Servlets) for access to application features. 
 *
 * @author selsky
 */
public interface Controller {
	
	/**
	 * Executes feature-specific logic using the incoming request.
	 * 
	 * @param awRequest - feature parameters and user specific data 
	 */
	public void execute(AwRequest awRequest);
	
}
