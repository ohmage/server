package edu.ucla.cens.awserver.domain;


public class UserRoleClassResult {
	private String _urn;
	private String _name;
	private String _description;
	private String _role;
	
	public UserRoleClassResult(String urn, String name, String description, String role) {
		_urn = urn;
		_name = name;
		_description = description;
		_role = role;
	}
	
	public String getUrn() {
		return _urn;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getDescription() {
		return _description;
	}

	public String getRole() {
		return _role;
	}
}
