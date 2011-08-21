package org.ohmage.config.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nu.xom.Node;
import nu.xom.Nodes;

import org.ohmage.config.grammar.custom.ConditionValuePair;

/**
 * Single and multi-choice (custom and non-custom) prompt type validator. 
 * 
 * @author Joshua Selsky
 */
public class ChoicePromptTypeValidator extends AbstractNumberPromptTypeValidator {
	private Map<Integer, String> choices;
	
	public ChoicePromptTypeValidator() {
		choices = new HashMap<Integer, String>();
	}
	
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);

		// At least two key-label pairs must be present
		// All keys must be number
		
		Nodes keyNodes = promptNode.query("properties/property/key"); // could check for the number of 'p' nodes here, but 
		                                                              // the number of 'k' nodes == the number of 'p' nodes
		                                                              // and the values of the 'k' nodes are what needs to be validated 
		if(keyNodes.size() < 2) {
			throw new IllegalStateException("At least 2 'property' nodes are required for prompt:\n" + promptNode.toXML());
		}

		// Make sure there are not duplicate keys
		int kSize = keyNodes.size();
		for(int j = 0; j < kSize; j++) {
			int key = getValidNonNegativeInteger(keyNodes.get(j).getValue().trim());
			if(choices.containsKey(key)) {
				throw new IllegalArgumentException("duplicate found for choice key: " + key);
			}
			
			choices.put(getValidNonNegativeInteger(keyNodes.get(j).getValue().trim()), null);
		}
		
		Nodes labelNodes = promptNode.query("properties/property/label");
				
		// Make sure there are not duplicate labels
		Set<String> labelSet = new HashSet<String>();
		int lSize = labelNodes.size();
		
		for(int i = 0; i < lSize; i++) {
			if(! labelSet.add(labelNodes.get(i).getValue().trim())) {
				throw new IllegalArgumentException("duplicate found for label: " + labelNodes.get(i).getValue().trim());
			}
			choices.put(Integer.parseInt(keyNodes.get(i).getValue().trim()), labelNodes.get(i).getValue().trim());
		}
		
		// If values exists, there must be one present for each key-label pair
		Nodes valueNodes = promptNode.query("properties/property/value");
		if((0 != valueNodes.size()) && (valueNodes.size() != labelNodes.size())) {
				throw new IllegalArgumentException("The number of value nodes is not equal to the number of label nodes. " +
					"If values are present, each label must also specify a value");
		}
		
		// This is an edge case, but until we have more displayTypes it seems ok here
		// Eventually displayType will either be repurposed or thrown out altogether.
		
		String promptType = promptNode.query("promptType").get(0).getValue().trim();
		if("single_choice".equals(promptType)) {
			
			String displayType = promptNode.query("displayType").get(0).getValue().trim();
			if("count".equals(displayType) || "measurement".equals(displayType)) {
				
				// An integer value is required for each choice. 
				Nodes vNodes = promptNode.query("properties/property/value");
				if(vNodes.size() < 1) {
					throw new IllegalArgumentException("values are required for single_choice prompts that have a displayType of "
						+ "count or measurement");					
				}
				if(vNodes.size() != keyNodes.size()) {
					throw new IllegalArgumentException("values are required for each choice in single_choice prompts that have a " 
						+ "displayType of count or measurement");
				}
				int vNodesSize = vNodes.size();
				for(int i = 0; i < vNodesSize; i++) {
					try {
						Float.parseFloat(vNodes.get(i).getValue().trim());
					} catch(NumberFormatException nfe) {
						throw new IllegalArgumentException("Value must be an integer for choice option in prompt " 
							+ promptNode.toXML(), nfe);
					}
				}
			}
		}
	}

	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			int i = 0; 
			try {
				i = Integer.parseInt(pair.getValue());
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("invalid condition value: " + i, nfe);
			}
			
			if(! choices.containsKey(i)) {
				throw new IllegalArgumentException("Value not found in set of choices: " + pair.getValue());
			} 
		}
		
		// the only conditions allowed are == and !=
		String condition = pair.getCondition();
		
		if(! "==".equals(condition) && ! "!=".equals(condition)) {
			throw new IllegalArgumentException("invalid condition in multi or single choice prompt: " + pair.getCondition());
		}
	}
	
	/**
	 * Checks whether the provided value exists in one of the configured choices.
	 */
	public void checkDefaultValue(String value) {
		if(! choices.containsValue(value)) {
			throw new IllegalArgumentException("default value [" + value + "] is missing from choices");
		}
	}
	
	/**
	 * Returns whether or not the list of choices contains some key.
	 * 
	 * @param key The key whose existence is being checked.
	 * 
	 * @return Whether or not the list of choices contains some key.
	 */
	protected boolean choicesContains(Integer key) {
		return choices.containsKey(key);
	}
	
	/**
	 * Adds a new integer key and string value as a possible choice replacing
	 * and returning any existing value associated with the key.
	 * 
	 * @param key The integer key.
	 * 
	 * @param value The string value.
	 * 
	 * @return If there already existed some key in the choice list, the new
	 * 		   value will replace the old value and the old value is returned.
	 */
	protected String addChoice(Integer key, String value) {
		return choices.put(key, value);
	}
	
	protected void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes) {
		// do nothing
	}
}
