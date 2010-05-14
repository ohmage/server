package edu.ucla.cens.awserver.domain;

/**
 * Bare bones representation of an AW user. 
 * 
 * @author selsky
 */
public class SimpleUser {
	private int _id;
	private String _userName;
	
	public int getId() {
		return _id;
	}
	
	public void setId(int id) {
		_id = id;
	}
	
	public String getUserName() {
		return _userName;
	}
	
	public void setUserName(String userName) {
		_userName = userName;
	}
}
