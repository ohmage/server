package edu.ucla.cens.awserver.request;

import java.util.List;

/**
 * State for requests that need use of a List (for query results, etc). 
 * 
 * @author selsky
 */
public class ResultListAwRequest extends AbstractAwRequest {
	// Output State
	private List<?> _resultList;

	public List<?> getResultList() {
		return _resultList;
	}

	public void setResultList(List<?> resultList) {
		_resultList = resultList;
	}

	@Override
	public String toString() {
		return "CampaignExistsAwRequest [_resultList=" + _resultList
				+ ", toString()=" + super.toString() + "]";
	}
}
