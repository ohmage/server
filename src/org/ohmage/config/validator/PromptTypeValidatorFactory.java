package org.ohmage.config.validator;

import java.util.ArrayList;
import java.util.List;

import org.andwellness.config.xml.ChoicePromptTypeValidator;
import org.andwellness.config.xml.CustomChoicePromptTypeValidator;
import org.andwellness.config.xml.HoursBeforeNowPromptTypeValidator;
import org.andwellness.config.xml.NumberMinMaxPromptTypeValidator;
import org.andwellness.config.xml.PhotoPromptTypeValidator;
import org.andwellness.config.xml.PromptTypeValidator;
import org.andwellness.config.xml.RemoteActivityPromptTypeValidator;
import org.andwellness.config.xml.TextMinMaxPromptTypeValidator;
import org.andwellness.config.xml.TimestampPromptTypeValidator;

/**
 * Factory for PromptTypeValidators.
 * 
 * @author selsky
 */
public class PromptTypeValidatorFactory {
	private static final List<String> _promptTypes;
	
	static {
		_promptTypes = new ArrayList<String>();
		
		_promptTypes.add("timestamp");
		_promptTypes.add("number");
		_promptTypes.add("hours_before_now");
		_promptTypes.add("text");
		_promptTypes.add("multi_choice");
		_promptTypes.add("multi_choice_custom");
		_promptTypes.add("single_choice");
		_promptTypes.add("single_choice_custom");
		_promptTypes.add("photo");
		_promptTypes.add("remote_activity");
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
		return _promptTypes.contains(promptType);
	}                                                           
}
