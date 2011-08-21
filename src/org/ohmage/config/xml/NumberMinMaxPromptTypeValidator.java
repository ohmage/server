package org.ohmage.config.xml;

import nu.xom.Node;
import nu.xom.Nodes;

import org.ohmage.config.grammar.custom.ConditionValuePair;

/**
 * Validates the prompt types number and hours_before now, which both require a min and max property that must be a valid integer.
 * 
 * @author selsky
 */
public class NumberMinMaxPromptTypeValidator extends AbstractNumberPromptTypeValidator {
	
	/**
	 * Checks values (from, e.g., conditions) against the min and max defined by this instance. 
	 */
	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			int v = 0;
			try {
				v = Integer.parseInt(pair.getValue());
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("not a number: " + pair.getValue(), nfe); 
			}
			if(v < getMin() || v > getMax()) {
				throw new IllegalArgumentException("number or hours_before_now prompt value of out range. min=" + getMin() + ", max=" +
				    getMax() + ", value=" + pair.getValue());
			}
		}
	}
	
	
	/**
	 * Makes sure max is greater than min and that min and max are both valid integers.
	 */
	protected void performExtendedConfigValidation(Node promptNode, Nodes minLNodes, Nodes maxLNodes) {
		setMin(getValidNegOrPosInteger(minLNodes.get(0).getValue().trim())); 
		setMax(getValidNegOrPosInteger(maxLNodes.get(0).getValue().trim()));
		
		if(getMax() < getMin()) {
			throw new IllegalStateException("max cannot be less than min: " + promptNode.toXML());
		}
	}
}
