package org.ohmage.config.xml;

import nu.xom.Node;
import nu.xom.Nodes;

import org.ohmage.config.grammar.custom.ConditionValuePair;

/**
 * Validates the text prompt type.
 * 
 * @author selsky
 */
public class TextMinMaxPromptTypeValidator extends AbstractNumberPromptTypeValidator {

	/**
	 * Only allowed value here is SKIPPED.
	 */
	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			throw new IllegalArgumentException("invalid value for photo prompt type: " + pair.getValue());
		}
	}
	
	/**
	 * Makes sure that max is greater than min and that min and max are both positive integers.
	 */
	protected void performExtendedConfigValidation(Node promptNode, Nodes minLNodes, Nodes maxLNodes) {
		int min = getValidPositiveInteger(minLNodes.get(0).getValue().trim()); 
		int max = getValidPositiveInteger(maxLNodes.get(0).getValue().trim());
		
		if(max < min) {
			throw new IllegalStateException("max cannot be greater than min: " + promptNode.toXML());
		}
	}
	
	/**
	 * @throws IllegalArgumentException because default text values are disallowed.
	 */
	public void checkDefaultValue(String value) {
		throw new IllegalArgumentException("default values for text prompts are disallowed");
	}
}
