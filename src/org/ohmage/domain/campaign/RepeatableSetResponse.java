package org.ohmage.domain.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a response for a repeatable set. This includes all of
 * the responses for its prompts and/or sub repeatable sets.
 * 
 * @author John Jenkins
 */
public class RepeatableSetResponse extends Response {
	public static final String REPEATABLE_SET_ID = "repeatable_set_id";
	public static final String SKIPPED = "skipped";
	public static final String NOT_DISPLAYED = "not_displayed";
	public static final String RESPONSES = "responses";
	
	private final RepeatableSet repeatableSet;
	
	private final Map<Integer, Map<Integer, Response>> responses;
	
	/**
	 * Creates a new repeatable set response. 
	 * 
	 * @param repeatableSet The repeatable set from which these responses were
	 * 						generated.
	 * 
	 * @param noResponse If the repeatable set has no responses, this will give
	 * 					 the reason why.
	 * 
	 * @throws IllegalArgumentException Thrown if the repeatable set is null.
	 */
	public RepeatableSetResponse(final RepeatableSet repeatableSet, 
			final NoResponse noResponse) {
		super(noResponse);
		
		if(repeatableSet == null) {
			throw new IllegalArgumentException("The repeatable set is null.");
		}
		this.repeatableSet = repeatableSet;
		
		responses = new HashMap<Integer, Map<Integer, Response>>();
	}
	
	/**
	 * Returns the repeatable set from which these responses originated.
	 * 
	 * @return A RepeatableSet object representing the repeatable set from 
	 * 		   which these responses were generated.
	 */
	public RepeatableSet getRepeatableSet() {
		return repeatableSet;
	}
	
	/**
	 * Adds an iteration of responses to the response group. 
	 * 
	 * @param iteration The iteration of the repeatable set to which these
	 * 					responses belong.
	 * 
	 * @param responses A map of response indices to their response.
	 * 
	 * @throws IllegalArgumentException Thrown if there already exists a map of
	 * 									responses for that iteration.
	 */
	public void addResponseGroup(final int iteration, final Map<Integer, Response> responses) {
		if(this.responses.containsKey(iteration)) {
			throw new IllegalArgumentException("There is already a list of responses for that iteration.");
		}
		
		this.responses.put(iteration, Collections.unmodifiableMap(responses));
	}
	
	/**
	 * Returns an unmodifiable map of iteration to map of index to response.
	 * 
	 * @return An unmodifiable map. The key on the map is the iteration number.
	 * 		   The value for that iteration key is a map of prompt index to
	 * 		   Response objects. 
	 */
	public Map<Integer, Map<Integer, Response>> getResponseGroups() {
		return Collections.unmodifiableMap(responses);
	}

	/**
	 * Returns a JSONObject that represents the values of the responses in this
	 * repeatable set as well as the repeatable set itself.
	 * 
	 * @return A JSONObject representing this repeatable set and all of the
	 * 		   prompt responses contained within it.
	 */
	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(REPEATABLE_SET_ID, repeatableSet.getId());
			// TODO: This is always true. This needs to be removed.
			result.put(SKIPPED, true);
			
			if(wasNotDisplayed()) {
				result.put(NOT_DISPLAYED, true);
			}
			else {
				result.put(NOT_DISPLAYED, false);

				List<Integer> iterations = new ArrayList<Integer>(responses.keySet());
				Collections.sort(iterations);

				JSONArray repeatableSetResponses = new JSONArray();
				for(Integer iteration : iterations) {
					Map<Integer, Response> iterationResponses = responses.get(iteration);
					List<Integer> indices = new ArrayList<Integer>(iterationResponses.keySet());
					Collections.sort(indices);
					
					JSONArray iterationResponse = new JSONArray();
					for(Integer index : indices) {
						iterationResponse.put(iterationResponses.get(index).toJson());
					}
					
					repeatableSetResponses.put(iterationResponse);
				}
				result.put(RESPONSES, repeatableSetResponses);
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}

	/**
	 * Returns the unique identifier for the repeatable set.
	 * 
	 * @return The unique identifier for the repeatable set.
	 */
	@Override
	public String getId() {
		return repeatableSet.getId();
	}

	/**
	 * Generates a hash code for this repeatable set response.
	 * 
	 * @return A hash code for this repeatable set response.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((repeatableSet == null) ? 0 : repeatableSet.hashCode());
		result = prime * result
				+ ((responses == null) ? 0 : responses.hashCode());
		return result;
	}

	/**
	 * Determines if this repeatable set response is logically equivalent to
	 * another object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this repeatable set response is logically equivalent to
	 * 		   the other object; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepeatableSetResponse other = (RepeatableSetResponse) obj;
		if (repeatableSet == null) {
			if (other.repeatableSet != null)
				return false;
		} else if (!repeatableSet.equals(other.repeatableSet))
			return false;
		if (responses == null) {
			if (other.responses != null)
				return false;
		} else if (!responses.equals(other.responses))
			return false;
		return true;
	}
}