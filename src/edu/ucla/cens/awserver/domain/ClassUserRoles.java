package edu.ucla.cens.awserver.domain;

import java.util.LinkedList;
import java.util.List;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Combines a class and a role for a user.
 * 
 * @author John Jenkins
 */
public class ClassUserRoles {
	private ClassDescription _class;
	private List<String> _roles;
	
	/**
	 * Creates a new object with a class and no roles.
	 * 
	 * @param classInfo A Class object that has information about a class.
	 * 
	 * @throws IllegalArgumentException Thrown if the class information object
	 * 									is null.
	 */
	public ClassUserRoles(ClassDescription classInfo) throws IllegalArgumentException {
		if(classInfo == null) {
			throw new IllegalArgumentException("The information about the class cannot be null.");
		}
		
		_class = classInfo;
		_roles = new LinkedList<String>();
	}
	
	/**
	 * Returns the class to which these roles are associated.
	 * 
	 * @return The class to which these roles are associated.
	 */
	public ClassDescription getUserClass() {
		return _class;
	}
	
	/**
	 * Adds a role to the list of roles.
	 * 
	 * @param role The role to be added to the list of roles.
	 * 
	 * @throws IllegalArgumentException Thrown if the role is null or
	 * 									whitespace only.
	 */
	public void addRole(String role) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			throw new IllegalArgumentException("The role cannoe be null or whitespace only.");
		}
		
		_roles.add(role);
	}
	
	/**
	 * Adds a list of roles to the current list of roles.
	 * 
	 * @param roles The roles to add to the current user.
	 * 
	 * @throws IllegalArgumentException Thrown if the list of roles is null.
	 */
	public void addRoles(List<String> roles) throws IllegalArgumentException {
		if(roles == null) {
			throw new IllegalArgumentException("The list of roles cannot be null.");
		}
		
		_roles.addAll(roles);
	}
	
	/**
	 * Returns the role for this user in this class.
	 * 
	 * @return The role for this user in this class.
	 */
	public List<String> getRoles() {
		return _roles;
	}
}
