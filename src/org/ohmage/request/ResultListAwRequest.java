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
package org.ohmage.request;

import java.util.Collections;
import java.util.List;

/**
 * State for requests that need use of a List (for query results, etc). 
 * 
 * @author selsky
 */
public class ResultListAwRequest extends AbstractAwRequest {
	private String  _userToken;
	private List<?> _resultList;
	
	/**
	 * Basic constructor that sets the '_resultList' to an empty linked list.
	 * This is done as it is believed to be better to return empty lists than
	 * null.
	 */
	public ResultListAwRequest() {
		super();
		
		_userToken = null;
		_resultList = Collections.emptyList();
	}

	public List<?> getResultList() {
		return _resultList;
	}

	public void setResultList(List<?> resultList) {
		_resultList = resultList;
	}

	public String getUserToken() {
		return _userToken;
	}
	
	public void setUserToken(String userToken) {
		_userToken = userToken;
	}
	
	@Override
	public String toString() {
		return "ResultsListAwRequest [_resultList=" + _resultList
				+ ", toString()=" + super.toString()
				+ "]";
	}
}
