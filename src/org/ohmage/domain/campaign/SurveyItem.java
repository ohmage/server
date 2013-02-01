/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.domain.campaign;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.DomainException;

/**
 * This class corresponds to all objects that present information to or sample
 * information from a user.  
 * 
 * @author John Jenkins
 */
public abstract class SurveyItem {
	private static final String JSON_KEY_ID = "id";
	private static final String JSON_KEY_CONDITION = "condition";
	private static final String JSON_KEY_INDEX = "index";
	
	/**
	 * The prompt's campaign-unique identifier.
	 */
	private final String id;
	
	/**
	 * The condition as to whether or not this abstract prompt should be 
	 * processed.
	 */
	private final String condition;
	
	/**
	 * The index of the survey item in its group of survey items.
	 */
	private final int index;
	
	/**
	 * The repeatable set that contains this survey item or null if it is at
	 * the top of the survey hierarchy.
	 */
	private RepeatableSet parent = null;
	
	/**
	 * This class represents all of the different possible types of survey
	 * items.
	 * 
	 * @author John Jenkins
	 */
	public static enum Type {
		MESSAGE ("message"),
		REPEATABLE_SET ("repeatableSet"),
		PROMPT ("prompt");
		
		private final String xmlValue;
		
		/**
		 * Creates a Type with how it is displayed in XML.
		 * 
		 * @param xmlValue The XML value for a survey item type.
		 */
		private Type(final String xmlValue) { 
			this.xmlValue = xmlValue;
		}
		
		/**
		 * Compares the 'xmlValue' with the internal XML value for each of the
		 * types until a match is found, which is returned. If no match is 
		 * found an exception is thrown.
		 * 
		 * @param xmlValue The value to be compared to the Types' XML values.
		 * 
		 * @return A Type that is equivalent to the XML type of one of the 
		 * 		   Types.
		 * 
		 * @throws IllegalArgumentException Thrown if no Type has the XML type
		 * 									'xmlValue'.
		 */
		public static Type getValue(final String xmlValue) {
			Type[] types = Type.values();
			
			for(int i = 0; i < types.length; i++) {
				if(types[i].xmlValue.equals(xmlValue)) {
					return types[i];
				}
			}
			
			throw new IllegalArgumentException("Unknown type.");
		}
		
		/**
		 * Returns the XML value for this Type.
		 * 
		 * @return The XML value for this Type.
		 */
		@Override
		public String toString() {
			return xmlValue;
		}
	}
	
