package edu.ucla.cens.awserver.domain;

/**
 * Definition of user roles.
 * 
 * @author selsky
 */
public interface UserRole {

	int getId();
	void setId(int id);
	
	String getRole();
	void setRole(String role);
}
