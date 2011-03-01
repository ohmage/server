package edu.ucla.cens.awserver.validator.prompt;

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
