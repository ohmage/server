/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;

/**
 * Representation of prompt configuration data.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public abstract class Prompt extends SurveyItem {
	public static final String JSON_KEY_UNIT = "unit";
	public static final String JSON_KEY_PROMPT_TYPE = "prompt_type";
	public static final String JSON_KEY_DISPLAY_TYPE = "display_type";
	
	/**
	 */
	private final String unit;
	
	/**
	 */
	private final String text;
	/**
	 */
	private final String abbreviatedText;
	/**
	 */
	private final String explanationText;
	
	/**
	 */
	private final boolean skippable;
	/**
	 */
	private final String skipLabel;
	
	/**
	 * @author  jojenki
	 */
	public static enum DisplayType {
		/**
		 */
		MEASUREMENT,
		/**
		 */
		EVENT,
		/**
		 */
		COUNT,
		/**
		 */
		CATEGORY,
		/**
		 */
		METADATA;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	/**
	 */
	private final DisplayType displayType;
	/**
	 */
	private final String displayLabel;
	
	/**
	 * @author  jojenki
	 */
	public static enum Type {
		/**
		 */
		TIMESTAMP,
		/**
		 */
		NUMBER,
		/**
		 */
		HOURS_BEFORE_NOW,
		/**
		 */
		TEXT, 
		/**
		 */
		SINGLE_CHOICE,
		/**
		 */
		SINGLE_CHOICE_CUSTOM,
		/**
		 */
		MULTI_CHOICE,
		/**
		 */
		MULTI_CHOICE_CUSTOM,
		/**
		 */
		PHOTO,
		/**
		 */
		REMOTE_ACTIVITY;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	/**
	 */
	protected final Type type;
	
	/**
	 * @author  jojenki
	 */
	public static final class ValueLabelPair {
		private final String label;
		private final Number value;
		
		public ValueLabelPair(final String label, final Number value) {
			if(label == null) {
				throw new IllegalArgumentException("The label cannot be null.");
			}
			
			this.label = label;
			this.value = value;
		}
		
		/**
		 * @return
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * @return
		 */
		public Number getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ValueLabelPair other = (ValueLabelPair) obj;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}
	}

	public Prompt(final String id, final String condition, final String unit,
			final String text, final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final Type type, final int index) {
		
		super(id, condition, index);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(text)) {
			throw new IllegalArgumentException("The text cannot be null.");
		}
		if(skippable && StringUtils.isEmptyOrWhitespaceOnly(skipLabel)) {
			throw new IllegalArgumentException("The prompt is skippable, but the skip label is null.");
		}
		if(displayType == null) {
			throw new IllegalArgumentException("The display type cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(displayLabel)) {
			throw new IllegalArgumentException("The display label cannot be null.");
		}
		
		this.unit = unit;
		
		this.text = text;
		this.abbreviatedText = abbreviatedText;
		this.explanationText = explanationText;
		
		this.skippable = skippable;
		this.skipLabel = skipLabel;
		
		this.displayType = displayType;
		this.displayLabel = displayLabel;
		
		this.type = type;
	}
	
	/**
	 * @return
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @return
	 */
	public String getAbbreviatedText() {
		return abbreviatedText;
	}
	
	/**
	 * @return
	 */
	public String getExplanationText() {
		return explanationText;
	}
	
	public boolean skippable() {
		return skippable;
	}
	
	/**
	 * @return
	 */
	public String getSkipLabel() {
		return skipLabel;
	}
	
	/**
	 * @return
	 */
	public DisplayType getDisplayType() {
		return displayType;
	}

	/**
	 * @return
	 */
	public String getDisplayLabel() {
		return displayLabel;
	}
	
	/**
	 * @return
	 */
	public Type getType() {
		return type;
	}
	
	@Override
	public final int getNumPrompts() {
		return 1;
	}
	
	public JSONObject toJson() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_UNIT, unit);
			result.put(JSON_KEY_PROMPT_TYPE, type.toString());
			result.put(JSON_KEY_DISPLAY_TYPE, displayType.toString());
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
	
	public abstract boolean validateValue(final Object value);
	public abstract PromptResponse createResponse(final Object value, final Integer repeatableSetIteration);

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((abbreviatedText == null) ? 0 : abbreviatedText.hashCode());
		result = prime * result
				+ ((displayLabel == null) ? 0 : displayLabel.hashCode());
		result = prime * result
				+ ((displayType == null) ? 0 : displayType.hashCode());
		result = prime * result
				+ ((explanationText == null) ? 0 : explanationText.hashCode());
		result = prime * result
				+ ((skipLabel == null) ? 0 : skipLabel.hashCode());
		result = prime * result + (skippable ? 1231 : 1237);
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		Prompt other = (Prompt) obj;
		if (abbreviatedText == null) {
			if (other.abbreviatedText != null)
				return false;
		} else if (!abbreviatedText.equals(other.abbreviatedText))
			return false;
		if (displayLabel == null) {
			if (other.displayLabel != null)
				return false;
		} else if (!displayLabel.equals(other.displayLabel))
			return false;
		if (displayType != other.displayType)
			return false;
		if (explanationText == null) {
			if (other.explanationText != null)
				return false;
		} else if (!explanationText.equals(other.explanationText))
			return false;
		if (skipLabel == null) {
			if (other.skipLabel != null)
				return false;
		} else if (!skipLabel.equals(other.skipLabel))
			return false;
		if (skippable != other.skippable)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (type != other.type)
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}
}
