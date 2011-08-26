package org.ohmage.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.User;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.configuration.PromptProperty;
import org.ohmage.domain.survey.read.CustomChoiceItem;
import org.ohmage.domain.survey.read.PromptResponseMetadata;
import org.ohmage.domain.survey.read.SingleChoicePromptValueAndLabel;
import org.ohmage.domain.survey.read.SurveyResponseReadIndexedResult;
import org.ohmage.domain.survey.read.SurveyResponseReadResult;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.util.SurveyResponseReadWriterUtils;

/**
 * Services for survey response read.
 * 
 * @author Joshua Selsky
 */
public final class SurveyResponseReadServices {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseReadServices.class);
	
	/**
	 * Private to prevent instantiation.
	 */
	private SurveyResponseReadServices() { }
	
	/**
	 * Verifies that the promptIdList contains prompts that are present in the
	 * configuration.
	 * 
	 * @param request  The request to fail should the promptIdList be invalid.
	 * @param promptIdList  The prompt ids to validate.
	 * @param configuration  The configuration to use for prompt id lookup.
	 * @throws ServiceException if an invalid prompt id is detected.
	 * @throws IllegalArgumentException if request, promptIdList, or
	 * configuration are null.
	 */
	public static void verifyPromptIdsBelongToConfiguration(Request request, List<String> promptIdList, Configuration configuration)
		throws ServiceException {
		
		// check for logical errors
		if(request == null || promptIdList == null || configuration == null) {
			throw new IllegalArgumentException("A non-null request, promptIdList, and configuration are required");
		}
		
		for(String promptId : promptIdList) {
			if(configuration.getSurveyIdForPromptId(promptId) == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("The configuration for campaign ");
				sb.append(configuration.getUrn());
				sb.append(" did not contain the prompt id ");
				sb.append(promptId);
				String msg = sb.toString();
				request.setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, msg);
				throw new ServiceException(msg);
			}
		}
	}
	
	/**
	 * Verifies that the surveyIdList contains surveys that are present in the
	 * configuration.
	 * 
	 * @param request  The request to fail should the surveyIdList be invalid.
	 * @param surveyIdList  The survey ids to validate.
	 * @param configuration  The configuration to use for survey id lookup
	 * @throws ServiceException if an invalid survey id is detected.
	 * @throws IllegalArgumentException if request, surveyIdList, or
	 * configuration are null.
	 */
	public static void verifySurveyIdsBelongToConfiguration(Request request, List<String> surveyIdList, Configuration configuration)
		throws ServiceException {
		
		// check for logical errors
		if(request == null || surveyIdList == null || configuration == null) {
			throw new IllegalArgumentException("A non-null request, surveyIdList, and configuration are required");
		}
		
		for(String surveyId : surveyIdList) {
			if(! configuration.surveyIdExists(surveyId)) {
				StringBuilder sb = new StringBuilder();
				sb.append("The configuration for campaign ");
				sb.append(configuration.getUrn());
				sb.append(" did not contain the survey id ");
				sb.append(surveyId);
				String msg = sb.toString();
				request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, msg);
				throw new ServiceException(msg);
			}
		}
	}
	
	/**
	 * <p>Filters the provided surveyResponseList according to our ACL rules for
	 * survey responses:</p>
	 * <ul>
	 * <li>Owners of responses can view their data anytime.</li>
	 * <li>Supervisors can view any user's data anytime.</li>
	 * <li>Authors can view shared responses if the campaign is private.</li> 
	 * </ul> 
	 * 
	 * <p>This method assumes that the user's role has already been checked
	 * 
	 * 
	 * @param user The requester behind the survey response query.
	 * @param campaignId The campaign URN for the 
	 * @param surveyResponseList 
	 * @param privacyState
	 * @return
	 */
	public static List<SurveyResponseReadResult> performPrivacyFilter(User user, String campaignId,
		List<SurveyResponseReadResult> surveyResponseList, String privacyState) {
		
		// check for logical errors
		if(user == null  || StringUtils.isEmptyOrWhitespaceOnly(campaignId) || surveyResponseList == null) {
			throw new IllegalArgumentException("user, campaignId, and surveyResponseList must all be non-null");
		}
		
		if(surveyResponseList.isEmpty()) {
			return surveyResponseList;
		}
		
		int numberOfResults = surveyResponseList.size();
		
		// Supervisors can read all data all the time
		if(! user.isSupervisorInCampaign(campaignId)) {
			for(int i = 0; i < numberOfResults; i++) {
				
				SurveyResponseReadResult currentResult = surveyResponseList.get(i);
				
				// _logger.info(currentResult.getPrivacyState());
				
				// Filter based on our ACL rules
				
				if( 
				    // Owners and supervisors can see unshared responses
					(resultIsUnshared(currentResult) && ! currentResult.getUsername().equals(user.getUsername()))
					
					|| 
					
					((! resultIsUnshared(currentResult)) && (! currentResult.getUsername().equals(user.getUsername()) 
							&& ! user.isAuthorInCampaign(campaignId) && ! user.isAnalystInCampaign(campaignId))) 
					
					||
					
					// Owners, supervisors, and authors can see shared responses if the campaign is private 
					((user.getCampaignsAndRoles().get(campaignId).getCampaign().getPrivacyState().equals(CampaignPrivacyStateCache.PRIVACY_STATE_PRIVATE) 
						&& currentResult.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_SHARED)) 
						&& ! user.isAuthorInCampaign(campaignId) 
						&& ! currentResult.getUsername().equals(user.getUsername()))
						
				  ) {
					
					surveyResponseList.remove(i);
					i--;
					numberOfResults--;
				}
			}
		}
		
		// Filter based on the optional privacy state query parameter
		
		if(privacyState != null) {
			for(int i = 0; i < numberOfResults; i++) {
				if(! (surveyResponseList.get(i)).getPrivacyState().equals(privacyState)) { 
					surveyResponseList.remove(i);
					i--;
					numberOfResults--;
				}
			}
		}
		
		return surveyResponseList;
		
	}
	
	/**
	 * Checks whether the result is shared or unshared.
	 * 
	 * @param result The result to check.
	 * @return true if the result is shared; false otherwise.
	 */
	private static boolean resultIsUnshared(SurveyResponseReadResult result) {
		return ! result.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_SHARED); 
	}
	
	/* Methods for generating output */
	
	private static final String NEWLINE = System.getProperty("line.separator");
	
	/**
	 * 
	 * @param request
	 * @param numberOfSurveys
	 * @param numberOfPrompts
	 * @param indexedResultList
	 * @param outputColumns
	 * @param uniqueCustomChoiceMap
	 * @return
	 * @throws JSONException
	 */
	public static String generateJsonRowsOutput(SurveyResponseReadRequest request, 
			                                    int numberOfSurveys,
			                                    int numberOfPrompts,
			                                    List<SurveyResponseReadIndexedResult> indexedResultList,
			                                    List<String> outputColumns,
			                                    Map<String, List<CustomChoiceItem>> uniqueCustomChoiceMap) throws JSONException {
		
		LOGGER.info("Generating row-based JSON output");
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("number_of_prompts", numberOfPrompts);
		metadata.put("number_of_surveys", numberOfSurveys);
		metadata.put("items", outputColumns);
		main.put("metadata", metadata);
		
		if(indexedResultList.size() > 0) {
			
			JSONArray dataArray = new JSONArray();
			
			for(SurveyResponseReadIndexedResult result : indexedResultList) {
				
				JSONObject record = new JSONObject();
				
				for(String rowItem : outputColumns) {
				
					if("urn:ohmage:user:id".equals(rowItem)) {
						
						record.put("user", result.getUsername());
						
					} else if("urn:ohmage:context:client".equals(rowItem)) {
						
						record.put("client", result.getClient());
					
					} else if("urn:ohmage:context:timestamp".equals(rowItem)){
						
						record.put("timestamp", result.getTimestamp());
					
					} else if("urn:ohmage:context:timezone".equals(rowItem)) {
						
						record.put("timezone", result.getTimezone());
						
					} else if("urn:ohmage:context:utc_timestamp".equals(rowItem)) {
					
						record.putOpt("utc_timestamp", SurveyResponseReadWriterUtils.generateUtcTimestamp(result));
					
				    } else if("urn:ohmage:context:launch_context_long".equals(rowItem)) {
				    	
				    	record.put("launch_context_long", result.getLaunchContext() == null ? null : new JSONObject(result.getLaunchContext()));
				    	
				    } else if("urn:ohmage:context:launch_context_short".equals(rowItem)) {
				    	
				    	record.put("launch_context_short", result.getLaunchContext() == null ? null : SurveyResponseReadWriterUtils.shortLaunchContext(result.getLaunchContext()));
				    	
				    } else if("urn:ohmage:context:location:status".equals(rowItem)) {
				    
				    	record.put("location_status", result.getLocationStatus());
				    
				    } else if("urn:ohmage:context:location:latitude".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) {
				    		JSONObject location = new JSONObject(result.getLocation());
				    		
				    		if(! Double.isNaN(location.optDouble("latitude"))) {
				    			record.put("latitude", location.optDouble("latitude"));
							} else {
								record.put("latitude", JSONObject.NULL);
							}
				    		
				    	} else {
							record.put("latitude", JSONObject.NULL);
						}
				    
				    } else if("urn:ohmage:context:location:longitude".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) {
				    		JSONObject location = new JSONObject(result.getLocation());
				    		
				    		if(! Double.isNaN(location.optDouble("longitude"))) {
				    			record.put("longitude", location.optDouble("longitude"));
							} else {
								record.put("longitude", JSONObject.NULL);
							}
				    	} else {
							record.put("longitude", JSONObject.NULL);
						}
				    	
				    } else if("urn:ohmage:context:location:timestamp".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) { 
					    	JSONObject location = new JSONObject(result.getLocation());
					    	
					    	if(! "".equals(location.optString("timestamp")) ) { 
					    		record.put("location_timestamp", location.optString("timestamp"));
					    	} else {
					    		record.put("location_timestamp", JSONObject.NULL);
					    	}
				    	} else {
				    		record.put("location_timestamp", JSONObject.NULL);
				    	}
				    	
				    } else if("urn:ohmage:context:location:accuracy".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) {
				    		
					    	JSONObject location = new JSONObject(result.getLocation());
					    	
				    		if(! Double.isNaN(location.optDouble("accuracy"))) {
				    			record.put("location_accuracy", location.optDouble("accuracy"));
							} else {
								record.put("location_accuracy", JSONObject.NULL);
							}
				    	} else {
							record.put("location_accuracy", JSONObject.NULL);
						}
				    	
				    } else if("urn:ohmage:context:location:provider".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) { 
					    	JSONObject location = new JSONObject(result.getLocation());
					    	
					    	if(! "".equals(location.optString("provider")) ) { 
					    		record.put("location_provider", location.optString("provider"));
					    	} else {
					    		record.put("location_provider", JSONObject.NULL);
					    	}
				    	} else {
				    		record.put("location_provider", JSONObject.NULL);
				    	}
				    	
				    } else if ("urn:ohmage:survey:id".equals(rowItem)) {
				    	
				    	record.put("survey_id", result.getSurveyId());
				    	
				    } else if("urn:ohmage:survey:title".equals(rowItem)) {
				    	
				    	record.put("survey_title", result.getSurveyTitle());
				    
				    } else if("urn:ohmage:survey:description".equals(rowItem)) {
				    	
				    	record.put("survey_description", result.getSurveyDescription());
				    	
				    } else if("urn:ohmage:survey:privacy_state".equals(rowItem)) {
				    	
				    	record.put("privacy_state", result.getPrivacyState());
				    	
				    } else if("urn:ohmage:repeatable_set:id".equals(rowItem)) {
				    	
				    	record.put("repeatable_set_id", 
				    		result.getRepeatableSetId() == null ? JSONObject.NULL : result.getRepeatableSetId());
				    
				    } else if("urn:ohmage:repeatable_set:iteration".equals(rowItem)) {
				    	
				    	record.put("repeatable_set_iteration", 
				    		result.getRepeatableSetIteration() == null ? JSONObject.NULL : result.getRepeatableSetIteration());
				    
					} else if (rowItem.startsWith("urn:ohmage:prompt:id:")) {
						
						JSONObject promptObject = new JSONObject();
						
						Map<String, Map<String, PromptProperty>> choiceGlossaryMap = result.getChoiceGlossaryMap();
						Map<String, PromptResponseMetadata> promptResponseMetadataMap = result.getPromptResponseMetadataMap();
						Map<String, Object> promptResponseMap = result.getPromptResponseMap();
						Iterator<String> responseIterator = promptResponseMap.keySet().iterator();
						
						while(responseIterator.hasNext()) {
							String key = responseIterator.next();
							JSONObject response = new JSONObject();
							Map<String, PromptProperty> ppMap = choiceGlossaryMap.isEmpty() ? null : choiceGlossaryMap.get(key);
							List<CustomChoiceItem> customChoiceItemList = (null == uniqueCustomChoiceMap ? null : uniqueCustomChoiceMap.get(key));
							
							if(null != ppMap) {
								response.put("prompt_choice_glossary", SurveyResponseReadWriterUtils.choiceGlossaryToJson(ppMap));
							} 
							else if(null != customChoiceItemList)  {
								JSONObject choiceGlossaryObject = new JSONObject();
								for(CustomChoiceItem cci : customChoiceItemList) {
									JSONObject choice = new JSONObject();
									choice.put("label", cci.getLabel());
									choice.put("type", cci.getType());
									choiceGlossaryObject.put(String.valueOf(cci.getId()), choice);
								}
								response.put("prompt_choice_glossary", choiceGlossaryObject);
							}
							
							response.put("prompt_response", promptResponseMap.get(key));
							response.put("prompt_display_type", promptResponseMetadataMap.get(key).getDisplayType());
							response.put("prompt_unit", promptResponseMetadataMap.get(key).getUnit());
							response.put("prompt_type", promptResponseMetadataMap.get(key).getPromptType());
							response.put("prompt_text", promptResponseMetadataMap.get(key).getPromptText());
							response.put("prompt_index", request.getConfiguration().getIndexForPrompt(result.getSurveyId(), key));
							promptObject.put(key, response); // the key here is the prompt_id from the XML config
						}

						record.put("responses", promptObject);
					}
					
					if(request.getReturnId()) { // only allowed for json-rows output
						record.put("survey_key", result.getSurveyPrimaryKeyId());
					}
				}
				
				dataArray.put(record);
			}
			
			Set<String> tmpHashSet = new HashSet<String>();
			
			if(request.getCollapse()) {
				int size = dataArray.length();
				
				LOGGER.info("Number of results before collapsing: " + size);
				
				for(int i = 0; i < size; i++) {
					String string = dataArray.getJSONObject(i).toString();
					if(! tmpHashSet.add(string)) {
						dataArray.remove(i);
						size--;
						i--;
					} 
				}
				
				LOGGER.info("Number of results after collapsing: " + dataArray.length());
			}
			
			main.put("data", dataArray);
			
		} else {
			
			main.put("data", new JSONArray());
		}
		
		return request.getPrettyPrint() ? main.toString(4) : main.toString();		
	}
	
	/**
	 * 
	 * @param request
	 * @param numberOfSurveys
	 * @param numberOfPrompts
	 * @param indexedResultList
	 * @param outputColumns
	 * @param uniqueCustomChoiceMap
	 * @return
	 * @throws JSONException
	 */
	public static String generateMultiResultJsonColumnOutput(SurveyResponseReadRequest request, 
                                                             int numberOfSurveys,
                                                             int numberOfPrompts,
                                                             List<SurveyResponseReadIndexedResult> indexedResultList,
                                                             List<String> outputColumns,
                                                             Map<String, List<CustomChoiceItem>> uniqueCustomChoiceMap) throws JSONException {

		LOGGER.info("Generating multi-result column-based JSON output");
		
		// Each column id in the output gets mapped to its associated list of values 
		Map<String, List<Object>> columnMap = new HashMap<String, List<Object>> ();
		
		// For prompt responses, the choice glossary and prompt metadata only needs to be set in the "column" once.
		// Choice glossaries will be present for all choice types. For the custom types the glossaries are 
		// created by hand from the provided customChoiceMap.
		
		Map<String, Map<String, PromptProperty>> choiceGlossaryMap = new HashMap<String, Map<String, PromptProperty>>();
		Map<String, PromptResponseMetadata> promptResponseMetadataMap = new HashMap<String, PromptResponseMetadata>();
		
		for(SurveyResponseReadIndexedResult result : indexedResultList) {
			if(result.getChoiceGlossaryMap() != null) {
				Iterator<String> iterator = result.getChoiceGlossaryMap().keySet().iterator();
				while(iterator.hasNext()) {
					String key = iterator.next();
					if(! choiceGlossaryMap.containsKey(key)) {
						choiceGlossaryMap.put(key, result.getChoiceGlossaryMap().get(key));
					}
				}
			}
			Iterator<String> iterator = result.getPromptResponseMetadataMap().keySet().iterator();
			while(iterator.hasNext()) {
				String key = iterator.next();
				if(! promptResponseMetadataMap.containsKey(key)) {
					promptResponseMetadataMap.put(key, result.getPromptResponseMetadataMap().get(key));
				}	
			}
		}
		
		for(SurveyResponseReadIndexedResult result : indexedResultList) {
		
			for(String outputColumnKey : outputColumns) {
			
				if(! columnMap.containsKey(outputColumnKey)) {
					List<Object> columnList = new ArrayList<Object>();
					columnMap.put(outputColumnKey, columnList);
				}	
				
				if("urn:ohmage:user:id".equals(outputColumnKey)) {
					
					columnMap.get(outputColumnKey).add(result.getUsername());
					
				} else if("urn:ohmage:context:client".equals(outputColumnKey)) {
					
					columnMap.get(outputColumnKey).add(result.getClient());
				
				} else if("urn:ohmage:context:timestamp".equals(outputColumnKey)){
					
					columnMap.get(outputColumnKey).add(result.getTimestamp());
				
				} else if("urn:ohmage:context:timezone".equals(outputColumnKey)) {
					
					columnMap.get(outputColumnKey).add(result.getTimezone());
					
				} else if("urn:ohmage:context:utc_timestamp".equals(outputColumnKey)) {
				
					columnMap.get(outputColumnKey).add(SurveyResponseReadWriterUtils.generateUtcTimestamp(result));
				
			    } else if("urn:ohmage:context:launch_context_long".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getLaunchContext() == null ? null : new JSONObject(result.getLaunchContext()));
			    	
			    } else if("urn:ohmage:context:launch_context_short".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getLaunchContext() == null ? null : SurveyResponseReadWriterUtils.shortLaunchContext(result.getLaunchContext()));
			    	
			    } else if("urn:ohmage:context:location:status".equals(outputColumnKey)) {
			    
			    	columnMap.get(outputColumnKey).add(result.getLocationStatus());
			    
			    } else if("urn:ohmage:context:location:latitude".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("latitude"))) {
			    			columnMap.get(outputColumnKey).add(location.optDouble("latitude"));
						} else {
							columnMap.get(outputColumnKey).add(JSONObject.NULL);
						}
			    		
			    	} else {
						columnMap.get(outputColumnKey).add(JSONObject.NULL);
					}
			    
			    } else if("urn:ohmage:context:location:longitude".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("longitude"))) {
			    			columnMap.get(outputColumnKey).add(location.optDouble("longitude"));
						} else {
							columnMap.get(outputColumnKey).add(JSONObject.NULL);
						}
			    	} else {
						columnMap.get(outputColumnKey).add(JSONObject.NULL);
					}
			    	
			    } else if("urn:ohmage:context:location:timestamp".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) { 
				    	JSONObject location = new JSONObject(result.getLocation());
				    	
				    	if(! "".equals(location.optString("timestamp")) ) { 
				    		columnMap.get(outputColumnKey).add(location.optString("timestamp"));
				    	} else {
				    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
				    	}
			    	} else {
			    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
			    	}
			    	
			    } else if("urn:ohmage:context:location:accuracy".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) {
			    		
				    	JSONObject location = new JSONObject(result.getLocation());
				    	
			    		if(! Double.isNaN(location.optDouble("accuracy"))) {
			    			columnMap.get(outputColumnKey).add(location.optDouble("accuracy"));
						} else {
							columnMap.get(outputColumnKey).add(JSONObject.NULL);
						}
			    	} else {
						columnMap.get(outputColumnKey).add(JSONObject.NULL);
					}
			    	
			    } else if("urn:ohmage:context:location:provider".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) { 
				    	JSONObject location = new JSONObject(result.getLocation());
				    	
				    	if(! "".equals(location.optString("provider")) ) { 
				    		columnMap.get(outputColumnKey).add(location.optString("provider"));
				    	} else {
				    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
				    	}
			    	} else {
			    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
			    	}
			    	
			    } else if ("urn:ohmage:survey:id".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getSurveyId());
			    	
			    } else if("urn:ohmage:survey:title".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getSurveyTitle());
			    
			    } else if("urn:ohmage:survey:description".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getSurveyDescription());
			    	
			    } else if("urn:ohmage:survey:privacy_state".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getPrivacyState());
			    	
			    } else if("urn:ohmage:repeatable_set:id".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getRepeatableSetId() == null ? JSONObject.NULL : result.getRepeatableSetId());
			    
			    } else if("urn:ohmage:repeatable_set:iteration".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getRepeatableSetIteration() == null ? JSONObject.NULL : result.getRepeatableSetIteration());
			    
				} else if (outputColumnKey.startsWith("urn:ohmage:prompt:id:")) {
					
					columnMap.get(outputColumnKey).add(result.getPromptResponseMap().get(outputColumnKey.substring("urn:ohmage:prompt:id:".length())));
				}
			}
		}
				
		// Build the output
		
		// Build metadata section
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("campaign_urn", request.getCampaignUrn());
		metadata.put("number_of_prompts", numberOfPrompts);
		metadata.put("number_of_surveys", numberOfSurveys);
		JSONArray items = new JSONArray();
		for(String key : outputColumns) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		
		JSONArray data = new JSONArray();
		main.put("data", data);

		
		Iterator<String> columnMapIterator = columnMap.keySet().iterator();
		while(columnMapIterator.hasNext()) {
			String key = columnMapIterator.next();
			
			if(key.contains("prompt:id")) {
				
				String promptId = key.substring("urn:ohmage:prompt:id:".length());
				
				Map<String, PromptProperty> choiceGlossary = choiceGlossaryMap.get(promptId);
				PromptResponseMetadata promptResponseMetadata = promptResponseMetadataMap.get(promptId);
				List<CustomChoiceItem> customChoiceItemList = (null == uniqueCustomChoiceMap ? null : uniqueCustomChoiceMap.get(promptId));  
				
				JSONObject column = new JSONObject();
				JSONObject context = new JSONObject();
				
				context.put("unit", null == promptResponseMetadata.getUnit() ? JSONObject.NULL : promptResponseMetadata.getUnit());
				context.put("prompt_type", promptResponseMetadata.getPromptType());
				context.put("display_type", promptResponseMetadata.getDisplayType());
				context.put("display_label", promptResponseMetadata.getDisplayLabel());
				context.put("text", promptResponseMetadata.getPromptText());
				
				if(null != choiceGlossary) {
					JSONObject choiceGlossaryObject = new JSONObject();
					Iterator<String> choiceGlossaryIterator = choiceGlossary.keySet().iterator();
					while(choiceGlossaryIterator.hasNext()) {
						PromptProperty pp = choiceGlossary.get(choiceGlossaryIterator.next());
						JSONObject choice = new JSONObject();
						choice.put("value", pp.getValue());
						choice.put("label", pp.getLabel());
						choiceGlossaryObject.put(pp.getKey(), choice);
					}
					context.put("choice_glossary", choiceGlossaryObject);
				} 
				else if(null != customChoiceItemList) {
					JSONObject choiceGlossaryObject = new JSONObject();
					for(CustomChoiceItem cci : customChoiceItemList) {
						JSONObject choice = new JSONObject();
						choice.put("label", cci.getLabel());
						choice.put("type", cci.getType());
						choiceGlossaryObject.put(String.valueOf(cci.getId()), choice);
					}
					context.put("choice_glossary", choiceGlossaryObject);
				}
				else {
					context.put("choice_glossary", JSONObject.NULL);
				}
				
				column.put("context", context);
				column.put("values", columnMap.get(key));
				JSONObject labelledColumn = new JSONObject();
				labelledColumn.put(key, column);
				data.put(labelledColumn);
				
			} else {
				
				JSONObject column = new JSONObject();
				column.put("values", columnMap.get(key));
				JSONObject labelledColumn = new JSONObject();
				labelledColumn.put(key, column);
				data.put(labelledColumn);
			}
		}
				
		return request.getPrettyPrint() ? main.toString(4) : main.toString();


	}
	
	/**
	 * 
	 * @param request
	 * @param outputColumns
	 * @return
	 */
	public static String generateZeroResultJsonColumnOutput(SurveyResponseReadRequest request, List<String> outputColumns) 
		throws JSONException {
		
		LOGGER.info("Generating zero-result column-based JSON output");
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("number_of_prompts", 0);
		metadata.put("number_of_surveys", 0);
		metadata.put("campaign_urn", request.getCampaignUrn());
		JSONArray items = new JSONArray();
		for(String key : outputColumns) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		JSONArray data = new JSONArray();
		main.put("data", data);
		
		return request.getPrettyPrint() ? main.toString(4) : main.toString();
	}
	
	/**
	 * 
	 * @param request
	 * @param numberOfSurveys
	 * @param numberOfPrompts
	 * @param indexedResultList
	 * @param outputColumns
	 * @param uniqueCustomChoiceMap
	 * @return
	 * @throws JSONException
	 */
	public static String generateMultiResultCsvOutput(SurveyResponseReadRequest request, 
	         				                          int numberOfSurveys,
		                                              int numberOfPrompts,
		                                              List<SurveyResponseReadIndexedResult> indexedResultList,
		                                              List<String> outputColumns,
		                                              Map<String, List<CustomChoiceItem>> uniqueCustomChoiceMap) throws JSONException {
		
		LOGGER.info("Generating multi-result CSV output");
		
		Map<String, Map<String, PromptProperty>> choiceGlossaryMap = new HashMap<String, Map<String, PromptProperty>>();
		Map<String, PromptResponseMetadata> promptResponseMetadataMap = new HashMap<String, PromptResponseMetadata>();
		
		for(SurveyResponseReadIndexedResult result : indexedResultList) {
			if(result.getChoiceGlossaryMap() != null) {
				Iterator<String> iterator = result.getChoiceGlossaryMap().keySet().iterator();
				while(iterator.hasNext()) {
					String key = iterator.next();
					if(! choiceGlossaryMap.containsKey(key)) {
						choiceGlossaryMap.put(key, result.getChoiceGlossaryMap().get(key));
					}
				}
			}
			Iterator<String> iterator = result.getPromptResponseMetadataMap().keySet().iterator();
			while(iterator.hasNext()) {
				String key = iterator.next();
				if(! promptResponseMetadataMap.containsKey(key)) {
					promptResponseMetadataMap.put(key, result.getPromptResponseMetadataMap().get(key));
				}
			}
		}
		
		StringBuilder builder = new StringBuilder();
		
		if(! request.getSuppressMetadata()) {
			builder.append("## begin metadata").append(NEWLINE);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("campaign_urn", request.getCampaignUrn());
			jsonObject.put("number_of_prompts", numberOfPrompts);
			jsonObject.put("number_of_surveys", numberOfSurveys);
			
			builder.append("#" + jsonObject.toString().replace(",", ";")).append(NEWLINE)	
			       .append("## end metadata").append(NEWLINE).append("# begin prompt contexts").append(NEWLINE);
			
			Iterator<String> promptResponseMetadataIterator = promptResponseMetadataMap.keySet().iterator();
			
			while(promptResponseMetadataIterator.hasNext()) {
				String key = promptResponseMetadataIterator.next();
				
				JSONObject prompt = new JSONObject();
				JSONObject context = new JSONObject();
				
				context.put("unit", promptResponseMetadataMap.get(key).getUnit() == null ?  JSONObject.NULL : promptResponseMetadataMap.get(key).getUnit());
				context.put("prompt_type", promptResponseMetadataMap.get(key).getPromptType());
				context.put("display_type", promptResponseMetadataMap.get(key).getDisplayType());
				context.put("display_label", promptResponseMetadataMap.get(key).getDisplayLabel());
				context.put("text", promptResponseMetadataMap.get(key).getPromptText());
				if(null != choiceGlossaryMap.get(key)) {	
					context.put("choice_glossary", SurveyResponseReadWriterUtils.choiceGlossaryToJson(choiceGlossaryMap.get(key)));
				} 
				else if(null != uniqueCustomChoiceMap && uniqueCustomChoiceMap.containsKey(key)) {
					JSONObject main = new JSONObject();
					List<CustomChoiceItem> customChoiceItemList = uniqueCustomChoiceMap.get(key);
					for(CustomChoiceItem cci : customChoiceItemList) {
						JSONObject item = new JSONObject();
						item.put("label", cci.getLabel());
						item.put("type", cci.getType());
						main.put(String.valueOf(cci.getId()), item);
					} 
					context.put("choice_glossary", main);
				}
				else {
					context.put("choice_glossary", JSONObject.NULL);
				}
				prompt.put(key, context); 
				builder.append("#" + prompt.toString().replace(",", ";")).append(NEWLINE);
			}
			builder.append("## end prompt contexts").append(NEWLINE).append("## begin data").append(NEWLINE);
		}
		
		
		// Sort the column map key set for predictable columnar output.
		// In the future, there can be a column_order parameter to the API 
		// For now, canonicalize into 'ohmage order': user, datetime, survey id, repeatable set id, repeatable set iteration,
		// prompt id, and then 'context' columns and survey title and description in alpha order
		
		// Note that not all columns are necessarily present. This is based on the column_list parameter passed to the API.
	
		TreeSet<String> sortedColumnMapKeySet = new TreeSet<String>(outputColumns);
		ArrayList<String> canonicalColumnList = new ArrayList<String>();
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:user:id")) {
			canonicalColumnList.add("urn:ohmage:user:id");
			sortedColumnMapKeySet.remove("urn:ohmage:user:id");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:context:timestamp")) {
			canonicalColumnList.add("urn:ohmage:context:timestamp");
			sortedColumnMapKeySet.remove("urn:ohmage:context:timestamp");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:context:utc_timestamp")) {
			canonicalColumnList.add("urn:ohmage:context:utc_timestamp");
			sortedColumnMapKeySet.remove("urn:ohmage:context:utc_timestamp");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:survey:id")) {
			canonicalColumnList.add("urn:ohmage:survey:id");
			sortedColumnMapKeySet.remove("urn:ohmage:survey:id");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:repeatable_set:id")) {
			canonicalColumnList.add("urn:ohmage:repeatable_set:id");
			sortedColumnMapKeySet.remove("urn:ohmage:repeatable_set:id");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:repeatable_set:iteration")) {
			canonicalColumnList.add("urn:ohmage:repeatable_set:iteration");
			sortedColumnMapKeySet.remove("urn:ohmage:repeatable_set:iteration");
		}
		
		// Group all of the prompts in the order in which they appear in the survey XML
		Configuration configuration = request.getConfiguration();
		Map<String, List<Object>> surveyIdToPromptIdListMap = new HashMap<String, List<Object>>();
		
		Iterator<String> sortedSetIterator = sortedColumnMapKeySet.iterator();
		while(sortedSetIterator.hasNext()) {
			String columnName = sortedSetIterator.next();
			if(columnName.contains("prompt:id")) {
				String promptId = columnName.substring(columnName.lastIndexOf(":") + 1);
				String surveyId = configuration.getSurveyIdForPromptId(promptId);
				
				if(! surveyIdToPromptIdListMap.containsKey(surveyId)) {
					
					List<Object> promptIdList = new ArrayList<Object>(); // _logger.info(configuration.getNumberOfPromptsInSurvey(surveyId));
					promptIdList.addAll(Collections.nCopies(configuration.getNumberOfPromptsInSurvey(surveyId), null));
					promptIdList.set(configuration.getIndexForPrompt(surveyId, promptId), columnName);
					surveyIdToPromptIdListMap.put(surveyId, promptIdList);
					
				} else {
					
					surveyIdToPromptIdListMap.get(surveyId).set(configuration.getIndexForPrompt(surveyId, promptId), columnName);
				}
				
				sortedSetIterator.remove();
			}
		}
		
		//_logger.info(surveyIdToPromptIdListMap);
		
		Iterator<String> surveyIdToPromptIdListMapIterator = surveyIdToPromptIdListMap.keySet().iterator();
		while(surveyIdToPromptIdListMapIterator.hasNext()) {
			List<Object> promptIdList = surveyIdToPromptIdListMap.get(surveyIdToPromptIdListMapIterator.next());
			for(Object promptId : promptIdList) {
				canonicalColumnList.add((String) promptId);
			}
		}
		
		canonicalColumnList.addAll(sortedColumnMapKeySet);
		sortedColumnMapKeySet.clear();
		
		// Build the column headers
		// For the CSV output, user advocates have requested that the column names be made shorter and that single_choice
		// prompts have both their label and value present in the output
		List<String> copyOfCanonicalColumnList = new ArrayList<String> (canonicalColumnList);
		
		int s = copyOfCanonicalColumnList.size();
		int i = 0;
		for(String key : copyOfCanonicalColumnList) {
			String shortHeader = null;
			if(key.startsWith("urn:ohmage:context")) {
				shortHeader = key.replace("urn:ohmage:context", "sys");				
			} else if(key.startsWith("urn:ohmage:prompt:id")) {
				
				String internalPromptId = key.substring("urn:ohmage:prompt:id:".length());
				String type = promptResponseMetadataMap.get(internalPromptId).getPromptType();
				
				if("single_choice".equals(type)) {
					Configuration config = request.getConfiguration();
					if(config.promptContainsSingleChoiceValues(internalPromptId)) {
						shortHeader = internalPromptId + ":label," + internalPromptId + ":value";
					} 
					else {
						shortHeader = internalPromptId + ":label";
					}
				} 
				else {
					shortHeader = key.replace("urn:ohmage:prompt:id:", "");
				}
				
			} else if(key.startsWith("urn:ohmage")) {
				shortHeader = key.replace("urn:ohmage:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(NEWLINE);
		
		for(SurveyResponseReadIndexedResult result : indexedResultList) {
			
			for(String canonicalColumnId : canonicalColumnList) {
				
				if("urn:ohmage:user:id".equals(canonicalColumnId)) {
					builder.append(result.getUsername()).append(",");
				}
				else if("urn:ohmage:context:client".equals(canonicalColumnId)) {
					builder.append(result.getClient()).append(",");
				}
				else if("urn:ohmage:context:timestamp".equals(canonicalColumnId)) {
					builder.append(result.getTimestamp()).append(",");
				}
				else if("urn:ohmage:context:timezone".equals(canonicalColumnId)) {
					builder.append(result.getTimezone()).append(",");
				}
				else if("urn:ohmage:context:utc_timestamp".equals(canonicalColumnId)) {
					builder.append(result.getUtcTimestamp()).append(",");
				}
				else if("urn:ohmage:context:launch_context_short".equals(canonicalColumnId)) {
					if(null != result.getLaunchContext()) {
						builder.append(SurveyResponseReadWriterUtils.shortLaunchContext(result.getLaunchContext().replace(",", ";"))).append(",");
					} else {
						builder.append("null,");
					}
				}
				else if("urn:ohmage:context:launch_context_long".equals(canonicalColumnId)) {
					if(null != result.getLaunchContext()) {
						builder.append(result.getLaunchContext().replace(",", ";")).append(",");
					} else {
						builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:status".equals(canonicalColumnId)) {
					builder.append(result.getLocationStatus()).append(",");
				}
				else if("urn:ohmage:context:location:latitude".equals(canonicalColumnId)) {
					
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("latitude"))) {
			    			builder.append(location.optDouble("latitude")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:longitude".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("longitude"))) {
			    			builder.append(location.optDouble("longitude")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:timestamp".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("timestamp"))) {
			    			builder.append(location.optDouble("timestamp")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:accuracy".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("accuracy"))) {
			    			builder.append(location.optDouble("accuracy")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:provider".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("provider"))) {
			    			builder.append(location.optDouble("provider")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:repeatable_set:id".equals(canonicalColumnId)) {
					builder.append(result.getRepeatableSetId()).append(",");
				}
				else if("urn:ohmage:repeatable_set:iteration".equals(canonicalColumnId)) {
					builder.append(result.getRepeatableSetIteration()).append(",");
				}
				else if("urn:ohmage:repeatable_set:iteration".equals(canonicalColumnId)) {
					builder.append(result.getRepeatableSetIteration()).append(",");
				}
				else if("urn:ohmage:survey:id".equals(canonicalColumnId)) {
					builder.append(result.getSurveyId()).append(",");
				}
				else if("urn:ohmage:survey:title".equals(canonicalColumnId)) {
					builder.append(cleanAndQuoteString(result.getSurveyTitle())).append(",");
				}
				else if("urn:ohmage:survey:description".equals(canonicalColumnId)) {
					builder.append(cleanAndQuoteString(result.getSurveyDescription())).append(",");
				}
				else if("urn:ohmage:survey:privacy_state".equals(canonicalColumnId)) {
					builder.append(result.getPrivacyState()).append(",");
				}
				else if(canonicalColumnId.contains("prompt:id")) { 
					
					String promptId = canonicalColumnId.substring("urn:ohmage:prompt:id:".length());
					
					PromptResponseMetadata promptResponseMetadata = promptResponseMetadataMap.get(promptId);
					Map<String, Object> promptResponseMap = result.getPromptResponseMap();
					
					if("single_choice".equals(promptResponseMetadata.getPromptType())) {
						
						SingleChoicePromptValueAndLabel valueLabel = (SingleChoicePromptValueAndLabel) promptResponseMap.get(promptId);
						
						if(null != valueLabel) {
							if(null != valueLabel.getValue()) {
								builder.append(valueLabel.getLabel()).append(",").append(valueLabel.getValue());
							} 
							else {
								builder.append(valueLabel.getLabel());
							}
						} 
						else {
							builder.append("null");
							if(configuration.promptContainsSingleChoiceValues(promptId)) {
								builder.append(",null");
							}
						}
					}
					else {
						
						Object value = promptResponseMap.get(promptId);
						
						if(value instanceof JSONObject) { // single_choice_custom, multi_choice_custom
							builder.append(((JSONObject) value).toString().replace(",", ";"));
						} 
						
						else if(value instanceof JSONArray) { // multi_choice
							builder.append(((JSONArray) value).toString().replace(",", ";"));
						}
						
						else if (value instanceof String) { // clean up text for easier input into spreadsheets 
							String string = (String) value;
							
							if("text".equals(promptResponseMetadataMap.get(promptId).getPromptType())) {
								builder.append(cleanAndQuoteString(string));
							} else {
								builder.append(string);
							}
						}					
						else {
							builder.append(value);
						}
					}
					
					builder.append(",");
				}
				
			}
			int lastCommaIndex = builder.lastIndexOf(",");
			builder.replace(lastCommaIndex, lastCommaIndex + 1, "");
			builder.append(NEWLINE);
		}
		
		if(! request.getSuppressMetadata()) {
			builder.append("## end data").append(NEWLINE);
		}
		
		return builder.toString();
		
	}
	
	/**
	 * 
	 * @param request
	 * @param outputColumns
	 * @return
	 */
	public static String generateZeroResultCsvOutput(SurveyResponseReadRequest request, List<String> outputColumns) 
		throws JSONException {
		
		LOGGER.info("Generating zero-result CSV output");
		
		StringBuilder builder = new StringBuilder();
		
		if(! request.getSuppressMetadata()) {
			builder.append("## begin metadata").append(NEWLINE);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("number_of_prompts", 0);
			jsonObject.put("number_of_surveys", 0);
			jsonObject.put("campaign_urn", request.getCampaignUrn());
			builder.append(jsonObject.toString().replace(",", ";")).append(NEWLINE)
			       .append("## end metadata").append(NEWLINE)
			       .append("## begin prompt contexts").append(NEWLINE).append("# end prompt contexts").append(NEWLINE)
			       .append("## begin data").append(NEWLINE);
		}
		
		int s = outputColumns.size();
		
		// Logic that is completely redundant with the method above
		int i = 0;
		for(String key : outputColumns) {
			String shortHeader = null;
			if(key.startsWith("urn:ohmage:context")) {
				shortHeader = key.replace("urn:ohmage:context", "sys");				
			} else if(key.startsWith("urn:ohmage:prompt:id")) {
				shortHeader = key.replace("urn:ohmage:prompt:id:", "");
			} else if(key.startsWith("urn:ohmage")) {
				shortHeader = key.replace("urn:ohmage:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(NEWLINE);
		
		if(! request.getSuppressMetadata()) {
			builder.append("## end data").append(NEWLINE);
		}
		
		return builder.toString();
	}
	
	/**
	 * FIXME: move to StringUtils 
	 * @param string
	 * @return
	 */
	private static String cleanAndQuoteString(String string) {
		if(null != string) {
			return "\"" + string.trim().replaceAll("\\s", " ").replaceAll("\"", "'") + "\"";
		}
		return null;
	}
}
