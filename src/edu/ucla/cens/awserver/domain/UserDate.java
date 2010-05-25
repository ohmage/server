package edu.ucla.cens.awserver.domain;

/**
 * Composite map key with a date string and a userName string as the sortable field.
 * 
 * @author selsky
 */
public class UserDate implements Comparable<UserDate> {
	private String _userName;
	private String _date;
	
	public UserDate(String userName, String date) {
		_userName = userName;
		_date = date;
	}
	
	public String getUserName() {
		return _userName;
	}
	
	public String getDate() {
		return _date;
	}
	
	public int compareTo(UserDate userDate) {
		return _userName.compareTo(userDate._userName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_date == null) ? 0 : _date.hashCode());
		result = prime * result
				+ ((_userName == null) ? 0 : _userName.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "UserDate [_date=" + _date + ", _userName=" + _userName + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDate other = (UserDate) obj;
		if (_date == null) {
			if (other._date != null)
				return false;
		} else if (!_date.equals(other._date))
			return false;
		if (_userName == null) {
			if (other._userName != null)
				return false;
		} else if (!_userName.equals(other._userName))
			return false;
		return true;
	}
	
}
