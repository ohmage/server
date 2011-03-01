package edu.ucla.cens.awserver.domain;

/**
 * Bean-style wrapper for an immutable prompt property. A prompt property defines bounds or options on a prompt type.
 * 
 * @author selsky
 */
public class PromptProperty {
	private String _key;   
	private String _value; // an optional visualization-specific value for range-bound properties 
	private String _label; // a required label describing a selection or bound
	
	public PromptProperty(String key, String value, String label) {
		_key = key;
		_value = value;
		_label = label;
	}

	public String getKey() {
		return _key;
	}

	public String getValue() {
		return _value;
	}

	public String getLabel() {
		return _label;
	}

	@Override
	public String toString() {
		return "PromptProperty [_key=" + _key + ", _label=" + _label
				+ ", _value=" + _value + "]";
	}
}
