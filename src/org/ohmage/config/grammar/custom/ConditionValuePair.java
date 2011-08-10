package org.ohmage.config.grammar.custom;

/**
 * Associates a condition (an operator value) with a value.
 * 
 * @author selsky
 */
public class ConditionValuePair {
	private String condition;
	private String value;
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "ConditionValuePair [condition=" + condition + ", value="
				+ value + "]";
	}
}
