package edu.ucla.cens.awserver.domain;

/**
 * Definition of user roles.
 * 
 * @author selsky
 */
public interface UserRole {

	int getId();
	void setId(int id);
	
	String getName();
	void setName(String name);
}
