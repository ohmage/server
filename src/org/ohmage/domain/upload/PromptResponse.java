package org.ohmage.domain.upload;

/**
 * Immutable bean-style wrapper for prompt responses uploaded as part of a survey.
 * 
 * @author Joshua Selsky
 * @see org.ohmage.util.SurveyResponse
 */
public class PromptResponse {
	private String promptId;
	private String repeatableSetId;
	private String repeatableSetIteration;
	private String value;
	private String type;
	
	/**
	 * Default constructor. Assumes all parameters have been previously
	 * validated.
	 * 
	 * @param promptId The prompt id as given in a configuration. 
	 * @param repeatableSetId A repeatable set id as given in a configuration.
	 * @param repeatableSetIteration A repeatable set iteration determined by
	 * the iteration of a repeatable set as the client user fills out 1...n
	 * repeatable sets.
	 * @param value The prompt value. Can be JSON, a vanilla String, a timestamp,
	 * a number, etc. Stored in a schema-less column via the data layer.
	 * @param type The prompt type as defined by the currently supported prompt
	 * types. 
	 * @see org.ohmage.domain.configuration.PromptTypeKeys
	 * @see org.ohmage.domain.configuration.Configuration
	 */
	public PromptResponse(String promptId, String repeatableSetId, String repeatableSetIteration, String value, String type) {
		this.promptId = promptId;
		this.repeatableSetId = repeatableSetId;
		this.repeatableSetIteration = repeatableSetIteration;
		this.value = value;
		this.type = type;
	}

	public String getPromptId() {
		return promptId;
	}

	public String getRepeatableSetId() {
		return repeatableSetId;
	}

	public String getRepeatableSetIteration() {
		return repeatableSetIteration;
	}

	public String getValue() {
		return value;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "PromptResponse [promptId=" + promptId + ", repeatableSetId="
				+ repeatableSetId + ", repeatableSetIteration="
				+ repeatableSetIteration + ", value=" + value + ", type="
				+ type + "]";
	}
}
