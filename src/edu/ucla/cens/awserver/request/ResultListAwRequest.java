package edu.ucla.cens.awserver.request;

import java.util.LinkedList;
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
		_resultList = new LinkedList<Object>();
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
