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
package org.ohmage.test.config;

import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;

/**
 * This class is responsible for testing the configuration API.
 * 
 * @author John Jenkins
 */
public class ConfigApiTest {
	private final OhmageApi api;
	
	/**
	 * Creates a tester for the configuration API.
	 * 
	 * @param api The ohmage API object that should already be setup with a
	 * 			  connection to a server.
	 */
	public ConfigApiTest(final OhmageApi api) {
		this.api = api;
	}
	
	/**
	 * Test the server configuration call.
	 * 
	 * @throws ApiException Thrown if there is a bug in the library.
	 * 
	 * @throws IllegalStateException Thrown if the test fails.
	 */
	public void test() throws ApiException {
		// If a call can be made and an 
		try {
			api.getServerConfiguration();
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Valid request.", e);
		}
	}
}
