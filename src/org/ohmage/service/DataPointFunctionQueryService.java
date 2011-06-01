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
package org.ohmage.service;

import java.util.Map;

import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointFunctionQueryAwRequest;


/**
 * @author selsky
 */
public class DataPointFunctionQueryService implements Service {
	private Map<String, Service> _services;
	
	public DataPointFunctionQueryService(Map<String, Service> services) {
		if(null == services || services.isEmpty()) {
			throw new IllegalArgumentException("the map of services cannot be null or empty");
		}
		_services = services;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		DataPointFunctionQueryAwRequest req	= (DataPointFunctionQueryAwRequest) awRequest;
		String functionName = req.getFunctionName(); 
		Service service = _services.get(functionName);
		if(null == service) { // this is a logical/configuration error
			throw new IllegalStateException("service not found for function " + functionName);
		}
		service.execute(awRequest);
	}
}
