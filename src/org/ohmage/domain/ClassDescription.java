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
package org.ohmage.domain;

import org.ohmage.util.StringUtils;

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
