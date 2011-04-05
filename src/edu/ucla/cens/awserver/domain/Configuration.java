package edu.ucla.cens.awserver.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Immutable bean-style wrapper for accessing and validating campaign properties.
 * 
 * @author selsky
 */
public class Configuration {
	// private static Logger _logger = Logger.getLogger(Configuration.class);
	private String _urn;
	private String _name;
	private String _description;
	private String _runningState;
	private String _privacyState;
	private String _creationTimestamp;
	private Map<String, Survey> _surveyMap;
	private String _xml;
	
	public Configuration(String urn, String name, String description, String runningState, String privacyState, 
			String creationTimestamp, Map<String, Survey> surveyMap, String xml) {
		
		// If any of these properties are missing, it means the db is in an inconsistent state!
		if(StringUtils.isEmptyOrWhitespaceOnly(urn)) {
			throw new IllegalArgumentException("a URN is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("a name is required");
		}
		// a null or empty description is allowed
		if(StringUtils.isEmptyOrWhitespaceOnly(runningState)) {
			throw new IllegalArgumentException("running state is required");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
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
		
		_urn = urn;
		_name = name;
		_description = description;
		_runningState = runningState;
		_privacyState = privacyState;
		_creationTimestamp = creationTimestamp;
		_surveyMap = surveyMap; // TODO deep copy?
		_xml = xml;
	}
	
	public String getUrn() {
		return _urn;
	}

	public Map<String, Survey> getSurveys() {
		return Collections.unmodifiableMap(_surveyMap);
	}
	
	public String getXml() {
		return _xml;
	}

	public String getName() {
		return _name;
	}
	
	public String getDescription() {
		return _description;
	}

	public String getRunningState() {
		return _runningState;
	}

	public String getPrivacyState() {
		return _privacyState;
	}

	public String getCreationTimestamp() {
		return _creationTimestamp;
	}

	public boolean surveyIdExists(String surveyId) {
		return _surveyMap.containsKey(surveyId);
	}
	
	public String getSurveyTitleFor(String surveyId) {
		if(surveyIdExists(surveyId)) {
			return _surveyMap.get(surveyId).getTitle();
		}
		return null;
	}
	
	public String getSurveyDescriptionFor(String surveyId) {
		if(surveyIdExists(surveyId)) {
			return _surveyMap.get(surveyId).getDescription();
		}
		return null;
	}
	
	public boolean repeatableSetExists(String surveyId, String repeatableSetId) {
        if(! _surveyMap.get(surveyId).getSurveyItemMap().containsKey(repeatableSetId)) {
        	return false;
        } 
		SurveyItem si = _surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId);
        return si instanceof RepeatableSet;
	}

	public boolean promptExists(String surveyId, String promptId) {
        return _surveyMap.get(surveyId).getSurveyItemMap().containsKey(promptId);
	}
	
	public boolean promptExists(String surveyId, String repeatableSetId, String promptId) {
        return ((RepeatableSet)_surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().containsKey(promptId);
	}
	
	public boolean isPromptSkippable(String surveyId, String promptId) {
        return ((Prompt) _surveyMap.get(surveyId).getSurveyItemMap().get(promptId)).isSkippable();
	}
	
	public boolean isPromptSkippable(String surveyId, String repeatableSetId, String promptId) {
        return ((RepeatableSet)_surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId).isSkippable();
	}

	public String getPromptType(String surveyId, String promptId) {
		return ((Prompt)_surveyMap.get(surveyId).getSurveyItemMap().get(promptId)).getType();
	}

	public String getPromptType(String surveyId, String repeatableSetId, String promptId) {
		return ((RepeatableSet)_surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId).getType();
	}

	public Prompt getPrompt(String surveyId, String promptId) {
		return ((Prompt)_surveyMap.get(surveyId).getSurveyItemMap().get(promptId)); 
	}

	public Prompt getPrompt(String surveyId, String repeatableSetId, String promptId) {
		return ((RepeatableSet)_surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId)).getPromptMap().get(promptId); 
	}

	/**
	 * Returns the number of prompts in the repeatable set inside the survey represented by survey id. Assumes that surveyId and
	 * repeatableSetId are valid. 
	 */
	public int numberOfPromptsInRepeatableSet(String surveyId, String repeatableSetId) {
		SurveyItem si = _surveyMap.get(surveyId).getSurveyItemMap().get(repeatableSetId);
        return ((RepeatableSet) si).getPromptMap().size();
	}
	
	/**
	 * Returns the id of the survey that the provided promptId belongs to. The specification calls for all ids in a configuration
	 * to be unique. 
	 */
	public String getSurveyIdForPromptId(String promptId) {
		Set<String> keys = _surveyMap.keySet();
		for(String key : keys) { 
			Survey s = _surveyMap.get(key);
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
		Survey survey = _surveyMap.get(getSurveyIdForPromptId(promptId));
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
		if(null != props) {
			if(props.containsKey(key)) {
				return props.get(key).getValue();
			}
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
		if(null != props) {
			if(props.containsKey(key)) {
				return props.get(key).getLabel();
			}
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
