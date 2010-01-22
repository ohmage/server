package edu.ucla.cens.awserver.service;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.JsonObjectValidator;

/**
 * A service for validating the contents of JSON messages. The messages are sent to AW as a JSON Array where each element in the 
 * array corresponds to a type defined by the "t" (type) parameter in the original request URL. 
 * 
 * The JSON spec can be found <a href="http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-JSON">here</a>.
 * 
 * @author selsky
 */
public class JsonMessageContentValidationService implements Service {
//	private static Logger _logger = Logger.getLogger(JsonMessageContentValidationService.class);
	private Map<String, JsonObjectValidator[]> _validatorMap;
	private AwRequestAnnotator _noDataAnnotator;
	private AwRequestAnnotator _incorrectEntryAnnotator;;
	
	/**
	 * @throws IllegalArgumentException
	 * @throws IllegalArgumentException 
	 * @throws IllegalArgumentException  
	 */
	public JsonMessageContentValidationService(Map<String, JsonObjectValidator[]> validatorMap, 
		AwRequestAnnotator noDataAnnotator, AwRequestAnnotator incorrectEntryAnnotator) {
		
		if(null == validatorMap || validatorMap.size() == 0) {
			throw new IllegalArgumentException("the provided validator Map cannot be null or empty");
		}
		
		if(null == noDataAnnotator) {
			throw new IllegalArgumentException("the provided AwRequestAnnotator (noDataAnnotator) cannot be null");
		}
		
		if(null == incorrectEntryAnnotator) {
			throw new IllegalArgumentException("the provided AwRequestAnnotator (incorrectEntryAnnotator) cannot be null");
		}
		
		_validatorMap = validatorMap;
		_noDataAnnotator = noDataAnnotator;
		_incorrectEntryAnnotator = incorrectEntryAnnotator;
	}
	
	/**
	 * Validates the content of our sensor data upload JSON messages. The messages come in three main types: mobility mode only,
	 * mobility features, and prompt. Each prompt type is defined by a type in the data store (currently, the prompt_type table).
	 * 
	 * The messages are sent in a JSON array where each array element represents a message. The type of the messages is 
	 * found in the AwRequest attribute <code>requestType</code>. The array must contain only messages that belong to the 
	 * request type. 
	 * 
	 * This method is fail-fast in that it will mark an entire array of messages as invalid if a single entry in the array fails
	 * validation. The reason this is done is that incorrect data in the messages implies a logical error on the device uploading
	 * the data (or that the server validation rule is too strict) and also that it will be simpler to fix one error at a time 
	 * rather than having to go through many error messages in a server response.
	 * 
	 * A syntactically valid JSON Array that is found in the AwRequest using the key <code>jsonData</code> is required.
	 */
	public void execute(AwRequest awRequest) {
		  
//		  - validation for elements which must occur in all packets
//		  -- handle Double.NaN for latitude and longitude (MySQL will insert 0 so make it null instead) 
//		  - validation for mobility-specific messages (mode_only & mode_features)
//		  - validation for prompt-specific messages
//		  -- each prompt type has a data type in the db (therefore an inferrable validation rule)
		
		JSONArray jsonArray = (JSONArray) awRequest.getAttribute("jsonData");
		int length = jsonArray.length();
		
		if(0 == length) {
			
			_noDataAnnotator.annotate(awRequest, "no elements in the data array");
			return;
			
		}
		
		for(int i = 0; i < length; i++) {
			
			JSONObject jsonObject = null;
			
			try {
		
				jsonObject = jsonArray.getJSONObject(0);
				
			} catch (JSONException jsone) {
				
				_incorrectEntryAnnotator.annotate(awRequest, "missing top-level JSON object in data packet");
				return;
				
			}
			
			// Given the request type, retrieve the validator array to execute for the particular type

			JsonObjectValidator[] validators = _validatorMap.get(awRequest.getAttribute("requestType"));
			
			for(JsonObjectValidator validator : validators) {
				
				if(! validator.validate(awRequest, jsonObject)) {
					
					return; // bail out because somewhere within a message there is a validation failure 
					
				}
			}
		}	
	}
}
