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
package org.ohmage.validator.prompt;

import java.util.Map;

/**
 * Stores a cache of PromptValidators for validating prompt responses. The cache maps prompt types to their associated validators.
 * 
 * @author selsky
 */
public class PromptValidatorCache {
	private Map<String, PromptValidator> _validatorMap;
	
	public PromptValidatorCache(Map<String, PromptValidator> map) {
		if(null == map) {
			throw new IllegalArgumentException("a prompt validator map is required");
		}
		_validatorMap = map;
	}
	
	public PromptValidator getValidatorFor(String promptType) {
		PromptValidator pv = _validatorMap.get(promptType);
		if(null == pv) {
			throw new IllegalArgumentException("unknown validator for prompt type " + promptType);
		}
		return pv;
	}
}
