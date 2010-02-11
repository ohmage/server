package edu.ucla.cens.awserver.validator.json;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.domain.PromptType;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.ValidatorException;
import edu.ucla.cens.awserver.validator.prompt.PromptResponseValidator;
import edu.ucla.cens.awserver.validator.prompt.PromptResponseValidatorFactory;

/**
 * Validator for the responses from a prompt message.
 * 
 * @author selsky
 */
public class JsonMsgPromptResponsesValidator extends AbstractDaoAnnotatingJsonObjectValidator {
	private static Logger _logger = Logger.getLogger(JsonMsgPromptResponsesValidator.class);
	private String _key = "responses";
		
	public JsonMsgPromptResponsesValidator(AwRequestAnnotator awRequestAnnotator, Dao dao) {
		super(awRequestAnnotator, dao);
	}
	
	/**
	 * Validates a JSONArray of prompt responses. Assumes the array exists in the provide JSONObject. The entire array is checked 
	 * to make sure it is conformant with the allowed contents for its specified prompt group. Each prompt response in the array
	 * is checked to make sure it conforms to the type it represents. 
	 * 
	 * @return true if each response is conformant with its prompt type and group
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		JSONArray jsonArray = JsonUtils.getJsonArrayFromJsonObject(jsonObject, _key);
		
		// Each element in the array must contain a prompt_id and a response element
		int jsonArrayLength = jsonArray.length();
		int[] idArray = new int[jsonArrayLength];
		
		// first, do some preliminary validation against the data
		for(int i = 0; i < jsonArrayLength; i++) {
		
			JSONObject object = JsonUtils.getJsonObjectFromJsonArray(jsonArray, i);
			
			if(null == object) { // invalid data from phone
				getAnnotator().annotate(awRequest, "null object for prompt responses array at responses index " + i);
				return false;
			}
			
			Integer id = JsonUtils.getIntegerFromJsonObject(object, "prompt_id");
			
			if(null == id) { // invalid data from phone
				getAnnotator().annotate(awRequest, "missing or invalid prompt_id for prompt responses array at responses index " + i);
				return false;
			}
			
			idArray[i] = id;
		}
		
		// Now check the DAO for prompt existence (the entire group) and grab the validation restrictions (i.e., the prompt type)
		
		awRequest.setAttribute("promptIdArray", idArray); // Prep awRequest for DAO
		
		try {
			
			getDao().execute(awRequest);

		} catch(DataAccessException daoe) { // unrecoverable error, just rethrow
			
			throw new ValidatorException(daoe);
		}
		
		// This error is due to the phone sending data that does not match what is in the db so there is a serious configuration 
		// problem either on the client or in the server db.
		if(awRequest.isFailedRequest()) {
			throw new ValidatorException("cannot perform validation because an invalid number of prompts was sent in the request");
		}
		
		List<?> promptTypeList = (List<?>) awRequest.getAttribute("promptRestrictions");
		
//		if(_logger.isDebugEnabled()) {
//			StringBuilder builder = new StringBuilder();
//			
//			for(Object o : promptTypeList) {
//				builder.append(o);
//				builder.append("\n");
//			}
//			
//			_logger.debug("found the following prompts to validate against: " + builder.toString());
//		}
		
		// Now validate the contents of each prompt response
		
		for(int i = 0; i < jsonArrayLength; i++) {
			
			JSONObject promptResponse = JsonUtils.getJsonObjectFromJsonArray(jsonArray, i);
			
			PromptType promptType = getPromptTypeForPromptConfigId(promptTypeList, 
					JsonUtils.getIntegerFromJsonObject(promptResponse, "prompt_id"));
			
			String response = JsonUtils.getStringFromJsonObject(promptResponse, "response");
			
			if(! "RESPONSE_SKIPPED".equals(response)) { // 'RESPONSE_SKIPPED' is a valid response for any prompt
				
				PromptResponseValidator validator = PromptResponseValidatorFactory.make(promptType);
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("validating prompt response value " + response + " using " + promptType);
				}
				
				// Set the currentPromptId in order for it to be sent back to the phone in the cause of validation failure
				awRequest.setAttribute("currentPromptId", JsonUtils.getIntegerFromJsonObject(promptResponse, "prompt_id"));
								
				if(! validator.validate(response)) {
					getAnnotator().annotate(awRequest, "invalid prompt response");
					return false;
				}
			}
		}
		
		_logger.info("successfully validated prompt responses");
		return true;
	}
	
	private PromptType getPromptTypeForPromptConfigId(List<?> list, int promptConfigId) {

		for(Object object : list) {
			
			PromptType pt = (PromptType) object;
			
			if(pt.getPromptConfigId() == promptConfigId) {
				return pt;
			}
		}
		
		// if this code runs, it means previous validation and db querying has logical errors
		throw new IllegalArgumentException("no prompt type exists for prompt config id " + promptConfigId);
	}
}
