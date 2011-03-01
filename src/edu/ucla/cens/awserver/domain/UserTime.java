package edu.ucla.cens.awserver.domain;

/**
 * Combines a User with a time value for bin expiration.
 * 
 * @author selsky
 */
public class UserTime {
	private long _time;
	private User _user;
	
	public UserTime(User user, long time) {
		_user = user;
		_time = time;
	}
	
	public long getTime() {
		return _time;
	}
	
	public User getUser() {
		return _user;
	}
	
	public void setTime(long time) {
		_time = time;
	}
}
