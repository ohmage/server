package edu.ucla.cens.awserver.domain;

/**
 * Composite map key with a date string and a userName string as the sortable field.
 * 
 * @author selsky
 */
public class UserDate implements Comparable<UserDate> {
	private String _userName;
	private String _date;
	
	public String getUserName() {
		return _userName;
	}
	
	public void setUserName(String userName) {
		_userName = userName;
	}
	
	public String getDate() {
		return _date;
	}
	
	public void setDate(String date) {
		_date = date;
	}
	
	public int compareTo(UserDate userDate) {
		return _userName.compareTo(userDate._userName);
	}
}
