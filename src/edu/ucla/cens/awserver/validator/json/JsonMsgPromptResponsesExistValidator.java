package edu.ucla.cens.awserver.validator.json;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validator for the responses element of a prompt message.
 * 
 * @author selsky
 */
public class JsonMsgPromptResponsesExistValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "responses";
		
	public JsonMsgPromptResponsesExistValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the responses array exists and is not empty
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(jsonObject, _key);
		
		if(null == array || array.length() == 0) {
			getAnnotator().annotate(awRequest, "responses array from prompt message is null or empty");
			return false;
		}
		
		return true;
	}
}
