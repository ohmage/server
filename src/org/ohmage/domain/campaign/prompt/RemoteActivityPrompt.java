package org.ohmage.domain.campaign.prompt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.RemoteActivityPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents a remote Activity prompt.
 * 
 * @author John Jenkins
 */
public class RemoteActivityPrompt extends Prompt {
	public static final String JSON_KEY_PACKAGE = "package";
	public static final String JSON_KEY_ACTIVITY = "activity";
	public static final String JSON_KEY_ACTION = "action";
	public static final String JSON_KEY_AUTOLAUNCH = "autolaunch";
	public static final String JSON_KEY_RETRIES = "retries";
	public static final String JSON_KEY_MIN_RUNS = "min_runs";
	public static final String JSON_KEY_INPUT = "input";
	
	public static final String XML_KEY_PACKAGE = "package";
	public static final String XML_KEY_ACTIVITY = "activity";
	public static final String XML_KEY_ACTION = "action";
	public static final String XML_KEY_AUTOLAUNCH = "autolaunch";
	public static final String XML_KEY_RETRIES = "retries";
	public static final String XML_KEY_MIN_RUNS = "min_runs";
	public static final String XML_KEY_INPUT = "input";
	
	public static final String JSON_KEY_SCORE = "score";
	
	private static final int MAX_INPUT_LENGTH = 65536;
	
	private final String packagee;
	private final String activity;
	private final String action;
	private final boolean autolaunch;
	private final int retries;
	private final int minRuns;
	private final String input;
	
	/**
	 * Creates a remote Activity prompt.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param unit The unit value for this prompt.
	 * 
	 * @param text The text to be displayed to the user for this prompt.
	 * 
	 * @param abbreviatedText An abbreviated version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayType This prompt's
	 * 					 {@link org.ohmage.domain.campaign.Prompt.DisplayType}.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param packagee The remote Activity's package.
	 * 
	 * @param activity The remote Activity's name.
	 * 
	 * @param action The action to pass to the remote Activity.
	 * 
	 * @param autolaunch Whether or not to automatically launch the remote
	 * 					 Activity when the prompt is displayed.
	 * 
	 * @param retries The number of times the user is allowed to relaunch the
	 * 				  remote Activity.
	 * 
	 * @param minRuns The minimum number of times the user must launch the
	 * 				  remote activity.
	 * 
	 * @param input An optional string value to pass to the remote Activity.
	 * 				This may be null if no such Activity exists.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public RemoteActivityPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String abbreviatedText, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final DisplayType displayType, 
			final String displayLabel,
			final String packagee, 
			final String activity, 
			final String action,
			final boolean autolaunch, 
			final int retries, 
			final int minRuns,
			final String input, 
			final int index) 
			throws DomainException {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.REMOTE_ACTIVITY, index);
		
		if(! validatePackage(packagee)) {
			throw new DomainException("The package value is invalid.");
		}
		else if(! validateActivity(activity)) {
			throw new DomainException("The activity value is invalid.");
		}
		else if(! validateAction(action)) {
			throw new DomainException("The action value is invalid.");
		}
		else if(validateRetriesAndMinRuns(retries, minRuns)) {
			throw new DomainException(
					"The minimum number of runs is greater than the number of allowed runs.");
		}
		else if((input != null) && (! validateInput(input))) {
			throw new DomainException("The input is too long.");
		}
		
		this.packagee = packagee;
		this.activity = activity;
		this.action = action;
		this.autolaunch = autolaunch;
		this.retries = retries;
		this.minRuns = minRuns;
		this.input = input;
	}
	
	/**
	 * Returns the remote Activity's package.
	 * 
	 * @return The remote Activity's package.
	 */
	public String getPackage() {
		return packagee;
	}
	
	/**
	 * Returns the remote Activity's name.
	 * 
	 * @return The remote Activity's name.
	 */
	public String getActivity() {
		return activity;
	}
	
	/**
	 * Returns the action to pass to the remote Activity.
	 * 
	 * @return The action to pass to the remote Activity.
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Whether or not to automatically launch the remote Activity when the 
	 * prompt is displayed.
	 * 
	 * @return Whether or not to automatically launch the remote Activity when
	 * 		   the prompt is displayed.
	 */
	public boolean getAutolaunch() {
		return autolaunch;
	}
	
	/**
	 * Returns the number of times the user is allowed to relaunch the 
	 * Activity. The user will always be allowed to launch it at least once and
	 * this indicates the number of subsequent launches.
	 * 
	 * @return The number of times the user is allowed to relaunch the
	 * 		   Activity.
	 */
	public int getRetries() {
		return retries;
	}
	
	/**
	 * Returns the minimum number of times the user must launch the Activity.
	 * 
	 * @return The minimum number of times the user must launch the Activity.
	 */
	public int getMinRuns() {
		return minRuns;
	}
	
