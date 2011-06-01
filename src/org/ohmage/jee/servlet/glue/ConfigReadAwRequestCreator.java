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
package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ConfigReadRequest;


/**
 * Creates a request that represents a config read request.
 * 
 * @author John Jenkins
 */
public class ConfigReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(ConfigReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ConfigReadAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Since there are no parameters, there just return a basic object.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating system information query request.");
		
		return new ConfigReadRequest();
	}

}
