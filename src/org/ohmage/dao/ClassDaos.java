package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.domain.ClassInformation;
import org.ohmage.validator.UserClassValidators.UserAndRole;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting classes. While it may read information pertaining to
 * other entities, the information it takes and provides should pertain to 
 * classes only with the exception of linking other entities to classes such as
 * class update which needs to be able to associate users with a class in a
 * single transaction.
 * 
 * @author John Jenkins
 */
public class ClassDaos extends Dao {
	// Returns a boolean as to whether or not the given class exists.
	private static final String SQL_EXISTS_CLASS = 
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	// Returns the class' information and the a user's role in that class.
	private static final String SQL_GET_CLASS_INFO_AND_USER_ROLE = 
		"SELECT c.urn, c.name, c.description, ucr.role " +
		"FROM user u, class c, user_class uc, user_class_role ucr " +
		"WHERE c.urn = ? " +
		"AND c.id = uc.class_id " +
		"AND uc.user_id = u.id " +
		"AND u.username = ? " +
		"AND uc.user_class_role_id = ucr.id ";

	// Gets all of the users and their class role for a single class.
	private static final String SQL_GET_USERS_AND_CLASS_ROLES = 
		"SELECT u.username, ucr.role " +
		"FROM user u, user_class uc, user_class_role ucr, class c " +
		"WHERE u.id = uc.user_id " +
		"AND c.id = uc.class_id " + 
		"AND ucr.id = uc.user_class_role_id " +
		"AND c.urn = ?";
	
	// Inserts a new class.
	private static final String SQL_INSERT_CLASS =
		"INSERT INTO class(urn, name, description) " +
		"VALUES (?,?,?)";
	
	// Associates a user with a class.
	private static final String SQL_INSERT_ADD_USER_TO_CLASS = 
		"INSERT INTO user_class(user_id, class_id, user_class_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), (" +
				"SELECT id " +
				"FROM class " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_class_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Updates a class' name.
	private static final String SQL_UPDATE_CLASS_NAME =
		"UPDATE class " +
		"SET name = ? " +
		"WHERE urn = ?";
	
	// Updates a class' description.
	private static final String SQL_UPDATE_CLASS_DESCRIPTION = 
		"UPDATE class " +
		"SET description = ? " +
		"WHERE urn = ?";
	
	// Deletes a class.
	private static final String SQL_DELETE_CLASS = 
		"DELETE FROM class " + 
		"WHERE urn = ?";
	
