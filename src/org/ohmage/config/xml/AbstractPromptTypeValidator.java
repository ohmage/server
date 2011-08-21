package org.ohmage.config.xml;

import nu.xom.Node;

/**
 * Abstract utility to check for type-agnostic prompt response values.
 *  
 * @author selsky
 */
public abstract class AbstractPromptTypeValidator implements PromptTypeValidator {
	private boolean skippable;
	
	/**
	 * Utility to check for constant values such as SKIPPED, which are not dependent on any specific prompt type.
	 */
	public boolean isSkipped(String value) {
		if(! "SKIPPED".equals(value)) {
			return false;
		}
		
		if(! skippable) { // error
			throw new IllegalStateException("SKIPPED not allowed for prompt type in Condition"); 
		}
		
		return true;
	}
	
	public void setSkippable(Node promptNode) {
		skippable = Boolean.valueOf(promptNode.query("skippable").get(0).getValue().trim());
	}
}
