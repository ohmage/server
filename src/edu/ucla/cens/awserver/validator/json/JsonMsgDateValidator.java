package edu.ucla.cens.awserver.validator.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Validates the date element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgDateValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "date";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgDateValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the value returned from the AwRequest for the key "date" exists and is of the form yyyy-MM-dd hh:mm:ss.
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		String date = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == date) {
			getAnnotator().annotate(awRequest, "date in message is null");
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setLenient(false); // enforce valid dates 
		
		try {
		
			sdf.parse(date);
			
		} catch (ParseException pe) {
			
			getAnnotator().annotate(awRequest, "unparseable date. " + pe.getMessage() + " date value: " + date);
			return false;
			
		}
		
		return true;
		
	}
}
