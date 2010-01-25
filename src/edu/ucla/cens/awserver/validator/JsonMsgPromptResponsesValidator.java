package edu.ucla.cens.awserver.validator;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.domain.PromptType;
import edu.ucla.cens.awserver.util.JsonUtils;
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
	public boolean validate(AwRequest request, JSONObject jsonObject) {		 
		JSONArray jsonArray = JsonUtils.getJsonArrayFromJson(jsonObject, _key);
		
		// Each element in the array must contain a prompt_id and a response element
		// The response element is allowed to be null signifying "no response"
		int jsonArrayLength = jsonArray.length();
		int[] idArray = new int[jsonArrayLength];
		
		// first, do some preliminary validation against the data
		for(int i = 0; i < jsonArrayLength; i++) {
		
			JSONObject object = JsonUtils.getJsonObjectFromJsonArray(jsonArray, i);
			
			if(null == object) {
				getAnnotator().annotate(request, "null object for prompt responses array at responses index " + i);
				return false;
			}
			
			Integer id = JsonUtils.getIntegerFromJson(object, "prompt_id");
			
			if(null == id) {
				getAnnotator().annotate(request, "missing or invalid prompt_id for prompt responses array at responses index " + i);
				return false;
			}
			
			idArray[i] = id;
		}
		
		// Now check the DAO for prompt existence (the entire group) and grab the validation restrictions
		
		// TODO should really have another abstraction here to pass to the DAO (transfer object) rather than 
		// abusing the AwRequest
		request.setAttribute("promptIdArray", idArray); // Prep request for DAO
		
		try {
			
			getDao().execute(request);

		} catch(DataAccessException daoe) { // unrecoverable error, just rethrow
			
			throw new ValidatorException(daoe);
		}
		
		if(request.isFailedRequest()) {
			getAnnotator().annotate(request, "invalid number of prompts for prompt group");
			return false;
		}
		
		List<?> promtpTypeList = (List<?>) request.getAttribute("promptRestrictions");
		
		if(null == promtpTypeList || promtpTypeList.isEmpty()) {
			getAnnotator().annotate(request, "prompt type restrictions not found");
			return false;
		}
		
		if(promtpTypeList.size() != jsonArrayLength) {
			getAnnotator().annotate(request, "incorrect number of prompt type restrictions found");
			return false;
		}
		
		for(Object o : promtpTypeList) {
			_logger.info(o);
		}
		
		// ok, now the prompt response can be validated
		// check the prompt type. if necessary, parse the restriction JSON

//			INSERT INTO prompt_type (type, restriction) VALUES
//			  ("time_military", NULL),
//			  ("array_boolean", "3"),
//			  ("map", '{0:"10",1:"20",2:"30",3:"40",4:"50",5:"60+"}'),
//			  ("map", '{0:"<4",1:"5",2:"6",3:"7",4:"8",5:">8"}'),
//			  ("map", '{0:"very bad",1:,2:,3:"very good"}'),
//			  ("map", '{0:"not at all",1:,2:"slightly",3:,4:"moderately",5:,6:"extremely"}'),
//			  ("map", '{0:"never",1:"almost never",2:"sometimes",3:"fairly often",4:"very often"}'),
//			  ("map", '{0:"none",1:"light",2:"moderate",3:"vigorous"}'),
//			  ("map", '{0:"1",1:"2",2:"3",3:"4",4:"5",5:"6",6:"7",7:"8",8:"9",9:"10+"}'),
//			  ("map", '{1:"Yes",0:"No"}'),
//			  ("array_boolean", "6"),
//			  ("null", NULL),
//			  ("map", '{0:"10",1:"20",2:"30",3:"40",4:"50",5:"60+",6:"N/A"}');
        
		// Now validate the contents of each prompt response
		
		for(int i = 0; i < jsonArrayLength; i++) {
			
			JSONObject promptResponse = JsonUtils.getJsonObjectFromJsonArray(jsonArray, i);
			
			PromptType promptType = getPromptTypeForPromptConfigId(promtpTypeList, 
					JsonUtils.getIntegerFromJson(promptResponse, "prompt_id"));
			
			String response = JsonUtils.getStringFromJson(promptResponse, "response");
			
			PromptResponseValidator validator = PromptResponseValidatorFactory.make(promptType);
			
			if(! validator.validate(response)) {
				
				getAnnotator().annotate(request, "invalid prompt response");
				return false;
				
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
