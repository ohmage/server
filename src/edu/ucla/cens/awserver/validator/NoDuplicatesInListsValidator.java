package edu.ucla.cens.awserver.validator;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Validates that no duplicates exist amongst a list both within the list
 * itself and against other lists.
 * 
 * @author John Jenkins
 */
public class NoDuplicatesInListsValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(NoDuplicatesInListsValidator.class);
	
	String[] _keys;
	
	/**
	 * Sets up the validator with an annotator if it fails and a list of keys
	 * to use when querying the request to get the actual lists to check.
	 * 
	 * @param annotator What to respond with should this validation fail.
	 * 
	 * @param keys The keys to use against the request object to get the lists
	 * 			   of users.
	 */
	public NoDuplicatesInListsValidator(AwRequestAnnotator annotator, String[] keys) {
		super(annotator);
		
		if((keys == null) || (keys.length == 0)) {
			_logger.error("Must include a list of keys.");
			throw new IllegalArgumentException("A list of keys to reference against the request is required.");
		}
		
		_keys = keys;
	}
	
	/**
	 * Gets the lists from the request based on the keys that this object was
	 * created with. It then checks the lists to ensure that that no
	 * duplicates exist in its own list or in any other lists. It doesn't
	 * validate or require that all of the lists exist, only that their
	 * contents don't overlap. 
	 * 
	 * The complexity of this function is O(n^2) where n is the number of
	 * names in all of the lists combined.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		// Get all the lists.
		List<String[]> listOfLists = new LinkedList<String[]>();
		for(int i = 0; i < _keys.length; i++) {
			try {
				listOfLists.add(((String) awRequest.getToProcessValue(_keys[i])).split(","));
			}
			catch(IllegalArgumentException iae) {
				// If it wasn't in the toProcess map then maybe it hasn't had
				// any validation done on it yet, so get it from the
				// toValidate map.
				try {
					listOfLists.add(((String) awRequest.getToValidate().get(_keys[i])).split(","));
				}
				catch(NullPointerException e) {
					// This means it wasn't in either map which is acceptable
					// because it could be that it is an optional list. It is
					// not this validator's duty to check if lists exist, only
					// that, if they do that their content's don't overlap.
				}
			}
		}
		
		// Break the lists into arrays for faster referencing.
		String[][] lists = new String[listOfLists.size()][];
		int currIndex = 0;
		for(String[] currList : listOfLists) {
			lists[currIndex] = currList;
			currIndex++;
		}
		
		// Check for duplicates.
		for(int i = 0; i < lists.length; i++) {
			for(int j = 0; j < lists[i].length; j++) {
				String currItem = lists[i][j];
				
				// First, check that it doesn't exist in the rest of the list.
				for(int x = j+1; x < lists[i].length; x++) {
					if(currItem.equals(lists[i][x])) {
						getAnnotator().annotate(awRequest, "Duplicate entry found in list " + _keys[i] + " at index " + j + " and index " + x + ": " + currItem);
						awRequest.setFailedRequest(true);
						return false;
					}
				}
				
				// Then, check that it doesn't exist in any of the other lists.
				for(int k = i+1; k < lists.length; k++) {
					for(int x = 0; x < lists[k].length; x++) {
						if(currItem.equals(lists[k][x])) {
							getAnnotator().annotate(awRequest, "Duplicate entry found in list " + _keys[i] + " at index " + j + " and in list " + _keys[k] + " at index " + x + ": " + currItem);
							awRequest.setFailedRequest(true);
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
}
