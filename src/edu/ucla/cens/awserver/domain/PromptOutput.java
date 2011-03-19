package edu.ucla.cens.awserver.domain;

/**
 * Bag of prompt info for data point/export API.
 * 
 * @author selsky
 */
public class PromptOutput {
	
	private String _id;
	private String _type;
	private String _displayLabel;
	private String _displayType;
	private Object _displayValue;
	private String _unit;
	
	private Object _response; // unneeded?

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

	public Object getDisplayValue() {
		return _displayValue;
	}

	public void setDisplayValue(Object displayValue) {
		_displayValue = displayValue;
	}

	public String getUnit() {
		return _unit;
	}

	public void setUnit(String unit) {
		_unit = unit;
	}

	public Object getResponse() {
		return _response;
	}

	public void setResponse(Object response) {
		_response = response;
	}
	
}
