package edu.ucla.cens.awserver.domain;

/**
 * Default immutable UserRole implementation.
 * 
 * @author selsky
 */
public class UserRoleImpl implements UserRole {
	private int _id;
	private String _role;
	
	public UserRoleImpl(int id, String role) {
		_id = id;
		_role = role;
	}
	
	@Override
	public int getId() {
		return _id;
	}

	@Override
	public String getRole() {
		return _role;
	}	
}
