package org.ohmage.domain.configuration.prompt;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.RemoteActivityPromptResponse;

public class RemoteActivityPrompt extends Prompt {
	public static final String KEY_PACKAGE = "package";
	public static final String KEY_ACTIVITY = "activity";
	public static final String KEY_ACTION = "action";
	public static final String KEY_AUTOLAUNCH = "autolaunch";
	public static final String KEY_RETRIES = "retries";
	public static final String KEY_MIN_RUNS = "min_runs";
	public static final String KEY_INPUT = "input";
	
	private static final int MAX_INPUT_LENGTH = 65536;
	
	/**
	 */
	private final String packagee;
	/**
	 */
	private final String activity;
	/**
	 */
	private final String action;
	/**
	 */
	private final boolean autolaunch;
	/**
	 */
	private final int retries;
	/**
	 */
	private final int minRuns;
	/**
	 */
	private final String input;
	
	public RemoteActivityPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final String packagee, final String activity, final String action,
			final boolean autolaunch, final int retries, final int minRuns,
			final String input, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.REMOTE_ACTIVITY, index);
		
		if(! validatePackage(packagee)) {
			throw new IllegalArgumentException("The package value is invalid.");
		}
		else if(! validateActivity(activity)) {
			throw new IllegalArgumentException("The activity value is invalid.");
		}
		else if(! validateAction(action)) {
			throw new IllegalArgumentException("The action value is invalid.");
		}
		else if(validateRetriesAndMinRuns(retries, minRuns)) {
			throw new IllegalArgumentException("The minimum number of runs is greater than the number of allowed runs.");
		}
		else if((input != null) && (! validateInput(input))) {
			throw new IllegalArgumentException("The input is too long.");
		}
		
		this.packagee = packagee;
		this.activity = activity;
		this.action = action;
		this.autolaunch = autolaunch;
		this.retries = retries;
		this.minRuns = minRuns;
		this.input = input;
	}
	
	public String getPackage() {
		return packagee;
	}
	
	/**
	 * @return
	 */
	public String getActivity() {
		return activity;
	}
	
	/**
	 * @return
	 */
	public String getAction() {
		return action;
	}

	public boolean getAutolaunch() {
		return autolaunch;
	}
	
	/**
	 * @return
	 */
	public int getRetries() {
		return retries;
	}
	
	/**
	 * @return
	 */
	public int getMinRuns() {
		return minRuns;
	}
	
	/**
	 * Returns the input for the remote activity.
	 * @return  The input for the remote activity. This may be null.
	 */
	public String getInput() {
		return input;
	}
	
	@Override
	public boolean validateValue(Object value) {
		JSONObject valueJson;
		if(value instanceof String) {
			try {
				valueJson = new JSONObject((String) value);
			}
			catch(JSONException e) {
				return false;
			}
		}
		else if(value instanceof JSONObject) {
			valueJson = (JSONObject) value;
		}
		else {
			return false;
		}
		
		try {
			valueJson.getDouble("score");
		}
		catch(JSONException e) {
			// The "score" mandatory key is missing.
			return false;
		}
		
		return true;
	}
	
	@Override
	public RemoteActivityPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new RemoteActivityPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof JSONObject) {
			return new RemoteActivityPromptResponse(this, null, repeatableSetIteration, (JSONObject) response);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse or JSONObject value.");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
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
		if(! packagee.contains(".")) {
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
		if(! activity.contains(".")) {
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
		// There are no restrictions on the naming of Actions so, for now,
		// there is no validation of this data.
		return true;
	}
	
	/**
	 * As input to the remote Activity, there really is no way to validate the
	 * type of input that may be passed. Therefore, we will simply check for a
	 * reasonable length of less than {@link #_MAX_INPUT_LENGTH} characters.
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