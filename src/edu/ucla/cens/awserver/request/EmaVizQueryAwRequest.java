package edu.ucla.cens.awserver.request;

import java.util.List;

/**
 * 
 * @author selsky
 */
public class EmaVizQueryAwRequest extends AbstractAwRequest {
	private String _startDate;
	private String _endDate;
	
	
	private List<?> _resultList;
	
	public String getStartDate() {
		return _startDate;
	}
	
	public void setStartDate(String startDate) {
		_startDate = startDate;
	}
	
	public String getEndDate() {
		return _endDate;
	}
	
	public void setEndDate(String endDate) {
		_endDate = endDate;
	}
	
	public List<?> getResultList() {
		return _resultList;
	}
	
	public void setResultList(List<?> resultList) {
		_resultList = resultList;
	}

	@Override
	public String toString() {
		return "EmaVizQueryAwRequest [_endDate=" + _endDate + ", _resultList="
				+ _resultList + ", _startDate=" + _startDate + ", toString()="
				+ super.toString() + "]";
	}
	
}
