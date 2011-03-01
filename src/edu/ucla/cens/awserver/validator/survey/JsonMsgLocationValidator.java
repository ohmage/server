package edu.ucla.cens.awserver.validator.survey;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.json.AbstractAnnotatingJsonObjectValidator;

/** 
 * @author selsky
 */
public class JsonMsgLocationValidator extends AbstractAnnotatingJsonObjectValidator {
	private static Logger _logger = Logger.getLogger(JsonMsgLocationValidator.class);
	// TODO should be using a List of interfaces, not abstract classes
	// and should also refactor the whole Annotator idea. Could use an interface with annotate() instead of 
	// having to use getAnnotator().annotate(). 
	private List<AbstractAnnotatingJsonObjectValidator>  _validators;
		
	public JsonMsgLocationValidator(AwRequestAnnotator annotator, List<AbstractAnnotatingJsonObjectValidator> validators) {
		super(annotator);
		
		if(null == validators || validators.isEmpty()) {
			throw new IllegalStateException("a list of validators is required");
		}
		
		_validators = validators;
	}
	
	/**
	 * Validates the location object depending on the value of location_status.
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		
		String locationStatus = JsonUtils.getStringFromJsonObject(jsonObject, "location_status");
		
		if("unavailable".equals(locationStatus)) {
			
			if(null != JsonUtils.getObjectFromJsonObject(jsonObject, "location")) {
				_logger.warn("location_status is unavailable, but a location object was included " + JsonUtils.getObjectFromJsonObject(jsonObject, "location"));
				getAnnotator().annotate(awRequest, "location object exists even though location_status is unavailable");
				return false;
			}
			
		} else {
			
			if(null == JsonUtils.getJsonObjectFromJsonObject(jsonObject, "location")) {
				_logger.warn("location object is missing from message: " + jsonObject);
				getAnnotator().annotate(awRequest, "location object is missing from message");
				return false;
			}
			
			
			// lat, long, accuracy, provider, timestamp
			for(AbstractAnnotatingJsonObjectValidator validator : _validators) {
				if(! validator.validate(awRequest, jsonObject)) {
					return false;
				}
			}
		}
		
		return true;
	}
}
