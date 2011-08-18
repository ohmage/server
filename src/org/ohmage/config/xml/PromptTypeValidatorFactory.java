package org.ohmage.config.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for PromptTypeValidators.
 * 
 * @author selsky
 */
public class PromptTypeValidatorFactory {
	private static final List<String> promptTypes;
	
	static {
		promptTypes = new ArrayList<String>();
		
		promptTypes.add("timestamp");
		promptTypes.add("number");
		promptTypes.add("hours_before_now");
		promptTypes.add("text");
		promptTypes.add("multi_choice");
		promptTypes.add("multi_choice_custom");
		promptTypes.add("single_choice");
		promptTypes.add("single_choice_custom");
		promptTypes.add("photo");
		promptTypes.add("remote_activity");
	}
	
	// prevent instantiation
	private PromptTypeValidatorFactory() {
		
	}
	
	/**
	 * Returns a new PromptTypeValidator for the provided promptType. 
	 */
	public static PromptTypeValidator getValidator(String promptType) {
		
		if(null == promptType) {
			throw new IllegalArgumentException("cannot create a PromptTypeValidator for a missing prompt type.");
		}
		
		if("number".equals(promptType)) {
			
			return new NumberMinMaxPromptTypeValidator();
			
		} else if ("hours_before_now".equals(promptType)){
			
			return new HoursBeforeNowPromptTypeValidator();
			 
		} else if ("single_choice".equals(promptType) || "multi_choice".equals(promptType)) {
								
			return new ChoicePromptTypeValidator();
			
		} else if ("single_choice_custom".equals(promptType) || "multi_choice_custom".equals(promptType)) {
			
			return new CustomChoicePromptTypeValidator();
		
		} else if("text".equals(promptType))  {
		
			return new TextMinMaxPromptTypeValidator();
		
		} else if("photo".equals(promptType))  {
		
			return new PhotoPromptTypeValidator();
			
		} else if("timestamp".equals(promptType))  {
			
			return new TimestampPromptTypeValidator();
			
		} else if("remote_activity".equals(promptType))  {
			
			return new RemoteActivityPromptTypeValidator();
			
		} else { 
			
			throw new IllegalArgumentException("unknown prompt type.");
		}
	}
	
	/**
	 * Returns whether the provided promptType is supported by this factory. 
	 */
	public static boolean isValidPromptType(String promptType) {
		return promptTypes.contains(promptType);
	}                                                           
}
