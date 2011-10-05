package org.ohmage.domain.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RepeatableSetResponse extends Response {
	public static final String REPEATABLE_SET_ID = "repeatable_set_id";
	public static final String SKIPPED = "skipped";
	public static final String NOT_DISPLAYED = "not_displayed";
	public static final String RESPONSES = "responses";
	
	private final RepeatableSet repeatableSet;
	
	private final Map<Integer, Map<Integer, Response>> responses;
	
	public RepeatableSetResponse(final RepeatableSet repeatableSet, 
			final NoResponse noResponse) {
		super(noResponse);
		
		this.repeatableSet = repeatableSet;
		
		responses = new HashMap<Integer, Map<Integer, Response>>();
	}
	
	public RepeatableSet getRepeatableSet() {
		return repeatableSet;
	}
	
	public void addResponseGroup(final int iteration, final Map<Integer, Response> responses) {
		if(this.responses.put(iteration, responses) != null) {
			throw new IllegalArgumentException("There is already a list of responses for that iteration.");
		}
	}
	
	public Map<Integer, Map<Integer, Response>> getResponseGroups() {
		return Collections.unmodifiableMap(responses);
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(REPEATABLE_SET_ID, repeatableSet.getId());
			// TODO: This is always true. This needs to be removed.
			result.put(SKIPPED, "true");
			
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
				}
				result.put(RESPONSES, repeatableSetResponses);
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}

	@Override
	public String getId() {
		return repeatableSet.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
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