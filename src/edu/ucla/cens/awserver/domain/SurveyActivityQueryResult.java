package edu.ucla.cens.awserver.domain;

import java.sql.Timestamp;

public class SurveyActivityQueryResult {
	private double _value;
	private Timestamp _promptTimestamp;
	private String _promptTimezone;

	public double getValue() {
		return _value;
	}

	public void setValue(double value) {
		_value = value;
	}

	public Timestamp getPromptTimestamp() {
		return _promptTimestamp;
	}

	public void setPromptTimestamp(Timestamp promptTimestamp) {
		_promptTimestamp = promptTimestamp;
	}

	public String getPromptTimezone() {
		return _promptTimezone;
	}

	public void setPromptTimezone(String promptTimezone) {
		_promptTimezone = promptTimezone;
	}

	@Override
	public String toString() {
		return "SurveyActivityQueryResult [_promptTimestamp="
				+ _promptTimestamp + ", _promptTimezone=" + _promptTimezone
				+ ", _value=" + _value + "]";
	}
}
