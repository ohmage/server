package org.ohmage.domain;

/**
 * Simple wrapper class for wrapping an entity and an associated role.
 * 
 * @author John Jenkins
 */
public class EntityAndRole {
	private final String _entityId;
	private final String _role;
	
	/**
	 * Constructs a class and its document role.
	 * 
	 * @param entityId The entity's ID.
	 * 
	 * @param role The role of the entity.
	 */
	public EntityAndRole(String entityId, String role) {
		_entityId = entityId;
		_role = role;
	}
	
	/**
	 * Returns the entity's ID.
	 * 
	 * @return The entity's ID.
	 */
	public String getEntityId() {
		return _entityId;
	}
	
	/**
	 * Returns the entity's role.
	 * 
	 * @return The entity's role.
	 */
	public String getRole() {
		return _role;
	}
}
