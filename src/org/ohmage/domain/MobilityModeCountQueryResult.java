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
 * @author selsky
 */
public class MobilityModeCountQueryResult {
	private String _mode;
	private String _date;
	private String _userName;
	private int _count;
	private boolean _empty;
	
	public String getMode() {
		return _mode;
	}
	
	public void setMode(String mode) {
		_mode = mode;
	}
	
	public String getDate() {
		return _date;
	}
	
	public void setDate(String date) {
		_date = date;
	}
	
	public String getUserName() {
		return _userName;
	}
	
	public void setUserName(String userName) {
		_userName = userName;
	}

	public int getCount() {
		return _count;
	}

	public void setCount(int count) {
		_count = count;
	}

	public boolean isEmpty() {
		return _empty;
	}

	public void setEmpty(boolean empty) {
		_empty = empty;
	}
}
