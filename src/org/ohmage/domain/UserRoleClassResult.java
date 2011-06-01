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


public class UserRoleClassResult {
	private String _urn;
	private String _name;
	private String _description;
	private String _role;
	
	public UserRoleClassResult(String urn, String name, String description, String role) {
		_urn = urn;
		_name = name;
		_description = description;
		_role = role;
	}
	
	public String getUrn() {
		return _urn;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getDescription() {
		return _description;
	}

	public String getRole() {
		return _role;
	}
}
