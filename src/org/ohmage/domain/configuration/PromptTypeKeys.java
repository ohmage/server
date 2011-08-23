package org.ohmage.domain.configuration;

/**
 * A collection of constants for the supported prompt types in the system.
 * 
 * @author Joshua Selsky
 */
public final class PromptTypeKeys {

	/**
	 * Private to prevent instantiation.
	 */
	private PromptTypeKeys() { }
	
	public static final String TYPE_HOURS_BEFORE_NOW = "hours_before_now";
	public static final String TYPE_NUMBER = "number";
	public static final String TYPE_SINGLE_CHOICE = "single_choice";
	public static final String TYPE_SINGLE_CHOICE_CUSTOM = "single_choice_custom";
	public static final String TYPE_MULTI_CHOICE = "multi_choice";
	public static final String TYPE_MULTI_CHOICE_CUSTOM = "multi_choice_custom";
	public static final String TYPE_TIMESTAMP = "timestamp";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "photo";
	public static final String TYPE_REMOTE_ACTIVITY = "remote_activity";
}
