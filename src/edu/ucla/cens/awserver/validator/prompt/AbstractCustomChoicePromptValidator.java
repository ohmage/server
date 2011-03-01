package edu.ucla.cens.awserver.validator.prompt;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public abstract class AbstractCustomChoicePromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(AbstractCustomChoicePromptValidator.class);
	
	/**
	 * Returns a set of integers representing valid choice keys if the custom_choices JSON fragment contains choice_ids that are 
	 * non-duplicate integers and choice_values that are not empty strings.
	 */
	protected Set<Integer> validateCustomChoices(JSONArray choices, Prompt prompt) {
		// Validate the choice keys
		int numberOfCustomChoices = choices.length();
		Set<Integer> choiceSet = new HashSet<Integer>();
		
		for(int i = 0; i < numberOfCustomChoices; i++) {
			JSONObject choiceObject = JsonUtils.getJsonObjectFromJsonArray(choices, i);
			if(null == choiceObject) {
				if(_logger.isDebugEnabled()) {
					_logger.debug("Malformed custom choice message. Expected a JSONObject at custom_choices index " 
						+ i + "  for " + prompt.getId());
				}
				return null;
			}
			
			Integer choiceKey = JsonUtils.getIntegerFromJsonObject(choiceObject, "choice_id");
			if(null == choiceKey) {
				if(_logger.isDebugEnabled()) {
					_logger.debug("Malformed custom choice message. Expected an integer choice_id at custom_choices index " 
						+ i + "  for " + prompt.getId());
				}
				return null;
			}
			
			// make sure there are also values, duplicates allowed (TODO - is that correct??)
			String choiceValue = JsonUtils.getStringFromJsonObject(choiceObject, "choice_value");
			if(StringUtils.isEmptyOrWhitespaceOnly(choiceValue)) {
				if(_logger.isDebugEnabled()) {
					_logger.debug("Malformed custom choice message. Expected a choice_value at custom_choices index " 
						+ i + "  for " + prompt.getId());
				}
				return null;
			}
			
			if(! choiceSet.add(choiceKey)) {
				if(_logger.isDebugEnabled()) {
					_logger.debug("duplicate custom_choice found for prompt " + prompt.getId() + ". custom choices: " + choices);
				}
				return null;
			}
		}
		
		return choiceSet;
	}
}
