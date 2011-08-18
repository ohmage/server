package org.ohmage.config.xml;

import nu.xom.Node;
import nu.xom.Nodes;

/**
 * Validates the prompt types number and hours_before now, which both require a min and max property that must be a valid integer.
 * 
 * @author selsky
 */
public class HoursBeforeNowPromptTypeValidator extends NumberMinMaxPromptTypeValidator {
	
	/**
	 * Makes sure that max is greater than min and that min and max are both non-negative integers.
	 */
	@Override
	protected void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes) {
		setMin(getValidNonNegativeInteger(minVNodes.get(0).getValue().trim())); 
		setMax(getValidNonNegativeInteger(maxVNodes.get(0).getValue().trim()));
		
		if(getMax() < getMin()) {
			throw new IllegalStateException("max cannot be less than min:\n" + promptNode.toXML());
		}
	}
}
