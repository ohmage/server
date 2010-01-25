package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.ValidatorException;

/**
 * Validator for the group id from a prompt message.
 * 
 * TODO this class is nearly identical to JsonMsgPromptVersionIdValidator
 * 
 * @author selsky
 */
public class JsonMsgPromptGroupIdValidator extends AbstractDaoAnnotatingJsonObjectValidator {
	private String _key = "group_id";
		
	public JsonMsgPromptGroupIdValidator(AwRequestAnnotator awRequestAnnotator, Dao dao) {
		super(awRequestAnnotator, dao);
	}
	
	/**
	 * @return true if the value returned from the JSONObject for the key "group_id" exists and is a valid group id for the 
	 * campaign found in the AwRequest.
	 * @return false otherwise
	 */
	public boolean validate(AwRequest request, JSONObject jsonObject) {		 
		String groupId = JsonUtils.getStringFromJson(jsonObject, _key);
		
		if(null == groupId) {
			getAnnotator().annotate(request, "group_id in message is null or invalid");
			return false;
		}
		
		request.setAttribute("groupId", groupId);
		
		try {
		
			getDao().execute(request);
			
		} catch(DataAccessException daoe) { // unrecoverable error, just rethrow
			
			throw new ValidatorException(daoe);
		}
		
		if(request.isFailedRequest()) {
			getAnnotator().annotate(request, "prompt group_id " + groupId + " not found for campaign " + request.getAttribute("subdomain"));
			return false;
		}
		
		return true;
	}
}
