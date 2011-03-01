package edu.ucla.cens.awserver.validator.json;

import java.util.List;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the mode element from an mode_only or mode_features mobility message.
 * 
 * @author selsky
 */
public class JsonMsgMobilityModeValidator extends AbstractAnnotatingJsonObjectValidator {
//	private static Logger _logger = Logger.getLogger(JsonMsgMobilityModeValidator.class);
	protected String _key = "mode";
	protected List<String> _allowedValues;
		
	/**
     * @throws IllegalArgumentException if the provded list for allowed values is null or empty
	 */
	public JsonMsgMobilityModeValidator(AwRequestAnnotator awRequestAnnotator, List<String> allowedValues) {
		super(awRequestAnnotator);
		if(null == allowedValues || allowedValues.size() == 0) {
			throw new IllegalArgumentException("a non-null non-empty array of allowed values is required");
		}
		_allowedValues = allowedValues;
	}
	
	/**
	 * Validates the mode. If set up to doFeaturesValidation on construction, will attempt to retrieve the mode from the features
	 * object instead of the "root" object. Assumes the features object exists.  
	 * 
	 * @return true if the value returned from the AwRequest for the key "mode" exists and is a valid mode
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		String mode = JsonUtils.getStringFromJsonObject(jsonObject, _key);; 
				
		if(null == mode) {
			getAnnotator().annotate(awRequest, "mode in message is null");
			return false;
		}
		
		if(! _allowedValues.contains(mode)) {
			getAnnotator().annotate(awRequest, "invalid mode: " + mode);
			return false;
		}
		
		return true;
	}
}
