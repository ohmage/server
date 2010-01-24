package edu.ucla.cens.awserver.domain;

/**
 * A representation of a row from the prompt_type table.
 * 
 * @author selsky
 */
public class PromptType {

	private String _type;
	private String _restriction;
	
	public String getType() {
		return _type;
	}
	public void setType(String type) {
		_type = type;
	}
	public String getRestriction() {
		return _restriction;
	}
	public void setRestriction(String restriction) {
		_restriction = restriction;
	}
	@Override
	public String toString() {
		return "PromptType [_restriction=" + _restriction + ", _type=" + _type
				+ "]";
	}
	
}
