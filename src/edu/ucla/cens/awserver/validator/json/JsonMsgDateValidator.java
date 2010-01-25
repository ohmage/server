package edu.ucla.cens.awserver.validator.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
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
	 * 
	 * TODO - validation parameters for date value sanity (e.g. date should not be in the past or even too far in the future?)
	 */
	public boolean validate(AwRequest request, JSONObject jsonObject) {
		String date = JsonUtils.getStringFromJson(jsonObject, _key);
		
		if(null == date) {
			getAnnotator().annotate(request, "date in message is null");
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		try { // this only makes sure that the date is parseable, not that the value is a sane date
		
			sdf.parse(date);
			
		} catch (ParseException pe) {
			
			getAnnotator().annotate(request, "unparseable date. " + pe.getMessage() + " date value: " + date);
			return false;
			
		}
		
		return true;
		
	}
}
