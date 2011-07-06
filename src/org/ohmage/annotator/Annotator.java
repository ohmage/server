package org.ohmage.annotator;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * This class handles the information regarding errors in the system. It
 * contains an error code and some human-readable description of the error.
 * 
 * @author John Jenkins
 */
public class Annotator {
	/**
	 * The JSON key to use to represent the error code.
	 */
	public static final String JSON_KEY_CODE = "code";
	/**
	 * The JSON key to use to represent the human-readable text for the error.
	 */
	public static final String JSON_KEY_TEXT = "text"; 
	
	private String code;
	private String text;
	
	/**
	 * Default constructor. The default error response is a general server 
	 * error.
	 */
	public Annotator() {
		code = "0103";
		text = "General server error.";
	}
	
	/**
	 * Sets the initial code and text with which to respond.
	 * 
	 * @param initialCode The initial code to use. This should be a four
	 * 					  character String from the error code list on the
	 * 					  wiki.
	 * 
	 * @param initialText The initial text with which to respond. This should
	 * 					  correlate with the 'initialCode'.
	 */
	public Annotator(String initialCode, String initialText) {
		code = initialCode;
		text = initialText;
	}
	
	/**
	 * Updates the code and text for this annotator.
	 * 
	 * @param newCode The new code with which to respond. This should be a four
	 * 				  character String from the error codes list on the wiki.
	 * 
	 * @param newText The new text with which to respond. This should correlate
	 * 				  with the 'newCode'.
	 */
	public void update(String newCode, String newText) {
		code = newCode;
		text = newText;
	}
	
	/**
	 * Creates a JSONObject that represents the error code and text.
	 * 
	 * @return A JSONObject that represents the error code and text. The key
	 * 		   for the code is {@value #JSON_KEY_CODE} and the key for the text
	 * 		   is {@value #JSON_KEY_TEXT}.
	 * 
	 * @throws JSONException Thrown if there is an error building the 
	 * 						 JSONObject.
	 */
	public JSONObject toJsonObject() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_CODE, code);
		result.put(JSON_KEY_TEXT, text);
		
		return result;
	}
}
