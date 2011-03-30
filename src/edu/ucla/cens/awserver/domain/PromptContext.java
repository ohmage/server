package edu.ucla.cens.awserver.domain;

import java.util.Map;


/**
 * Bag of prompt info for data point/export API.
 * 
 * @author selsky
 */
public class PromptContext {
	
	private String _id;
	private String _type;
	private String _displayLabel;
	private String _displayType;
	private String _unit;
	private Map<String, PromptProperty> _choiceGlossary; // if single_choice or multi_choice
	
	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	public String getDisplayLabel() {
		return _displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		_displayLabel = displayLabel;
	}

	public String getDisplayType() {
		return _displayType;
	}

	public void setDisplayType(String displayType) {
		_displayType = displayType;
	}

	public String getUnit() {
		return _unit;
	}

	public void setUnit(String unit) {
		_unit = unit;
	}
	
	public Map<String, PromptProperty> getChoiceGlossary() {
		return _choiceGlossary;
	}

	public void setChoiceGlossary(Map<String, PromptProperty> choiceGlossary) {
		_choiceGlossary = choiceGlossary;
	}

	@Override
	public String toString() {
		return "PromptContext [_choiceGlossary=" + _choiceGlossary
				+ ", _displayLabel=" + _displayLabel + ", _displayType="
				+ _displayType + ", _id=" + _id + ", _type=" + _type
				+ ", _unit=" + _unit + "]";
	}
}
