package edu.ucla.cens.awserver.validator.survey;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.json.AbstractAnnotatingJsonObjectValidator;
import edu.ucla.cens.awserver.validator.prompt.PromptValidator;
import edu.ucla.cens.awserver.validator.prompt.PromptValidatorCache;

/**
 * Validator for the set of prompt responses in a survey. A survey is sent to the server as a JSON array of prompt or repeatable set
 * objects. Each object must be validated against its configuration. A prompt object contains a prompt_id and a value. A repeatable
 * set contains a repeatable_set_id, a skipped element, and an array of prompt_id-value (object) arrays. Multi-choice and 
 * single-choice prompts may be custom types in which case both a selected value (or values for multi-choice) and the set from which
 * the value was chosen from (custom_choices) are uploaded. This class is the main driver for validating each prompt or repeatable
 * set response. Validation is performed against a cached configuration identified by the combination of campaign name, campaign 
 * version, and survey id.   
 * 
 * @author selsky
 */
public class JsonMsgSurveyResponsesValidator extends AbstractAnnotatingJsonObjectValidator {
	private static Logger _logger = Logger.getLogger(JsonMsgSurveyResponsesValidator.class);
	private String _key = "responses";
	private CacheService _cacheService;
	private PromptValidatorCache _promptValidatorCache;
		
	public JsonMsgSurveyResponsesValidator(AwRequestAnnotator awRequestAnnotator, CacheService configurationCacheService, 
			PromptValidatorCache pvCache) {
		super(awRequestAnnotator);
		if(null == configurationCacheService) {
			throw new IllegalArgumentException("a Configuration CacheService is required");
		}
		if(null == pvCache) {
			throw new IllegalArgumentException("a PromptValidatorCache is required");
		}
		_cacheService = configurationCacheService;
		_promptValidatorCache = pvCache;
	}
	
