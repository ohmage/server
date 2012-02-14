/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ServerConfig;
import org.ohmage.exception.ServiceException;
import org.ohmage.service.ConfigServices;

/**
 * <p>This class is responsible for updating a class.</p>
 * <p>There are no required parameters for this call.</p>
 * 
 * @author John Jenkins
 */
public class ConfigReadRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(ConfigReadRequest.class);
	
	private ServerConfig result;
	
	/**
	 * Default constructor.
	 */
	public ConfigReadRequest() {
		super(null);
		
		result = null;
	}
	
	/**
	 * Gathers the appropriate information and stores the result in the result
	 * object.
	 */
	@Override
	public void service() {
		LOGGER.info("Gathering information about the system.");
		
		try {
			result = ConfigServices.readServerConfiguration();
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}
	
	/**
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		return new HashMap<String, String[]>();
	}
	
	/**
	 * Writes the response to the client.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing configuration read response.");
		
		try {
			JSONObject response = new JSONObject();
			response.put(JSON_KEY_DATA, (result == null) ? null : result.toJson());
			respond(
					httpRequest, 
					httpResponse,
					response);
		}
		catch(JSONException e) {
			LOGGER.error("Error building the JSONObject.", e);
			setFailed();
			respond(httpRequest, httpResponse, null);
		}
	}
}
