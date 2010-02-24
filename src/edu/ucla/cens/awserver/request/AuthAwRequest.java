package edu.ucla.cens.awserver.request;

import java.util.List;


/**
 * 
 * @author selsky
 */
public class AuthAwRequest extends AbstractAwRequest {
	private List<?> _resultList;

	public List<?> getResultList() {
		return _resultList;
	}

	public void setResultList(List<?> resultList) {
		_resultList = resultList;
	}

	@Override
	public String toString() {
		return "AuthAwRequest [_resultList=" + _resultList + ", toString()="
				+ super.toString() + "]";
	}
	
}
