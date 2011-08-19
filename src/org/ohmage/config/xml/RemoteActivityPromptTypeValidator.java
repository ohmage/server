package org.ohmage.config.xml;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathException;

import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.util.StringUtils;

public class RemoteActivityPromptTypeValidator extends AbstractPromptTypeValidator {
	private static final int MAX_INPUT_LENGTH = 65536;
	
	private int minRuns;
	private int retries;

	/**
	 * Check that the following properties exist and that they are sane.
	 * 
	 * 		package : The package to which the remote Activity belongs that
	 * 				  will catch the broadcasted Action.
	 * 		activity : The name of the Activity that will catch the
	 * 				   broadcasted Action.
	 * 		action : The Action to broadcast.
	 * 
	 * 		autolauch : Whether or not to broadcast the Action as soon as the
	 * 					prompt is displayed.
	 * 		retries : The number of times the user is allowed to re-broadcast
	 * 				  the Action to effectively recall the Activity. This
	 * 				  value may be 0 even if autolaunch is is "false" because
	 * 				  the user will always get to launch the Activity at least
	 * 				  once.
	 * 
	 * The following optional parameter(s) may also be included and their
	 * values are also sanity checked.
	 * 
	 * 		input : A String value that will be added to the broadcasted
	 * 				Intent as input to the calling action.
	 * 
	 * @throws IllegalStateException Thrown if there is an error with the
	 * 								 properties list.
	 */
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);

		// Check the entire property list.
		try {
			Nodes propertyNodes = promptNode.query("properties/property");
			
			// Check all the property values to make sure they are valid and
			// remember all the keys to make sure all required properties
			// existed.
			int numProperties = propertyNodes.size();
			List<String> propertyKeys = new ArrayList<String>(numProperties);
			Node currProperty, currPropertyKeyNode, currPropertyLabelNode;
			for(int i = 0; i < numProperties; i++)
			{
				currProperty = propertyNodes.get(i);				
				currPropertyKeyNode = getSingleTag(currProperty, "key");
				currPropertyLabelNode = getSingleTag(currProperty, "label");
				
				// This is checked after we have retrieved the 'key' and 
				// 'label' values because those are needed and problems with
				// them should be announced before general problems with the
				// number of properties.
				// FIXME: This doesn't work indicating a problem with the XOM
				//	libraries. For now, I will comment this out, but it needs
				//	to be addressed.
//				if(currProperty.getChildCount() != 2) {
//					throw new IllegalStateException("Too many property values. Expected 2 but found " + currProperty.getChildCount() + ": " + currProperty.toXML());
//				}

				validateKeyLabelPair(currPropertyKeyNode.getValue().trim(), currPropertyLabelNode.getValue().trim());
				propertyKeys.add(currPropertyKeyNode.getValue().trim());
			}
			
			// Finally, make sure all of the required properties were found.
			if(! propertyKeys.contains("package")) {
				throw new IllegalStateException("Missing 'package' key: " + promptNode.toXML());
			}
			else if(! propertyKeys.contains("activity")) {
				throw new IllegalStateException("Missing 'activity' key: " + promptNode.toXML());
			}
			else if(! propertyKeys.contains("action")) {
				throw new IllegalStateException("Missing 'action' key: " + promptNode.toXML());
			}
			else if(! propertyKeys.contains("autolaunch")) {
				throw new IllegalStateException("Missing 'autolaunch' key: " + promptNode.toXML());
			}
			else if(! propertyKeys.contains("retries")) {
				throw new IllegalStateException("Missing 'retries' key: " + promptNode.toXML());
			}
			else if(! propertyKeys.contains("min_runs")) {
				throw new IllegalStateException("Missing 'min_runs' key: " + promptNode.toXML());
			}
			else if(! validateRetriesAndMinRuns()) {
				throw new IllegalStateException("'min_runs' dictates that the user must run the remote Activity more times than they are allowed to run it via 'retries': " + promptNode.toXML());
			}
		}
		catch(XPathException e) {
			// Thrown if the list of properties is  not parsable.
			throw new IllegalStateException("Invalid properties list: " + promptNode.toXML());
		}
		catch(IndexOutOfBoundsException e) {
			// Thrown if the number of properties changes mid-analysis.
			// I don't know how this would ever happen unless there were some
			// concurrency issues, but I will throw an IllegalStateException
			// anyway.
			throw new IllegalStateException("The properties list has changed mid processing. Possible concurrency issue?");
		}
		catch(IllegalArgumentException e) {
			// Thrown if there is an incorrect number of a tag in a property
			// or if the values of one of those tags is null.
			if(StringUtils.isEmptyOrWhitespaceOnly(e.getMessage())) {
				throw new IllegalStateException("Invalid 'key' or 'label' value(s) in " + promptNode.toXML());
			}
			else {
				throw new IllegalStateException(e.getMessage() + " in " + promptNode.toXML());
			}
		}
	}

	/**
	 * Validates that, if the prompt was not skipped, it is a valid JSONArray.
	 */
	@Override
	@SuppressWarnings("unused")
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			try {
				JSONArray promptResult = new JSONArray(pair.getValue());
			}
			catch(JSONException e) {
				throw new IllegalArgumentException("Invalid return value: " + pair.getValue(), e);
			}
		}
	}

	/**
	 * Called when checking the default value, however default values are
	 * disallowed for remote Activity prompt types.
	 */
	@Override
	public void checkDefaultValue(String value) {
		throw new IllegalArgumentException("default values are disallowed for remote Activity prompt types");
	}

	/**
	 * Searches for the 'tag' tag in the 'property' property. If no such tag
	 * is found or more than one of the tag is found, it throws an 
	 * IllegalArgumentException exception. If there is any error parsing the
	 * property, it throws an XPathException exception. It then returns the
	 * single 'Node' in question.
	 * 
	 * @param property The property to be queried upon that should contain 
	 * 				   exactly one 'key' tag and exactly one a 'label' tag.
	 *  
	 * @param tag The tag to be searched for in the parameterized 'property'.
	 * 
	 * @return Returns a Node object that was derived from the property 'Node'
	 * 		   and contains the parameterized 'tag'.
	 * 
	 * @throws XPathException Thrown if the parameterized 'property' is not
	 * 						  parsable.
	 * 
	 * @throws IllegalArgumentException Thrown if the number of 'tag's in the
	 * 									'property' is not exactly 1.
	 */
	private Node getSingleTag(Node property, String tag) throws XPathException, IllegalArgumentException {
		Nodes result = property.query(tag);
		
		if(result.size() <= 0) {
			throw new IllegalArgumentException("No '" + tag + "' tag for: " + property.toXML());
		}
		else if(result.size() > 1) {
			throw new IllegalArgumentException("Too many '" + tag + "key' tags for: " + property.toXML());
		}
		
		return result.get(0);
	}
	
	/**
	 * Switches on the different possible property keys for the RemoteActivity
	 * prompt type and validates that their values ('label') are correct.
	 * 
	 * @param key The property key being validated.
	 * 
	 * @param label The value of this property.
	 * 
	 * @throws IllegalArgumentException Thrown if 'key' is null or not in the
	 * 									list of acceptable keys or if the
	 * 									'label' is null or not valid for the
	 * 									given key.
	 */
	private void validateKeyLabelPair(String key, String label) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("Empty key value");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(label)) {
			throw new IllegalArgumentException("'" + key + "' label is invalid");
		}
		
		if(key.equals("package")) {
			validatePackage(label);
		}
		else if(key.equals("activity")) {
			validateActivity(label);
		}
		else if(key.equals("action")) {
			validateAction(label);
		}
		else if(key.equals("autolaunch")) {
			validateAutolaunch(label);
		}
		else if(key.equals("retries")) {
			validateRetries(label);
		}
		else if(key.equals("input")) {
			validateInput(label);
		}
		else if(key.equals("min_runs")) {
			validateMinRuns(label);
		}
		else {
			throw new IllegalArgumentException("Invalid key in properties list: " + key);
		}
	}
	
	/**
	 * Unfortunately, there are few restrictions on Android's Package names.
	 * 
	 * @param value The value of the 'package' property.
	 */
	private void validatePackage(String value) {
		if(! value.contains(".")) {
			throw new IllegalArgumentException("All Android package names must contain at least one '.'");
		}
	}
	
	/**
	 * Unfortunately, there are few restrictions on Android's Activity names.
	 * 
	 * @param value The value of the 'activity' property.
	 */
	private void validateActivity(String value) {
		if(! value.contains(".")) {
			throw new IllegalArgumentException("All Android Activitys must contain at least one '.'");
		}
	}
	
	/**
	 * Unfortunately, there are few restrictions on Android's Action names.
	 * 
	 * @param value The value of the 'action' property.
	 */
	private void validateAction(String value) {
		// There are no restrictions on the naming of Actions so, for now,
		// there is no validation of this data.
	}
	
	/**
	 * Must be one of 'true' or 'false'.
	 * 
	 * @param value The value of the 'autolaunch' property.
	 */
	private void validateAutolaunch(String value) {
		if((! value.equals("true")) && (! value.equals("false"))) {
			throw new IllegalArgumentException("'autolaunch' must be either 'true' or 'false'");
		}
	}
	
	/**
	 * Must be a non-negative integer.
	 * 
	 * @param value The value of the 'retries' property.
	 */
	private void validateRetries(String value) {
		try {
			retries = Integer.parseInt(value);
			
			if(retries < 0) {
				throw new IllegalArgumentException("'retries' must be non-negative");
			}
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("'retries' is not a valid integer", e);
		}
	}
	
	/**
	 * As input to the remote Activity, there really is no way to validate the
	 * type of input that may be passed. Therefore, we will simply check for a
	 * reasonable length of less than {@link #_MAX_INPUT_LENGTH} characters.
	 * 
	 * @param value The value of the 'input' property.
	 */
	private void validateInput(String value) {
		if(value.length() > MAX_INPUT_LENGTH) {
			throw new IllegalArgumentException("'input' can only be " + MAX_INPUT_LENGTH + " characters");
		}
	}
	
	/**
	 * Validates that the minimum number of runs a user must do of the remote
	 * Activity is a non-negative integer.
	 * 
	 * @param value The minimum number of times a user must run the remote
	 * 				Activity.
	 */
	private void validateMinRuns(String value) {
		try {
			minRuns = Integer.parseInt(value);
			
			if(minRuns < 0) {
				throw new IllegalArgumentException("'min_runs' must be non-negative.");
			}
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("'min_runs' is not a valid integer.", e);
		}
	}
	
	/**
	 * Validates that the minimum number of runs is less than or equal to the
	 * maximum number of times that a user can actually execute the remote
	 * Activity.
	 */
	private boolean validateRetriesAndMinRuns() {
		if(minRuns > (retries + 1)) {
			return false;
		}
		
		return true;
	}
}
