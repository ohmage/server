package org.ohmage.config.validator;

import org.andwellness.config.xml.PromptTypeValidator;

import nu.xom.Node;

/**
 * Abstract utility to check for type-agnostic prompt response values.
 *  
 * @author selsky
 */
public abstract class AbstractPromptTypeValidator implements PromptTypeValidator {
	protected boolean _skippable;
	
	/**
	 * Utility to check for constant values such as SKIPPED, which are not dependent on any specific prompt type.
	 */
	public boolean isSkipped(String value) {
		if(! "SKIPPED".equals(value)) {
			return false;
		}
		
		if(! _skippable) { // error
			throw new IllegalStateException("SKIPPED not allowed for prompt type in condition"); 
		}
		
		return true;
	}
	
	public void setSkippable(Node promptNode) {
		_skippable = Boolean.valueOf(promptNode.query("skippable").get(0).getValue().trim());
	}
}
