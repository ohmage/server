package edu.ucla.cens.awserver.validator.survey;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.json.AbstractAnnotatingJsonObjectValidator;

/**
 * Validator for the responses element of a prompt message.
 * 
 * @author selsky
 */
public class JsonMsgSurveyResponsesExistValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "responses";
		
	public JsonMsgSurveyResponsesExistValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the responses array exists and is not empty
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(jsonObject, _key);
		
		if(null == array || array.length() == 0) {
			getAnnotator().annotate(awRequest, "responses array from survey message is null or empty");
			return false;
		}
		
		return true;
	}
}
