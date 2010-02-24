package edu.ucla.cens.awserver.validator.json;

import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
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
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		String groupId = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == groupId) {
			getAnnotator().annotate(awRequest, "group_id in message is null or invalid");
			return false;
		}
		
		awRequest.setGroupId(groupId);
		
		try {
		
			getDao().execute(awRequest);
			
		} catch(DataAccessException daoe) { // unrecoverable error, just rethrow
			
			throw new ValidatorException(daoe);
		}

		return true;
	}
}
