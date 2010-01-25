package edu.ucla.cens.awserver.validator.prompt;

import edu.ucla.cens.awserver.domain.PromptType;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Factory for returning PromptResponseValidators based on the prompt type.
 * 
 * @author selsky
 */
public class PromptResponseValidatorFactory {

	private PromptResponseValidatorFactory() { }
	
	/**
	 * @return a PromptResponseValidator for the provided PromptType
	 * @throws IllegalArgumentException if the provided PromptType.type is null, empty, or all whitespace
	 * @throws IllegalArgumentException if the provided PromptType.type is unknown
	 */
	public static PromptResponseValidator make(PromptType type) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(type.getType())) { // if this happens, it means there is an invalid row in the 
			                                                      // prompt_type table
			throw new IllegalArgumentException("missing type in PromptType");
		}
		
		if("time_military".equals(type.getType())) {
			
			return new TimeMilitaryValidator();
			
		} else if ("array_boolean".equals(type.getType())) {
			
			return new ArrayBooleanValidator(type.getRestriction());
			
		} else if ("map".equals(type.getType())) {
			
			return new MapValidator(type.getRestriction());
		
		} 
		
		throw new IllegalArgumentException("unknown PromptResponseValidator type: " + type);
		
	}
}
