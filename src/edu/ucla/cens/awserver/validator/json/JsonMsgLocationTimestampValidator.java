package edu.ucla.cens.awserver.validator.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * This is a direct copy of JsonMsgDateValidator with line 25 changed.
 * 
 * @author selsky
 */
public class JsonMsgLocationTimestampValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "timestamp";
		
	public JsonMsgLocationTimestampValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		String date = JsonUtils.getStringFromJsonObject(JsonUtils.getJsonObjectFromJsonObject(jsonObject, "location"), _key);
		
		if(null == date) {
			getAnnotator().annotate(awRequest, "timestamp in location object is null");
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setLenient(false); // enforce valid dates 
		
		try {
		
			sdf.parse(date);
			
		} catch (ParseException pe) {
			
			getAnnotator().annotate(awRequest, "unparseable location timestamp. " + pe.getMessage() + " date value: " + date);
			return false;
		}
		
		return true;
	}
}
