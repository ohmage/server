package edu.ucla.cens.awserver.domain;

/**
 * Default UserRole implementation.
 * 
 * @author selsky
 */
public class UserRoleImpl implements UserRole {
	private int _id;
	private String _name;
	
	@Override
	public int getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public void setId(int id) {
		_id = id;
	}

	@Override
	public void setName(String name) {
		if(null == name) {
			throw new IllegalArgumentException("a name is required");
		}
		
		_name = name;
	}
}
