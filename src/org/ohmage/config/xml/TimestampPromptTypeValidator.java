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
	private static final String FORMAT_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";
	
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
		// TODO: This can be done from a TimeUtil, but you don't get to report
		// the failure position. But, I believe this will always be ignored.
		if(null == (new SimpleDateFormat(FORMAT_TIMESTAMP)).parse(value, pp)) {
			throw new IllegalArgumentException("Not a valid timestamp [" +  value + "] Parsing failed at position " 
				+ pp.getErrorIndex());
		}
	}
}
