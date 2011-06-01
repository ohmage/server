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

/**
 * Container for results from the successful location update query (or any other feature that needs a userName-percentage holder). 
 * 
 * @author selsky
 */
public class UserPercentage {
	private String _userName;
	private double _percentage;
	
	public UserPercentage(String userName, double percentage) {
		_userName = userName;
		_percentage = percentage;
	}
	
	public String getUserName() {
		return _userName;
	}
	
	public double getPercentage() {
		return _percentage;
	}

	@Override
	public String toString() {
		return "UserPercentage [_percentage=" + _percentage + ", _userName="
				+ _userName + "]";
	}
}
