package edu.ucla.cens.awserver.validator;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;

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
		JSONArray array = JsonUtils.getJsonArrayFromJson(jsonObject, _key);
		
		// Each element in the array must contain a prompt_id and a response element
		// The response element is allowed to be null signifying "no response"
		int length = array.length();
		int[] idArray = new int[length];
		
		// first, do some preliminary validation against the data
		for(int i = 0; i < length; i++) {
		
			JSONObject object = JsonUtils.getJsonObjectFromJsonArray(array, i);
			
			if(null == object) {
				getAnnotator().annotate(request, "null object for prompt responses array at responses index " + i);
				return false;
			}
			
			Integer id = JsonUtils.getIntegerFromJson(object, "prompt_id");
			
			if(null == id) {
				getAnnotator().annotate(request, "missing or invalid prompt_id for prompt responses array at responses index " + i);
				return false;
			}
			
//			// validate that the prompt response is valid JSON
//			
//			JSONObject responseObject = JsonUtils.getObjectFromJson(object, "response");
//			
//			if(null == responseObject) {
//				getAnnotator().annotate(request, "response element is missing or invalid JSON for prompt responses array at " +
//					"responses index " + i);
//				return false;
//			}
			
			idArray[i] = id;
		}
		
		// Now check the DAO for prompt existence (the entire group) and grab the validation restrictions
		
		// TODO should really have another abstraction here to pass to the DAO (transfer object) rather than 
		// abusing the AwRequest
		request.setAttribute("promptIdArray", idArray); // Prep request for DAO
		
		try {
			
			getDao().execute(request);

			if(request.isFailedRequest()) {
				getAnnotator().annotate(request, "invalid number of prompts for prompt group");
				return false;
			}
			
			List<?> list = (List<?>) request.getAttribute("promptRestrictions");
			for(Object o : list) {
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
			
			
			
		} catch(DataAccessException daoe) { // unrecoverable error, just rethrow
			
			throw new ValidatorException(daoe);
		}
		
		return true;
	}
}
