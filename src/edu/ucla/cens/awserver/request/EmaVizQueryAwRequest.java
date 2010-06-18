package edu.ucla.cens.awserver.request;


/**
 * State for EMA visualization queries.
 * 
 * @author selsky
 */
public class EmaVizQueryAwRequest extends ResultListAwRequest {
	// Input state
	private String _startDate;
	private String _endDate;
	private String _userNameRequestParam;
	
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
	
	public String getUserNameRequestParam() {
		return _userNameRequestParam;
	}

	public void setUserNameRequestParam(String userNameRequestParam) {
		_userNameRequestParam = userNameRequestParam;
	}

	@Override
	public String toString() {
		return "EmaVizQueryAwRequest [_endDate=" + _endDate + ", _startDate="
				+ _startDate + ", toString()=" + super.toString() + "]";
	}
}