	// Deletes a user from a class if they have the specified role.
	private static final String SQL_DELETE_USER_FROM_CLASS_WITH_ROLE =
		"DELETE FROM user_class " +
		"WHERE user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		") " +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		") " +
		"AND user_class_role_id = (" +
			"SELECT id " +
			"FROM user_class_role " +
			"WHERE role = ?" +
		")";
	
	/**
	 * Inner class for gathering a class' information and the role of the user
	 * that is aggregating this information.
	 * 
	 * @author John Jenkins
	 */
	private static final class ClassInformationAndUserRole {
		private final ClassInformation classInformation;
		private final String role;
		
		/**
		 * Convenience constructor.
		 * 
		 * @param classInformation A ClassInformation object that contains all
		 * 						   the information for a class.
		 * 
		 * @param role The role of the requesting user in this class.
		 */
		private ClassInformationAndUserRole(ClassInformation classInformation, String role) {
			this.classInformation = classInformation;
			this.role = role;
		}
	}
	
	/**
	 * Inner class for aggregating a username and class role for a class.
	 * 
	 * @author John Jenkins
	 */
	private static final class UserAndClassRole {
		private final String username;
		private final String role;
		
		/**
		 * Convenience constructor.
		 * 
		 * @param username The username of a member of a class.
		 * 
		 * @param role The user's role in the class.
		 */
		private UserAndClassRole(String username, String role) {
			this.username = username;
			this.role = role;
		}
	}

	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static ClassDaos instance;
	
	/**
	 * Sets up this DAO with a shared DataSource to use. This is called from
	 * Spring and is an error to call within application code.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	public ClassDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Creates a new class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param className The name of the class.
	 * 
	 * @param classDescription An optional description of the class. This may
	 * 						   be null.
	 * 
	 * @throws DataAccessException Thrown if there is an error executing any of
	 * 							   the SQL.
	 */
	public static void createClass(String classId, String className, String classDescription) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the class.
			try {
				instance.jdbcTemplate.update(SQL_INSERT_CLASS, new Object[] { classId, className, classDescription });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_CLASS + "' with parameters: " +
						classId + ", " + className + ", " + classDescription, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Queries the database to see if a class exists.
	 * 
	 * @param classId The ID of the class in question.
	 * 
	 * @return Whether or not the class exists.
	 */
	public static Boolean getClassExists(String classId) {
		try {
			return (Boolean) instance.jdbcTemplate.queryForObject(SQL_EXISTS_CLASS, new Object[] { classId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_CLASS + "' with parameters: " + classId, e);
		}
	}
	
	/**
	 * Aggregates the information about a class as well as a list of users and
	 * their roles in the class for a list of classes.
	 * 
	 * @param classIds The list of class IDs whose information, users, and
	 * 				   users' roles are desired.
	 * 
	 * @param requester The username of the user who is making this request.
	 * 
	 * @return A List of ClassInformation objects correlating to the 
	 * 		   parameterized list of class IDs. This may be an empty list, but
	 * 		   it will never be null.
	 */
	public static List<ClassInformation> getClassesInformation(List<String> classIds, String requester) {
		List<ClassInformation> result = new LinkedList<ClassInformation>();
		
		for(String classId : classIds) {
			// Get the class' information and the user's role in the class.
			String userRole;
			ClassInformation classInformation;
			try {
				ClassInformationAndUserRole classInformationAndUserRole = instance.jdbcTemplate.queryForObject(
						SQL_GET_CLASS_INFO_AND_USER_ROLE, 
						new Object[] { classId, requester }, 
						new RowMapper<ClassInformationAndUserRole>() {
							@Override
							public ClassInformationAndUserRole mapRow(ResultSet rs, int row) throws SQLException {
								ClassInformationAndUserRole result = new ClassInformationAndUserRole(
										new ClassInformation(
												rs.getString("urn"),
												rs.getString("name"),
												rs.getString("description")),
										rs.getString("role")
								);
								return result;
							}
						}
				);
				
				classInformation = classInformationAndUserRole.classInformation;
				userRole = classInformationAndUserRole.role;
			}
			catch(org.springframework.dao.DataAccessException e){
				throw new DataAccessException("Error executing SQL_EXISTS_CLASS '" + SQL_GET_CLASS_INFO_AND_USER_ROLE + "' with parameters: " + 
						classId + ", " + requester, e);
			}
			
			boolean includeUserRoles = ClassRoleCache.ROLE_PRIVILEGED.equals(userRole);
			
			// Get all the users in this class and their class role.
			List<UserAndClassRole> usersAndRole;
			try {
				usersAndRole = instance.jdbcTemplate.query(
						SQL_GET_USERS_AND_CLASS_ROLES, 
						new Object[] { classId }, 
						new RowMapper<UserAndClassRole>() {
							@Override
							public UserAndClassRole mapRow(ResultSet rs, int row) throws SQLException {
								return new UserAndClassRole(rs.getString("username"), rs.getString("role"));
							}
						}
				);
			}
			catch(org.springframework.dao.DataAccessException e){
				throw new DataAccessException("Error executing SQL_EXISTS_CLASS '" + SQL_GET_USERS_AND_CLASS_ROLES + "' with parameter: " + classId, e);
			}
			
			// For each of the users add them to the current classes 
			// information object.
			for(UserAndClassRole userInformation : usersAndRole) {
				classInformation.addUser(userInformation.username, ((includeUserRoles) ? userInformation.role : ""));
			}
			
			// Add the class information to the list to be returned.
			result.add(classInformation);
		}
		
		return result;
	}
	
	private final static Logger LOGGER = Logger.getLogger(ClassDaos.class);
	
	/**
	 * Updates a class' information and adds and removes users from the class
	 * all as requested.
	 * 
	 * @param classId The class identifier to use to lookup which class to 
	 * 				  update.
	 * 
	 * @param className The class' new name or null in which case the name will
	 * 					not be updated.
	 * 
	 * @param classDescription The class' new description or null in which case
	 * 						   the description will not be updated.
	 *  
	 * @param usersToAdd A list of users and respective roles to associate with
	 * 					 this class.
	 * 
	 * @param usersToRemove A list of users and respective roles to remove from
	 * 						this class.
	 */
	public static void updateClass(String classId, String className, String classDescription, List<UserAndRole> usersToAdd, List<UserAndRole> usersToRemove) {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the name if it's not null.
			if(className != null) {
				try {
					instance.jdbcTemplate.update(SQL_UPDATE_CLASS_NAME, new Object[] { className, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error while executing SQL '" + SQL_UPDATE_CLASS_NAME + "' with parameters: " + 
							className + ", " + classId, e);
				}
			}
			
			// Update the name if it's not null.
			if(classDescription != null) {
				try {
					instance.jdbcTemplate.update(SQL_UPDATE_CLASS_DESCRIPTION, new Object[] { classDescription, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error while executing SQL '" + SQL_UPDATE_CLASS_DESCRIPTION + "' with parameters: " + 
							classDescription + ", " + classId, e);
				}
			}
			
			// Delete the users before adding the new ones. This facilitates
			// upgrading a user from one role to another.
			if(usersToRemove != null) {
				for(UserAndRole userAndRole : usersToRemove) {
					try {
						instance.jdbcTemplate.update(
								SQL_DELETE_USER_FROM_CLASS_WITH_ROLE, 
								new Object[] { 
									userAndRole.getUsername(), 
									classId,
									userAndRole.getRole() 
									}
								);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error while executing SQL '" + SQL_DELETE_USER_FROM_CLASS_WITH_ROLE + "' with parameters: " + 
								userAndRole.getUsername() + ", " + classId + ", " + userAndRole.getRole(), e);
					}
				}
			}
			
			// Add the users to the class.
			if(usersToAdd != null) {
				for(UserAndRole userAndRole : usersToAdd) {
					try {
						instance.jdbcTemplate.update(
								SQL_INSERT_ADD_USER_TO_CLASS,
								new Object[] {
									userAndRole.getUsername(),
									classId,
									userAndRole.getRole()
								}
							);
					}
					catch(org.springframework.dao.DataIntegrityViolationException e) {
						LOGGER.warn("Attempting to add a user to the class to which they are already associated. The following user is already associated with the class: " + userAndRole.getUsername());
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_ADD_USER_TO_CLASS + "' with parameters: " + 
								userAndRole.getUsername() + ", " + classId + ", " + userAndRole.getRole(), e);
					}
				}
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Deletes a class.
	 * 
	 * @param classId The unique identifier for the class to be deleted.
	 */
	public static void deleteClass(String classId) {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a new class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				instance.jdbcTemplate.update(SQL_DELETE_CLASS, new Object[] { classId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_DELETE_CLASS + "' with parameter: " + classId, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
}