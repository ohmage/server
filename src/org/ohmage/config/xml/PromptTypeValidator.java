package org.ohmage.config.xml;

import nu.xom.Node;

import org.ohmage.config.grammar.custom.ConditionValuePair;

/**
 * Prompt types have collections of properties that (1) have unique requirements and (2) place bounds on values that are allowed in
 * conditions. 
 * 
 * @author selsky
 */
public interface PromptTypeValidator {
	
	/**
	 * Validates the properties bundle for a specific instance of a type. Also sets the properties so they can be used for 
	 * validation of values of the type.
	 */
	void validateAndSetConfiguration(Node promptNode);
	
	/**
	 * Determines if the provided value and its condition are valid for the type instance.
	 */
	void validateConditionValuePair(ConditionValuePair pair);
	
	/**
	 * Determines if the provided default value is valid for the type instance.
	 */
	void checkDefaultValue(String value);
	
}
