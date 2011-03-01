package edu.ucla.cens.awserver.domain;

import java.util.Map;

/**
 * Builds a Map representation for a String representation of a configuration.
 * 
 * @author selsky
 */
public interface SurveyMapBuilder {
	
	Map<String, Survey> buildFrom(String string);
	
}
