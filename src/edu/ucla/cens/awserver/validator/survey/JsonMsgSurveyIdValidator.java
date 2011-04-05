package edu.ucla.cens.awserver.validator.survey;

import org.json.JSONObject;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.json.AbstractAnnotatingJsonObjectValidator;

/**
 * Validates the survey_id element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgSurveyIdValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "survey_id";
	private CacheService _cacheService;
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgSurveyIdValidator(AwRequestAnnotator awRequestAnnotator, CacheService cacheService) {
		super(awRequestAnnotator);
		if(null == cacheService) {
			throw new IllegalArgumentException("a CacheService is required");
		}
		_cacheService = cacheService;
	}
	
	/**
	 * @return true if the survey_id from the JSONObject is found in a configuration bound to the campaign name-version pair found 
	 * in the AwRequest
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		String surveyId = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == surveyId) {
			getAnnotator().annotate(awRequest, "survey_id in message is null");
			return false;
		}
		
		Configuration configuration = (Configuration) _cacheService.lookup(awRequest.getCampaignUrn());
		
		if(null == configuration) { // this is bad because it means that previous validation failed or didn't run
			throw new IllegalStateException("missing configuration for campaign URN: " + awRequest.getCampaignUrn());
		}
		
		if(! configuration.surveyIdExists(surveyId)) {
			getAnnotator().annotate(awRequest, "survey_id in message does not exist for configuration");
			return false;
		}
		
		return true;
	}
}
