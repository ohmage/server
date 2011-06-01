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
 * Composite map key with a date string and a userName string as the sortable field.
 * 
 * @author selsky
 */
public class UserDate implements Comparable<UserDate> {
	private String _userName;
	private String _date;
	
	public UserDate(String userName, String date) {
		_userName = userName;
		_date = date;
	}
	
	public String getUserName() {
		return _userName;
	}
	
	public String getDate() {
		return _date;
	}
	
	public int compareTo(UserDate userDate) {
		return _userName.compareTo(userDate._userName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_date == null) ? 0 : _date.hashCode());
		result = prime * result
				+ ((_userName == null) ? 0 : _userName.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "UserDate [_date=" + _date + ", _userName=" + _userName + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDate other = (UserDate) obj;
		if (_date == null) {
			if (other._date != null)
				return false;
		} else if (!_date.equals(other._date))
			return false;
		if (_userName == null) {
			if (other._userName != null)
				return false;
		} else if (!_userName.equals(other._userName))
			return false;
		return true;
	}
	
}
