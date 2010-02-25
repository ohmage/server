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

	@Override
	public String toString() {
		return "EmaVizQueryAwRequest [_endDate=" + _endDate + ", _startDate="
				+ _startDate + ", toString()=" + super.toString() + "]";
	}
}
