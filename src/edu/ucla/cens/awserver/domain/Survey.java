package edu.ucla.cens.awserver.domain;

import java.util.Collections;
import java.util.Map;

/**
 * A minified immutable survey generated from configuration XML: only the properties necessary for data validation are present in  
 * this class (so, no message nodes, descriptions, UI elements, etc). 
 * 
 * @author selsky
 */
public class Survey extends AbstractSurveyItem {
	private Map<String, SurveyItem> _surveyItemMap; // prompts and repeatableSets
	
	public Survey(String surveyId, Map<String, SurveyItem> surveyMap) {
		super(surveyId);
		_surveyItemMap = surveyMap; // TODO really need a deep copy here, but so far the creator of this Map does not change it
	}

	public Map<String, SurveyItem> getSurveyItemMap() {
		return Collections.unmodifiableMap(_surveyItemMap);
	}

	@Override
	public String toString() {
		return "Survey [_surveyMap=" + _surveyItemMap + ", getId()=" + getId()
				+ "]";
	}
}