	/**
	 * Returns the input for the remote activity.
	 * 
	 * @return  The input for the remote activity. This may be null.
	 */
	public String getInput() {
		return input;
	}
	
	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A JSONObject value if it is a valid response or a 
	 * 		   {@link NoResponse} object value if it is a valid 
	 * 		   {@link NoResponse} value.
	 * 
	 * @throws DomainException Thrown if the value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		JSONArray valueJson;
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already a JSONObject value, then set that to be validated.
		else if(value instanceof JSONArray) {
			valueJson = (JSONArray) value;
		}
		// If it's a JSON
		// If it is a string, parse it to check if it's a NoResponse value and,
		// if not, parse it as a JSONObject to be validated.
		else if(value instanceof String) {
			String valueString = (String) value;
			
			try {
				return NoResponse.valueOf(valueString);
			}
			catch(IllegalArgumentException iae) {
				try {
					valueJson = new JSONArray((String) valueString);
				}
				catch(JSONException e) {
					throw new DomainException(
							"The string value could not be decoded as a NoResponse or JSONObject object.");
				}
			}
		}
		// Finally, if its type is unknown, throw an exception.
		else {
			throw new DomainException(
					"The value is not decodable as a response value.");
		}
		
		int numResponses = valueJson.length();
		for(int i = 0; i < numResponses; i++) {
			JSONObject currResponse;
			try {
				currResponse = valueJson.getJSONObject(i);
			}
			catch(JSONException e) {
				throw new DomainException(
						"The item at index '" +
							i +
							"' isn't a valid JSONObject.",
						e);
			}
			
			try {
				currResponse.getDouble(JSON_KEY_SCORE);
			}
			catch(JSONException e) {
				throw new DomainException(
						"The JSON response value must contain the key '" +
							JSON_KEY_SCORE +
							"' which must be a numeric value.",
						e);
			}
		}
		
		return valueJson;
	}
	
	/**
	 * Creates a response to this prompt based on a response value.
	 * 
	 * @param response The response from the user as an Object.
	 * 
	 * @param repeatableSetIteration If this prompt belongs to a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which the response to
	 * 								 this prompt was made.
	 * 
	 * @throws DomainException Thrown if this prompt is part of a repeatable 
	 * 						   set but the repeatable set iteration value is 
	 * 						   null, if the repeatable set iteration value is 
	 * 						   negative, or if the value is not a valid 
	 * 						   response value for this prompt.
	 */
	@Override
	public RemoteActivityPromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new RemoteActivityPromptResponse(
				this,
				repeatableSetIteration,
				response);
	}
	
	/**
	 * Creates a JSONObject that represents this remote Activity prompt.
	 * 
	 * @return A JSONObject that represents this remote Activity prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_PACKAGE, packagee);
		result.put(JSON_KEY_ACTIVITY, activity);
		result.put(JSON_KEY_ACTION, action);
		result.put(JSON_KEY_AUTOLAUNCH, autolaunch);
		result.put(JSON_KEY_RETRIES, retries);
		result.put(JSON_KEY_MIN_RUNS, minRuns);
		result.put(JSON_KEY_INPUT, input);
		
		return result;
	}
	
	/**
	 * Returns a hash code representing this prompt.
	 * 
	 * @return A hash code representing this prompt.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((activity == null) ? 0 : activity.hashCode());
		result = prime * result + (autolaunch ? 1231 : 1237);
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + minRuns;
		result = prime * result
				+ ((packagee == null) ? 0 : packagee.hashCode());
		result = prime * result + retries;
		return result;
	}

	/**
	 * Determines if this prompt is logically equivalent to another prompt.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the other object is logically equivalent to this
	 * 		   prompt.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteActivityPrompt other = (RemoteActivityPrompt) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (autolaunch != other.autolaunch)
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (minRuns != other.minRuns)
			return false;
		if (packagee == null) {
			if (other.packagee != null)
				return false;
		} else if (!packagee.equals(other.packagee))
			return false;
		if (retries != other.retries)
			return false;
		return true;
	}

	/**
	 * Unfortunately, there are few restrictions on Android's Package names.
	 * 
	 * @param packagee The value of the 'package' property.
	 * 
	 * @return True if the value is valid; false, otherwise.
	 */
	private boolean validatePackage(final String packagee) {
		if(packagee == null) {
			return false;
		}
		else if(! packagee.contains(".")) {
			return false;
		}
		else if(packagee.startsWith(".")) {
			return false;
		}
		else if(packagee.endsWith(".")) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Unfortunately, there are few restrictions on Android's Activity names.
	 * 
	 * @param activity The value of the 'activity' property.
	 * 
	 * @return True if the value is valid; false, otherwise.
	 */
	private boolean validateActivity(final String activity) {
		if(activity == null) {
			return false;
		}
		else if(! activity.contains(".")) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Unfortunately, there are few restrictions on Android's Action names.
	 * 
	 * @param action The value of the 'action' property.
	 * 
	 * @return True if the value is valid; false, otherwise.
	 */
	private boolean validateAction(final String action) {
		if(action == null) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * As input to the remote Activity, there really is no way to validate the
	 * type of input that may be passed. Therefore, we will simply check for a
	 * reasonable length of less than {@link #MAX_INPUT_LENGTH} characters.
	 * 
	 * @param input The value of the 'input' property.
	 * 
	 * @return True if the value is valid; false, otherwise.
	 */
	private boolean validateInput(final String input) {
		if(input.length() > MAX_INPUT_LENGTH) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validates that the minimum number of runs is less than or equal to the
	 * maximum number of times that a user can actually execute the remote
	 * Activity.
	 * 
	 * @param minRuns The minimum number of runs.
	 * 
	 * @param retries the minimum number of retries.
	 * 
	 * @return True if the values are valid; false, otherwise.
	 */
	private boolean validateRetriesAndMinRuns(final int minRuns, final int retries) {
		if(minRuns > (retries + 1)) {
			return false;
		}
		
		return true;
	}
}