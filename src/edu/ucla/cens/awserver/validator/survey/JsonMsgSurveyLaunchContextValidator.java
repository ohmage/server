package edu.ucla.cens.awserver.validator.survey;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.json.AbstractAnnotatingJsonObjectValidator;

/**
 * Validator for the survey_launch_context element of a survey message. 
 * 
 * @author selsky
 */
public class JsonMsgSurveyLaunchContextValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "survey_launch_context";
		
	public JsonMsgSurveyLaunchContextValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if survey_launch_context does not exist or survey_launch_context exists and is a valid JSON object and contains
	 * a launch_time property 
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject o = JsonUtils.getJsonObjectFromJsonObject(jsonObject, _key);
		
		if(null != o) {
			
			String launchTime = JsonUtils.getStringFromJsonObject(o, "launch_time");
			
			if(null == launchTime) {
				getAnnotator().annotate(awRequest, "launch_time from survey_launch_context is null");
				return false;
			}
			
		} else {
			
			getAnnotator().annotate(awRequest, "missing survey_launch_context");
			return false;
		}
		
		return true;
	}
}
