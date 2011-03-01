package edu.ucla.cens.awserver.request;

import java.util.List;

/**
 * State for requests that need use of a List (for query results, etc). 
 * 
 * @author selsky
 */
public class ResultListAwRequest extends AbstractAwRequest {
	private String  _userToken;
	private List<?> _resultList;

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
				+ ", toString()=" + super.toString() + "]";
	}
}
