package edu.ucla.cens.awserver.domain;

public class PromptResponseDataPacket implements DataPacket {
	private String _promptId;   
	private String _repeatableSetId; 
	private Integer _repeatableSetIteration;
	private String _value;
	private String _type;
	
	public PromptResponseDataPacket() { }
	
	public String getPromptId() {
		return _promptId;
	}
	
	public void setPromptId(String promptId) {
		_promptId = promptId;
	}
	
	public void setRepeatableSetId(String repeatableSetId) {
		_repeatableSetId = repeatableSetId;
	}
	
	public String getRepeatableSetId() {
		return _repeatableSetId;
	}

	public void setRepeatableSetIteration(Integer repeatableSetIteration) {
		_repeatableSetIteration = repeatableSetIteration;
	}
	
	public Integer getRepeatableSetIteration() {
		return _repeatableSetIteration;
	}

	public String getValue() {
		return _value;
	}
	
	public void setValue(String value) {
		_value = value;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public String toString() {
		return "PromptResponseDataPacket [_promptId=" + _promptId
				+ ", _repeatableSetId=" + _repeatableSetId
				+ ", _repeatableSetIteration=" + _repeatableSetIteration
				+ ", _type=" + _type + ", _value=" + _value + "]";
	}
}
