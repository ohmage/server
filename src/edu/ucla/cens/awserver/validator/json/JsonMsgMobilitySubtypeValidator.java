package edu.ucla.cens.awserver.validator.json;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * A validator of validators for handling the two different mobility subtypes: mode_features and mode_only.
 * 
 * @author selsky
 */
public class JsonMsgMobilitySubtypeValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "subtype";
	private Map<String, JsonObjectValidator[]> _validatorMap;
	
	/**
	 * @throws IllegalArgumentException if the provided Map is null or empty
	 */
	public JsonMsgMobilitySubtypeValidator(AwRequestAnnotator awRequestAnnotator, Map<String, JsonObjectValidator[]> validatorMap) {
		super(awRequestAnnotator);
		if(null == validatorMap || validatorMap.size() == 0) {
			throw new IllegalArgumentException("a non-null, non-emty validator map is required");
		}
		_validatorMap = validatorMap;
	}
	
	/**
	 * Validates mobility messages by dispatching to validators retrieved from the validatorMap based on the mobility subtype. 
	 * Before dispatch, the subtype is validated based on the key set retrieved from the validatorMap.
	 * 
	 * @return false if the subtype is invalid or if the any of the subtype's specific message elements are invalid
	 * @return true otherwise
	 */
	public boolean validate(AwRequest request, JSONObject jsonObject) {
		// First, check the subtype because nothing can be done without it
		String subtype = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == subtype) {
			getAnnotator().annotate(request, "subtype in mobility message is null");
			return false;
		}
		
		Set<String> keySet = _validatorMap.keySet();
		if(! keySet.contains(subtype)) {
			getAnnotator().annotate(request, "unknown subtype in mobility message: " + subtype);
			return false;
		}
		
		// Now execute the validation chain for the particular subtype
		JsonObjectValidator[] validators = _validatorMap.get(subtype);
		for(JsonObjectValidator validator : validators) {
			
			if(! validator.validate(request, jsonObject)) {
				return false;
			}
			
		}
		
		return true;
	}
}
