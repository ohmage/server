package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class PromptTypeUtils {
	
	private PromptTypeUtils() { }
	
	public static boolean isSingleChoiceType(String type) {
		return "single_choice".equals(type);
	}
	
	public static boolean isMultiChoiceType(String type) {
		return "multi_choice".equals(type);
	}
	
	public static boolean isJsonArray(String type) {
		return "multi_choice".equals(type);
	}
	
	public static boolean isJsonObject(String type) {
		return "multi_choice_custom".equals(type)
		    || "single_choice_custom".equals(type);
	}
	
	public static boolean isNumberPromptType(String type) {
		return "hours_before_now".equals(type)
		    || "number".equals(type);
	}
	
}
