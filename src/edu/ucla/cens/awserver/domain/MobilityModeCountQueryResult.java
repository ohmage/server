package edu.ucla.cens.awserver.domain;

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
