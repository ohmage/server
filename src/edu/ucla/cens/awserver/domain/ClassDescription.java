package edu.ucla.cens.awserver.domain;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Basic information about a class.
 * 
 * @author John Jenkins
 */
public class ClassDescription {
	private String _urn;
	private String _name;
	private String _description;
	
	/**
	 * Builds a class information object.
	 *  
	 * @param urn The URN for this class. Cannot be null.
	 * 
	 * @param name The name of this class. Cannot be null.
	 * 
	 * @param description The description of this class. Can be null.
	 * 
	 * @throws IllegalArgumentException Thrown if the 'urn' or 'name' are null
	 * 									or empty.
	 */
	public ClassDescription(String urn, String name, String description) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(urn)) {
			throw new IllegalArgumentException("A class' URN cannot be null.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("A class' name cannot be null.");
		}
		
		_urn = urn;
		_name = name;
		_description = description;
	}
	
	/**
	 * The URN for this class.
	 * 
	 * @return The URN for this class.
	 */
	public String getUrn() {
		return _urn;
	}
	
	/**
	 * The name of this class.
	 * 
	 * @return The name of this class.
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * A description for this class. May be null.
	 * 
	 * @return A, possibly null, description for this class.
	 */
	public String getDescription() {
		return _description;
	}
}
