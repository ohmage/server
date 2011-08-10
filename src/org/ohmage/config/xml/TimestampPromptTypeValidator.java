package org.ohmage.config.xml;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import nu.xom.Node;
import nu.xom.Nodes;

import org.ohmage.config.grammar.custom.ConditionValuePair;

/**
 * Timestamp values are truncated ISO8601 timestamps of the form yyyy-MM-dd'T'HH:mm:ss where the single quotes are omitted around
 * the T.
 * 
 * @author selsky
 */
public class TimestampPromptTypeValidator implements PromptTypeValidator {
	private static final SimpleDateFormat _timestampFormat;
	
	static { // SimpleDateFormat is not thread-safe, but since the main validator has only one thread this is ok.
		_timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		_timestampFormat.setLenient(false);
	}
	
	@Override
	public void checkDefaultValue(String value) {
		checkTimestamp(value);
	}

	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		Nodes propNodes = promptNode.query("properties");
		if(propNodes.size() > 0) {
			throw new IllegalArgumentException("the properties element is disallowed for the timestamp prompt type");
		}
	}

	/**
	 * Makes sure the value is a valid timestamp
	 */
	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		checkTimestamp(pair.getValue());
	}
	
	private void checkTimestamp(String value) {
		ParsePosition pp = new ParsePosition(0);
		if(null == _timestampFormat.parse(value, pp)) {
			throw new IllegalArgumentException("Not a valid timestamp [" +  value + "] Parsing failed at position " 
				+ pp.getErrorIndex());
		}
	}
}
