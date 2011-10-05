package org.ohmage.domain.configuration.prompt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ohmage.domain.configuration.Response.NoResponse;
import org.ohmage.domain.configuration.prompt.response.MultiChoiceCustomPromptResponse;

public class MultiChoiceCustomPrompt extends CustomChoicePrompt {
	private final Collection<String> defaultValues;
	
	public MultiChoiceCustomPrompt(final String id, final String condition, 
			final String unit, final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Map<Integer, ValueLabelPair> choices,
			final Map<Integer, ValueLabelPair> customChoices,
			final Collection<String> defaultValues, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, customChoices, 
				Type.MULTI_CHOICE_CUSTOM, index);
		
		if(defaultValues != null) {
			Collection<ValueLabelPair> allValues = getAllChoices().values();
			
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
	public MultiChoiceCustomPromptResponse createResponse(final Object response, 
			final Integer repeatableSetIteration) {
		if(response instanceof NoResponse) {
			return new MultiChoiceCustomPromptResponse(this, (NoResponse) response, repeatableSetIteration, null);
		}
		else if(response instanceof Collection<?>) {
			Collection<String> responses = new ArrayList<String>(((Collection<?>) response).size());
			
			for(Object currResponse : ((Collection<?>) response)) {
				if(currResponse instanceof String) {
					responses.add((String) currResponse);
				}
			}
			
			return new MultiChoiceCustomPromptResponse(this, null, repeatableSetIteration, responses);
		}
		
		throw new IllegalArgumentException("The response was not a NoResponse value or a Collection of Strings.");
	}
}