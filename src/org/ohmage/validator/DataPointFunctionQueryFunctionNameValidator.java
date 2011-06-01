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
package org.ohmage.validator;

import java.util.List;

import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointFunctionQueryAwRequest;


/**
 * Validates the functionId from the AwRequest.
 * 
 * @author selsky
 */
public class DataPointFunctionQueryFunctionNameValidator extends AbstractAnnotatingValidator {
	private List<String> _functionIds;
	
	public DataPointFunctionQueryFunctionNameValidator(AwRequestAnnotator awRequestAnnotator, List<String> functionIds) {
		super(awRequestAnnotator);
		
		if(null == functionIds || functionIds.isEmpty()) {
			throw new IllegalArgumentException("the list of function ids cannot be null or empty");
		}
		
		_functionIds = functionIds;
	}
	
	public boolean validate(AwRequest awRequest) {
		DataPointFunctionQueryAwRequest req = (DataPointFunctionQueryAwRequest) awRequest; 
		
		if(null == req.getFunctionName()) { // logical error!
			throw new ValidatorException("functionName missing from AwRequest");
		}
		
		if(! _functionIds.contains(req.getFunctionName())) {
			getAnnotator().annotate(awRequest, "invalid function name found: " + req.getFunctionName());
			return false;
		}
		
		return true;
	}
}
