package edu.ucla.cens.awserver.domain;

import java.util.Collections;
import java.util.Map;

/**
 * A repeatable set has an id and a Map of Prompts.
 * 
 * @author selsky
 */
public class RepeatableSet extends AbstractSurveyItem {
	private Map<String, Prompt> _promptMap;
	
	public RepeatableSet(String id, Map<String, Prompt> promptMap) {
		super(id);
		_promptMap = promptMap; 
	}

	public Map<String, Prompt> getPromptMap() {
		return Collections.unmodifiableMap(_promptMap);
	}

	@Override
	public String toString() {
		return "RepeatableSet [_promptMap=" + _promptMap + ", getId()="
				+ getId() + "]";
	}	
}
