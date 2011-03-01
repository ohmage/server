package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class DataPointFunctionQueryMetadata {
	private String _label;
	private String _type;
	private String _unit;
	
	public String getLabel() {
		return _label;
	}
	
	public void setLabel(String label) {
		_label = label;
	}
	
	public String getType() {
		return _type;
	}
	
	public void setType(String type) {
		_type = type;
	}
	
	public String getUnit() {
		return _unit;
	}
	
	public void setUnit(String unit) {
		_unit = unit;
	}
	
	public boolean isEmpty() {
		return (_label == null && _type == null && _unit == null);
	}
}
