package org.ohmage.config.xml;

import nu.xom.Node;
import nu.xom.Nodes;


/**
 * A collection of helper methods for validating number-based prompt types.
 * 
 * @author selsky
 */
public abstract class AbstractNumberPromptTypeValidator extends AbstractPromptTypeValidator {
	private int min;
	private int max;
	
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);
		
		// make sure there are no unknown props
		Nodes propertyNodes = promptNode.query("properties/property"); 
		if(2 != propertyNodes.size()) {
			throw new IllegalStateException("invalid prompt configuration:\n" + promptNode.toXML());
		}
		
		Nodes minNodes = promptNode.query("properties/property/key[normalize-space(text())='min']");  
		if(1 != minNodes.size()) {
			throw new IllegalStateException("missing or extra 'min' property for XML fragment: " + promptNode.toXML());
		}
		
		Nodes maxNodes = promptNode.query("properties/property/key[normalize-space(text())='max']");
		if(1 != maxNodes.size()) {
			throw new IllegalStateException("missing or extra 'max' property for XML fragment: " + promptNode.toXML());
		}
		
		Nodes minLNodes = minNodes.get(0).getParent().query("label");
		if(1 != minLNodes.size()) {
			throw new IllegalStateException("missing or extra 'min' label for XML fragment: " + promptNode.toXML());
		}
		
		Nodes maxLNodes = maxNodes.get(0).getParent().query("label");
		if(1 != maxLNodes.size()) {
			throw new IllegalStateException("missing or extra 'max' label for XML fragment: " + promptNode.toXML());
		}
		
		performExtendedConfigValidation(promptNode, minLNodes, maxLNodes);
	} 
	
	public void checkDefaultValue(String value) {
		int intValue = 0;
		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Value is not an integer: " + value, nfe);
		}
		
		if(intValue < min || intValue > max) {
			throw new IllegalArgumentException("Value is out of min-max range: " + value);
		}
	}
	
	protected int getValidNegOrPosInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value, nfe);
		}
	}
	
	protected int getValidNonNegativeInteger(String value) {
		int i = 0;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value, nfe);
		}
		
		if(i < 0) {
			throw new IllegalArgumentException("Value must be non-negative: " + value);
		}
		
		return i; 
	}
	
	protected int getValidPositiveInteger(String value) {
		int i = 0;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value, nfe);
		}
		
		if(i < 1) {
			throw new IllegalArgumentException("Value must be positive: " + value);
		}
		
		return i; 
	}
	
	protected int getMin() {
		return min;
	}
	
	protected void setMin(int min) {
		this.min = min;
	}
	
	protected int getMax() {
		return max;
	}
	
	protected void setMax(int max) {
		this.max = max;
	}
	
	protected abstract void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes);
}
