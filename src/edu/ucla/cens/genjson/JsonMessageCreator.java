package edu.ucla.cens.genjson;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Generator of AW JSON messages for testing.
 * 
 * @author selsky
 */
public interface JsonMessageCreator {
	
	/**
	 * Creates JSON messages corresponding to a particular type of message. The number of messages created depends on the
	 * numberOfMessages parameter. 
	 */
	public JSONArray createMessage(int numberOfEntries) throws JSONException;
}