	/**
	 * Creates an abstract prompt with a condition determining if it should be
	 * displayed to the user or not.
	 * 
	 * @param id The campaign-unique identifier for this prompt.
	 * 
	 * @param condition Determines if this abstract prompt should be displayed
	 * 					to the user or not.
	 * 
	 * @param index The index of the survey item in its list of survey items.
	 * 
	 * @throws DomainException Thrown if the ID is null.
	 */
	public SurveyItem(
			final String id, 
			final String condition, 
			final int index) 
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		
		this.id = id;
		this.condition = condition;
		this.index = index;
	}
	
	/**
	 * Returns the campaign-unique identifier for this prompt.
	 * 
	 * @return The campaign-unique identifier for this prompt.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the condition.
	 * 
	 * @return The condition as a string.
	 */
	public String getCondition() {
		return condition;
	}
	
	/**
	 * Returns this survey item's index in its group of survey items.
	 * 
	 * @return This survey item's index in its group of survey items.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Returns this survey item's parent which must be a repeatable set.
	 * 
	 * @return The RepeatableSet object that is the parent of this survey item
	 * 		   or null if the survey item is not part of a repeatable set.
	 */
	public RepeatableSet getParent() {
		return parent;
	}
	
	/**
	 * Validates that a condition-value pair is valid for the given prompt.
	 * 
	 * @param pair The condition-value pair to validate.
	 * 
	 * @throws DomainException The value was not applicable for the prompt or
	 * 						   was invalid for some constraint on the prompt.
	 */
	public final void validateCondition(
			final ConditionValuePair pair)
			throws DomainException {

		
		// If the value is a valid NoResponse value, then it is acceptable to
		// compare against this prompt.
		if(checkNoResponseConditionValuePair(pair)) {
			return;
		}
		
		validateConditionValuePair(pair);
	}
	
	/**
	 * Returns whether or not this survey item may be skipped.
	 * 
	 * @return Whether or not this survey item may be skipped.
	 */
	public boolean skippable() {
		return false;
	}
	
	/**
	 * Generates a JSONObject that represents this survey item.
	 * 
	 * @return A JSONObject representing this survey item.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_ID, id);
		result.put(JSON_KEY_CONDITION, condition);
		result.put(JSON_KEY_INDEX, index);
		
		return result;
	}
	
	/**
	 * Returns the number of items contained in this survey item. For messages
	 * and prompts this will only be one, but for repeatable sets this may be
	 * many. Also, repeatable sets should return at least one indicating
	 * themselves.
	 * 
	 * @return The number of survey items that this survey item 
	 * 		   contains/represents.
	 */
	public abstract int getNumSurveyItems();
	
	/**
	 * Returns the number of prompts that may be displayed to a user from this
	 * abstract prompt.
	 * 
	 * @return The number of prompts that may be displayed to a user from this
	 * 		   abstract prompt.
	 */
	public abstract int getNumPrompts();
	
	/**
	 * Writes a Concordia definition of the prompt to the generator.
	 * 
	 * @param generator The generator to use to write the definition.
	 * 
	 * @throws JsonGenerationException There was an error generating the JSON.
	 * 
	 * @throws IOException There was an error writing to the generator.
	 */
	public abstract void toConcordia(
		final JsonGenerator generator)
		throws JsonGenerationException, IOException;

	/**
	 * Creates a hash code value for this survey item.
	 * 
	 * @return A hash code value for this survey item.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Determines if this survey item is equal to another object.
	 * 
	 * @return True if this survey item is logically equivalent to the other
	 * 		   object; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyItem other = (SurveyItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	/**
	 * Validates that a condition-value pair is valid for the given prompt. The
	 * check against {@link NoResponse} values has already been done, so this
	 * should check against type-specific survey items.
	 * 
	 * @param pair The condition-value pair to validate.
	 * 
	 * @throws DomainException The value was not applicable for the prompt or
	 * 						   was invalid for some constraint on the prompt.
	 */
	protected abstract void validateConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException;
	
	/**
	 * Sets the parent of this survey item which must be a repeatable set.
	 * 
	 * @param parent The repeatable set that contains this survey item.
	 */
	protected void setParent(final RepeatableSet parent) {
		this.parent = parent;
	}
	
	/**
	 * Checks if the value of the pair is a {@link NoResponse} value and, if 
	 * so, verifies that that is a valid value for this prompt. If it is a
	 * {@link NoResponse} value, then true will be returned or an exception 
	 * will be thrown indicating that it wasn't valid for this prompt. If it is
	 * not a {@link NoResponse} value, false is returned.
	 * 
	 * @param pair The condition-value pair to check.
	 * 
	 * @return True if this is a {@link NoResponse} value; false, otherwise.
	 * 
	 * @throws DomainException The value of the pair was a {@link NoResponse}
	 * 						   value and isn't valid for this prompt.
	 */
	protected final boolean checkNoResponseConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException {
		
		try {
			NoResponse noResponse = NoResponse.valueOf(pair.getValue().toUpperCase());
			
			switch(noResponse) {
			case SKIPPED:
				if(skippable()) {
					return true;
				}
				else {
					throw new DomainException(
							"The response '" + 
								getId() +
								"' cannot be skipped, so the condition is invalid.");
				}
			
			case NOT_DISPLAYED:
				return true;
				
			default:
				throw new DomainException("Unknown 'no response' value.");
			}
				
		}
		// It is not a NoResponse value.
		catch(IllegalArgumentException e) {
			return false;
		}
	}
}