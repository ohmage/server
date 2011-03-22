package edu.ucla.cens.awserver.domain;

import java.util.Collections;
import java.util.Map;

/**
 * A minified immutable survey generated from configuration XML. 
 * 
 * @author selsky
 */
public class Survey extends AbstractSurveyItem {
	private Map<String, SurveyItem> _surveyItemMap; // prompts and repeatableSets
	private String _title;
	private String _description;
	
	public Survey(String surveyId, String title, String description, Map<String, SurveyItem> surveyMap) {
		super(surveyId);
		_surveyItemMap = surveyMap; // TODO really need a deep copy here, but so far the creator of this Map does not change it
		_title = title;
		_description = description;
	}

	public Map<String, SurveyItem> getSurveyItemMap() {
		return Collections.unmodifiableMap(_surveyItemMap);
	}
	
	public String getTitle() {
		return _title;
	}
	
	public String getDescription() {
		return _description;
	}

	@Override
	public String toString() {
		return "Survey [_description=" + _description + ", _surveyItemMap="
				+ _surveyItemMap + ", _title=" + _title + "]";
	}
}
