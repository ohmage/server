package org.ohmage.domain.configuration.prompt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.MultiChoicePromptResponse;
import org.ohmage.util.StringUtils;

public class MultiChoicePrompt extends ChoicePrompt {
	private final Collection<String> defaultValues;
	
	public MultiChoicePrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, ValueLabelPair> choices, 
			final Collection<String> defaultValues, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, Type.MULTI_CHOICE, index);
		
		if(defaultValues != null) {
			Collection<ValueLabelPair> allValues = getChoices().values();
			
			Set<String> tempDefaultValues = new HashSet<String>(defaultValues);
			tempDefaultValues.removeAll(allValues);
			if(tempDefaultValues.size() != 0) {
				throw new IllegalArgumentException(
						"The following default values are not valid choices: " + 
						tempDefaultValues);
			}
		}
		this.defaultValues = defaultValues;
	}
	
	public final Collection<String> getDefaultValues() {
		return defaultValues;
	}

	@Override
	public boolean validateValue(Object value) {
		Collection<ValueLabelPair> choiceValues = getChoices().values();
		
		if(value instanceof Collection<?>) {
			for(Object currValue : ((Collection<?>) value)) {
				if(currValue instanceof String) {
					String currString = (String) currValue;
					
					if(! choiceValues.contains(currString)) {
						return false;
					}
				}
			}
		}
		else if(value instanceof String) {
			String string = (String) value;
			String prunedString = string.substring(1, string.length() - 1);
			
			List<String> result = StringUtils.splitString(prunedString, ",");
			for(String currValue : result) {
				if(! choiceValues.contains(currValue)) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public MultiChoicePromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new MultiChoicePromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof Collection<?>) {
			Collection<String> responses = new ArrayList<String>(((Collection<?>) response).size());
			
			for(Object currResponse : ((Collection<?>) response)) {
				if(currResponse instanceof String) {
					responses.add((String) currResponse);
				}
			}
			
			return new MultiChoicePromptResponse(this, null, repeatableSetIteration, responses);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse value or a Collection of Strings.");
	}
}