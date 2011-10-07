/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.util.StringUtils;


/**
 * Immutable bean-style wrapper for accessing and validating campaign 
 * configuration properties. The methods in this class assume that the caller
 * has validated that the parameters (i.e., prompt ids, survey ids, repeatable
 * set ids) are valid for a particular configuration instance.
 * 
 * @author Joshua Selsky
 */
public class Configuration {
	//private static Logger _logger = Logger.getLogger(Configuration.class);
	private String urn;
	private String name;
	private String description;
	private CampaignRunningStateCache.RunningState runningState;
	private CampaignPrivacyStateCache.PrivacyState privacyState;
	private String creationTimestamp;
	private Map<String, Survey> surveyMap;
	private String xml;
	
	/**
	 * Creates a configuration from XML and db properties.
	 * 
	 * @param urn  The campaign URN.
	 * @param name  The campaign name.
	 * @param description  The campaign description.
	 * @param runningState  The campaign's running state
	 * @param privacyState  The campaign's privacy state
	 * @param creationTimestamp  The campaign's creation timestamp.
	 * @param surveyMap  A Map of the surveys contained by the campaign.
	 * @param xml  The raw campaign XML.
	 */
	public Configuration(String urn, String name, String description, 
			CampaignRunningStateCache.RunningState runningState, 
			CampaignPrivacyStateCache.PrivacyState privacyState, 
			String creationTimestamp, Map<String, Survey> surveyMap, String xml) {
		
		// If any of these properties are missing, it means the db is in an inconsistent state!
		if(StringUtils.isEmptyOrWhitespaceOnly(urn)) {
			throw new IllegalArgumentException("a URN is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("a name is required");
		}
		// a null or empty description is allowed
		if(runningState == null) {
			throw new IllegalArgumentException("running state is required");
		}
		if(privacyState == null) {
			throw new IllegalArgumentException("privacy state is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(creationTimestamp)) {
			throw new IllegalArgumentException("a creation timestamp is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(xml)) {
			throw new IllegalArgumentException("xml is required");
		}
		if(null == surveyMap || surveyMap.isEmpty()) {
			throw new IllegalArgumentException("a map of surveys is required");
		}
		
		this.urn = urn;
		this.name = name;
		this.description = description;
		this.runningState = runningState;
		this.privacyState = privacyState;
		this.creationTimestamp = creationTimestamp;
		this.surveyMap = surveyMap; // TODO deep copy?
		this.xml = xml;
	}
	
	public String getUrn() {
		return urn;
	}

	public Map<String, Survey> getSurveys() {
		return Collections.unmodifiableMap(surveyMap);
	}
	
	public String getXml() {
		return xml;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public CampaignRunningStateCache.RunningState getRunningState() {
		return runningState;
	}

	public CampaignPrivacyStateCache.PrivacyState getPrivacyState() {
		return privacyState;
	}

	public String getCreationTimestamp() {
		return creationTimestamp;
	}

	public boolean surveyIdExists(String surveyId) {
		return surveyMap.containsKey(surveyId);
	}
	
	public String getSurveyTitleFor(String surveyId) {
		if(surveyIdExists(surveyId)) {
			return surveyMap.get(surveyId).getTitle();
		}
		return null;
	}
	
	public String getSurveyDescriptionFor(String surveyId) {
		if(surveyIdExists(surveyId)) {
			return surveyMap.get(surveyId).getDescription();
		}
		return null;
	}
	
	public boolean repeatableSetExists(String surveyId, String repeatableSetId) {
        if(! surveyMap.get(surveyId).getSurveyItemMap().containsKey(repeatableSetId)) {
        	return false;
        } 
		SurveyItem si = surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId);
        return si instanceof RepeatableSet;
	}

	public boolean promptExists(String surveyId, String promptId) {
        return surveyMap.get(surveyId).getSurveyItemMap().containsKey(promptId);
	}
	
	public boolean promptExists(String surveyId, String repeatableSetId, String promptId) {
        return ((RepeatableSet)surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().containsKey(promptId);
	}
	
	public boolean isPromptSkippable(String surveyId, String promptId) {
        return ((Prompt) surveyMap.get(surveyId).getSurveyItemMap().get(promptId)).isSkippable();
	}
	
	public boolean isPromptSkippable(String surveyId, String repeatableSetId, String promptId) {
        return ((RepeatableSet)surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId).isSkippable();
	}

	public String getPromptType(String surveyId, String promptId) {
		return ((Prompt)surveyMap.get(surveyId).getSurveyItemMap().get(promptId)).getType();
	}

	public String getPromptType(String surveyId, String repeatableSetId, String promptId) {
		return ((RepeatableSet)surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId).getType();
	}

	public Prompt getPrompt(String surveyId, String promptId) {
		return ((Prompt)surveyMap.get(surveyId).getSurveyItemMap().get(promptId)); 
	}

	public Prompt getPrompt(String surveyId, String repeatableSetId, String promptId) {
		return ((RepeatableSet)surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId); 
	}

	public int getIndexForPrompt(String surveyId, String promptId) {
		Survey survey = surveyMap.get(surveyId);
		
		if(isPromptInRepeatableSet(surveyId, promptId)) {
			String repeatableSetId = survey.getRepeatableSetIdForPromptId(promptId);
			return ((RepeatableSet) survey.getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId).getIndex();
		}
		
		return ((Prompt) survey.getSurveyItemMap().get(promptId)).getIndex();
	}
	
	public boolean isPromptInRepeatableSet(String surveyId, String promptId) {
		return null != surveyMap.get(surveyId).getRepeatableSetIdForPromptId(promptId);
	}
	
	public boolean promptContainsSingleChoiceValues(String promptId) {
		if(null != promptId) {
			String surveyId = getSurveyIdForPromptId(promptId);
			if(isPromptInRepeatableSet(surveyId, promptId)) {
				String repeatableSetId = surveyMap.get(surveyId).getRepeatableSetIdForPromptId(promptId);
				Prompt p = ((RepeatableSet) surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId);
				if("single_choice".equals(p.getType())) {
					Iterator<String> propertyIterator = p.getProperties().keySet().iterator();
					// If a value exists for any choice, values must exist for 
					// every choice. Our configuration validation process 
					// handles this rule.  
					return p.getProperties().get(propertyIterator.next()).getValue() != null;
				}
			} else { 
				Prompt p = ((Prompt) surveyMap.get(surveyId).getSurveyItemMap().get(promptId));
				if("single_choice".equals(p.getType())) {

					Iterator<String> propertyIterator = p.getProperties().keySet().iterator();
					// If a value exists for any choice, values must exist for 
					// every choice. Our configuration validation process 
					// handles this rule.  
					return p.getProperties().get(propertyIterator.next()).getValue() != null;
				}
			}
		}
		
		return false;
	}

	public int getNumberOfPromptsInSurvey(String surveyId) {
		return surveyMap.get(surveyId).getSurveyItemMap().keySet().size();
	}
	
	public int getNumberOfPromptsInSurvey(String surveyId, String repeatableSetId) {
		return ((RepeatableSet)surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().keySet().size();
	}
	
	/**
	 * Returns the number of prompts in the repeatable set inside the survey represented by survey id. Assumes that surveyId and
	 * repeatableSetId are valid. 
	 */
	public int numberOfPromptsInRepeatableSet(String surveyId, String repeatableSetId) {
		SurveyItem si = surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId);
        return ((RepeatableSet) si).getPromptMap().size();
	}
	
	/**
	 * Returns the id of the survey that the provided promptId belongs to. The specification calls for all ids in a configuration
	 * to be unique. 
	 */
	public String getSurveyIdForPromptId(String promptId) {
		Set<String> keys = surveyMap.keySet();
		for(String key : keys) { 
			Survey s = surveyMap.get(key);
			Map<String, SurveyItem> itemMap = s.getSurveyItemMap(); 
			Set<String> itemKeys = itemMap.keySet();
			for(String itemKey : itemKeys) {
				SurveyItem si = itemMap.get(itemKey);
				if(si instanceof RepeatableSet) {
					if(((RepeatableSet)si).getPromptMap().keySet().contains(promptId)) {
						return key;
					}
				} else {
					if(itemKey.equals(promptId)) {
						return key;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the promptIds of those prompts with a displayType of metadata for the survey that contains the provided promptId.  
	 */
	public List<String> getMetadataPromptIds(String promptId) {
		Survey survey = surveyMap.get(getSurveyIdForPromptId(promptId));
		List<String> list = new ArrayList<String>();
		if(null != survey) {
			Map<String, SurveyItem> itemMap = survey.getSurveyItemMap();
			Set<String> keys = itemMap.keySet();
			for(String key : keys) {
				SurveyItem si = itemMap.get(key);
				if(si instanceof Prompt) {
					if(((Prompt) si).getDisplayType().equals("metadata")) {
						list.add(si.getId());
					}
				} else {
					Map<String, Prompt> promptMap = ((RepeatableSet)si).getPromptMap();
					Iterator<String> rsPromptKeyIterator = promptMap.keySet().iterator();
					while(rsPromptKeyIterator.hasNext()) {
						Prompt p = promptMap.get(rsPromptKeyIterator.next()); 
						if(p.getDisplayType().equals("metadata")) {
							list.add(p.getId());
						}
					}
 				}
			}
		}
		return list;
	}

	public String getPromptTextFor(String surveyId, String promptId) {
		return getPrompt(surveyId, promptId).getText();		
	}
	
	public String getPromptTextFor(String surveyId, String repeatableSetId, String promptId) {
		return getPrompt(surveyId, repeatableSetId, promptId).getText();
	}

	public String getDisplayTypeFor(String surveyId, String promptId) {
		return getPrompt(surveyId, promptId).getDisplayType();		
	}
	
	public String getDisplayTypeFor(String surveyId, String repeatableSetId, String promptId) {
		return getPrompt(surveyId, repeatableSetId, promptId).getDisplayType();
	}

	public String getDisplayLabelFor(String surveyId, String promptId) {
		return getPrompt(surveyId, promptId).getDisplayLabel();		
	}
	
	public String getDisplayLabelFor(String surveyId, String repeatableSetId, String promptId) {
		return getPrompt(surveyId, repeatableSetId, promptId).getDisplayLabel();
	}
	
	public String getValueForChoiceKey(String surveyId, String promptId, String key) {
		return getChoiceValueFrom(getPrompt(surveyId, promptId), key);
	}
	
	public String getValueForChoiceKey(String surveyId, String repeatableSetId, String promptId, String key) {
		return getChoiceValueFrom(getPrompt(surveyId, repeatableSetId, promptId), key);
	}
	
	private String getChoiceValueFrom(Prompt prompt, String key) {
		Map<String, PromptProperty> props = prompt.getProperties();
		String value = null;
		if((null != props) && (props.containsKey(key))) {
			return props.get(key).getValue();
		}
		return value;
	}

	public String getLabelForChoiceKey(String surveyId, String promptId, String key) {
		return getChoiceLabelFrom(getPrompt(surveyId, promptId), key);
	}
	
	public String getLabelForChoiceKey(String surveyId, String repeatableSetId, String promptId, String key) {
		return getChoiceLabelFrom(getPrompt(surveyId, repeatableSetId, promptId), key);
	}
	
	private String getChoiceLabelFrom(Prompt prompt, String key) {
		Map<String, PromptProperty> props = prompt.getProperties();
		String value = null;
		if((null != props) && (props.containsKey(key))) {
			return props.get(key).getLabel();
		}
		return value;
	}
	
	public Map<String, PromptProperty> getChoiceGlossaryFor(String surveyId, String repeatableSetId, String promptId) {
		return getPrompt(surveyId, repeatableSetId, promptId).getProperties();
	}
	
	public Map<String, PromptProperty> getChoiceGlossaryFor(String surveyId, String promptId) {
		return getPrompt(surveyId, promptId).getProperties();
	}
	
	public String getUnitFor(String surveyId, String promptId) {
		return getPrompt(surveyId, promptId).getUnit();
	}
	
	public String getUnitFor(String surveyId, String repeatableSetId, String promptId) {
		return getPrompt(surveyId, repeatableSetId, promptId).getUnit();
	}
}
