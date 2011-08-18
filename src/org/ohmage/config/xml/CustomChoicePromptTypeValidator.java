package org.ohmage.config.xml;

import java.util.HashSet;
import java.util.Set;

import nu.xom.Node;
import nu.xom.Nodes;

/**
 * Validator for multi_choice_custom and single_choice_custom prompt types.
 * 
 * @author selsky
 */
public class CustomChoicePromptTypeValidator extends ChoicePromptTypeValidator {
	
	/**
	 * Zero properties are allowed in custom choice prompt types, but if properties are configured, they are checked here.
	 */
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);

		// Check to see whether there are any properties to validate
		Nodes propertiesNodes = promptNode.query("properties");
		
		if(propertiesNodes.size() > 0) {
		
			// All k must be number
			// 'v' nodes exist at this point because of schema validation
			
			Nodes kNodes = promptNode.query("properties/property/key"); // could check for the number of 'p' nodes here, but 
			                                                            // the number of 'k' nodes == the number of 'p' nodes
			                                                            // and the values of the 'k' nodes are what needs to be 
			                                                            // validated 
			int kSize = kNodes.size();
			for(int j = 0; j < kSize; j++) {
				int key = getValidNonNegativeInteger(kNodes.get(j).getValue().trim());
				if(choicesContains(key)) {
					throw new IllegalArgumentException("duplicate choice key found: " + key); 
				}
				addChoice(key, null);
			}
			
			Nodes lNodes = promptNode.query("properties/property/label");
					
			// Make sure there are not duplicate values
			Set<String> valueSet = new HashSet<String>();
			int vSize = lNodes.size();
			
			for(int i = 0; i < vSize; i++) {
				int key = Integer.parseInt(kNodes.get(i).getValue().trim());
				String value = lNodes.get(i).getValue().trim();
				if(! valueSet.add(value)) {
					throw new IllegalArgumentException("duplicate choice label found: " + value);
				}
				addChoice(key, value);
			}
		}
	}
}
