package edu.ucla.cens.awserver.domain;

/**
 * Default UserRole implementation.
 * 
 * @author selsky
 */
public class UserRoleImpl implements UserRole {
	private int _id;
	private String _role;
	
	@Override
	public int getId() {
		return _id;
	}

	@Override
	public String getRole() {
		return _role;
	}

	@Override
	public void setId(int id) {
		_id = id;
	}

	@Override
	public void setRole(String role) {
		if(null == role) {
			throw new IllegalArgumentException("a role is required");
		}
		
		_role = role;
	}
}