	/**
	 * Validates each prompt response in a survey upload. Assumes the provided JSONObject contains one survey.
	 * 
	 * TODO this method needs to be refactored because it does too much. In particular, the repeatable_set validation could be
	 * moved elsewhere. There are too many places where it returns.
	 * 
	 * @return true if each response is conformant with its configuration
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		JSONArray jsonArray = JsonUtils.getJsonArrayFromJsonObject(jsonObject, _key);
		String surveyId = JsonUtils.getStringFromJsonObject(jsonObject, "survey_id");
		Configuration configuration = 
			(Configuration) _cacheService.lookup(new CampaignNameVersion(awRequest.getCampaignName(), 
                                                                         awRequest.getCampaignVersion()));
		
		int arraySize = jsonArray.length();
	
		for(int i = 0; i < arraySize; i++) {
			JSONObject response = JsonUtils.getJsonObjectFromJsonArray(jsonArray, i);
			
			// determine whether it is a repeatable set or a prompt response
			String promptId = JsonUtils.getStringFromJsonObject(response, "prompt_id");
			
			if(null == promptId) { // check to see if there is a repeatable_set
				
				String repeatableSetId = JsonUtils.getStringFromJsonObject(response, "repeatable_set_id");
				
				if(null == repeatableSetId) { // the response is malformed
					getAnnotator().annotate(awRequest, "malformed response: missing prompt_id and repeatable_set_id: "
						+ "one must be present");
					return false;
				}
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("beginning to validate a repeatableSet: " + repeatableSetId);
				}
				
				// handle the repeatable set
				// a repeatable set must have the properties: skipped, not_displayed, repeatable_set_id, and a responses array
				// the repeatable_set must exist in the survey
				
				if(! configuration.repeatableSetExists(surveyId, repeatableSetId)) {
					getAnnotator().annotate(awRequest, "repeatableSet does not exist, id: " + repeatableSetId);
					return false;
				}
				
				// skipped here does not mean that the repeatable_set itself was skipped. it just means that the user clicked
				// "skip" (or some variant on skip) when presented with the continunation screen for the repeatable_set
				String skipped = JsonUtils.getStringFromJsonObject(response, "skipped");
				
				if(StringUtils.isEmptyOrWhitespaceOnly(skipped) || ! StringUtils.isBooleanString(skipped)) {
					getAnnotator().annotate(awRequest, "invalid skipped value in repeatable set, id: " + repeatableSetId);
					return false;
				}
				
				String notDisplayed = JsonUtils.getStringFromJsonObject(response, "not_displayed");
				
				if(StringUtils.isEmptyOrWhitespaceOnly(notDisplayed) || ! StringUtils.isBooleanString(notDisplayed)) {
					getAnnotator().annotate(awRequest, "invalid not_displayed value in repeatable set, id: " + repeatableSetId);
					return false;
				}
				
				JSONArray rsResponses = JsonUtils.getJsonArrayFromJsonObject(response, "responses");
				if(null == rsResponses) {
					getAnnotator().annotate(awRequest, "missing responses array in repeatable set, id: " + repeatableSetId);
					return false;
				}
				
				// a zero-length array of responses is allowed only if the repeatable set was not displayed
				int size = rsResponses.length();
				if(0 == size && ! Boolean.valueOf(notDisplayed)) {
					getAnnotator().annotate(awRequest, "empty responses array in repeatable set that was displayed, id: " 
						+ repeatableSetId);
					return false;
				}
				
				if(0 != size && Boolean.valueOf(notDisplayed)) {
					getAnnotator().annotate(awRequest, "non-empty responses array in repeatable set that was not displayed, id: " 
						+ repeatableSetId);
					return false;
				}
				
				if(! Boolean.valueOf(notDisplayed)) { // only validate if the repeatable set was displayed
				
					// now validate each prompt in the the responses array
					for(int j = 0; j < size; j++) {
						
						// Each repeatable set iteration in the responses array is grouped into its own anonymous array
						JSONArray innerArray = JsonUtils.getJsonArrayFromJsonArray(rsResponses, j);
						int innerArrayLength = innerArray.length();
						
						// make sure that every prompt in the repeatable set is accounted for
						int numberOfPromptsRequired = configuration.numberOfPromptsInRepeatableSet(surveyId, repeatableSetId);
						
						if(innerArrayLength != numberOfPromptsRequired) {
							getAnnotator().annotate(awRequest, "incorrect number of prompts returned in repeatable set iteration. "
								+ numberOfPromptsRequired + " expected, " + innerArrayLength + "received. id: "+ repeatableSetId);
							return false;
						}
						
						// now check each prompt in the repeatable set
						for(int k = 0; k < innerArrayLength; k++) { 
							
							// each array entry is a JSONObject representing a prompt response
							JSONObject promptResponseJsonObject = JsonUtils.getJsonObjectFromJsonArray(innerArray, k);
							if(null == promptResponseJsonObject) {
								getAnnotator().annotate(awRequest, "null json object at array index "+ k 
									+ "for repeatable set id "+ repeatableSetId);
								return false;
							}
							
							String repeatableSetPromptId = JsonUtils.getStringFromJsonObject(promptResponseJsonObject, "prompt_id");
							if(null == repeatableSetPromptId) {
								getAnnotator().annotate(awRequest, "missing prompt_id in json object at array index "+ k 
										+ " for repeatable set id "+ repeatableSetId);
								return false;
							}
							
							// make sure the prompt exists in the configuration
							if(! configuration.promptExists(surveyId, repeatableSetId, repeatableSetPromptId)) {
								getAnnotator().annotate(awRequest, "unknown prompt in json object at array index "+ k 
										+ " for repeatable set id "+ repeatableSetId);
								return false;
							}
							
							Prompt prompt = configuration.getPrompt(surveyId, repeatableSetId, repeatableSetPromptId);
							String promptType = configuration.getPromptType(surveyId, repeatableSetId, repeatableSetPromptId);
							PromptValidator pv = _promptValidatorCache.getValidatorFor(promptType);
							
							if(_logger.isDebugEnabled()) {
								_logger.debug("validating prompt " + repeatableSetPromptId + " in repeatableSet " + repeatableSetId);
							}
							
							if(! pv.validate(prompt, promptResponseJsonObject)) {
								getAnnotator().annotate(awRequest, "invalid value for prompt_id " + repeatableSetPromptId + " in "
									+ "json object at responses array index " + k + " for repeatable set id "+ repeatableSetId 
									+ " in survey " + surveyId);
								return false;
							}
						}
					}
				}
				
			} else { // Just a single prompt response
				
				if(! configuration.promptExists(surveyId, promptId)) {
					getAnnotator().annotate(awRequest, "unknown prompt for survey_id, prompt_id [" + surveyId + ", " + promptId + "]");
					return false;
				}
				
				String promptType = configuration.getPromptType(surveyId, promptId);
				PromptValidator pv = _promptValidatorCache.getValidatorFor(promptType);
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("validating prompt " + promptId);
				}
				
				if(! pv.validate(configuration.getPrompt(surveyId, promptId), response)) {
					getAnnotator().annotate(awRequest, "invalid value for prompt_id " + promptId + " in " +
						"json object at responses array index " + i + " for survey " + surveyId);
					return false;
				}
			}
		}
		
		return true;
	}
}
