package edu.ucla.cens.awserver.request;

/**
 * State for mobility data point API queries.
 * 
 * @author selsky
 */
public class MobilityQueryAwRequest extends ResultListAwRequest {
	private String _startDate;
	private String _endDate;
	private String _userNameRequestParam;
	private String _client;
	
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

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}
}
