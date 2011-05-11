package edu.ucla.cens.awserver.domain;

import java.util.Collections;
import java.util.Map;

/**
 * A survey prompt configuration.
 * 
 * @author selsky
 */
public class Prompt extends AbstractSurveyItem {
	private String _displayType;
	private String _type;
	private Map<String, PromptProperty> _properties;
	private boolean _skippable;
	private String _displayLabel;
	private String _unit;
	private String _text;

	public Prompt(String id, String displayType, String type, Map<String, PromptProperty> props, boolean skippable, 
			String displayLabel, String unit, String text) {
		
		super(id);
		_displayType = displayType;
		_type = type;
		_properties = props; // TODO really need a deep copy here
		_skippable = skippable;
		_displayLabel = displayLabel;
		_unit = unit;
		_text = text;
	}

	public String getText() {
		return _text;
	}
	
	public String getDisplayType() {
		return _displayType;
	}

	public String getType() {
		return _type;
	}
	
	public String getUnit() {
		return _unit;
	}

	public String getDisplayLabel() {
		return _displayLabel;
	}
	
	public Map<String, PromptProperty> getProperties() {
		return Collections.unmodifiableMap(_properties);
	}

	public boolean isSkippable() {
		return _skippable;
	}

	@Override
	public String toString() {
		return "Prompt [_displayType=" + _displayType + ", _type=" + _type
				+ ", _properties=" + _properties + ", _skippable=" + _skippable
				+ ", _displayLabel=" + _displayLabel + ", _unit=" + _unit
				+ ", _text=" + _text + "]";
	}	
}
