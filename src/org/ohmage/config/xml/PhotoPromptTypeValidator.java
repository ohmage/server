package org.ohmage.config.xml;

import nu.xom.Node;
import nu.xom.Nodes;

import org.ohmage.config.grammar.custom.ConditionValuePair;

/**
 * Validates the photo prompt type.
 * 
 * @author selsky
 */
public class PhotoPromptTypeValidator extends AbstractNumberPromptTypeValidator {
	
	/**
	 * Checks that the min and max properties exist and that their values are valid.
	 * 
	 * @throws IllegalArgumentException if the configuration is invalid.
	 */
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);
		
		// make sure there are no unknown props
		Nodes propertyNodes = promptNode.query("properties/property"); 
		if(1 != propertyNodes.size()) {
			throw new IllegalStateException("invalid prompt configuration: " + promptNode.toXML());
		}
		
		Nodes resNodes = promptNode.query("properties/property/key[normalize-space(text())='res']");
		if(1 != resNodes.size()) {
			throw new IllegalStateException("missing 'res' property for XML fragment: " + promptNode.toXML());
		}
						
		Nodes resLabelNodes = resNodes.get(0).getParent().query("label"); // the schema check should prevent this 
		if(1 != resLabelNodes.size()) {
			throw new IllegalStateException("missing or extra 'res' label for XML fragment: " + promptNode.toXML());
		}
		
		getValidPositiveInteger(resLabelNodes.get(0).getValue().trim()); // TODO we need a valid set of values for res (720, etc)
	}

	/**
	 * Checks values (from, e.g., conditions) against the min and max defined by this instance. 
	 */
	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			throw new IllegalArgumentException("invalid value for photo prompt type: " + pair.getValue());
		}
	}
	
	/**
	 * @throws IllegalArgumentException because photo prompts cannot have default values.
	 */
	public void checkDefaultValue(String value) {
		throw new IllegalArgumentException("default values are disallowed for photo prompts.");
	}
	
	protected void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes) {
		// do nothing
	}
}
