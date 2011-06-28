package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassRosterUpdateRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ClassRosterUpdateDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(ClassRosterUpdateDao.class);
	
	private static final String SQL_GET_USER_ID = 
		"SELECT id " +
		"FROM user " +
		"WHERE username = ?";
	
	private static final String SQL_GET_CLASS_ID =
		"SELECT id " +
		"FROM class " +
		"WHERE urn = ?";
	
	private static final String SQL_INSERT_USER_CLASS =
		"INSERT INTO user_class(class_id, user_id, user_class_role_id) " +
		"VALUES (?,?,?)";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public ClassRosterUpdateDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets teh
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the roster value from the request.
		String roster;
		try {
			roster = (String) awRequest.getToProcessValue(InputKeys.ROSTER);
		}
		catch(IllegalArgumentException outerException) {
			throw new DataAccessException("Missing required key: " + InputKeys.ROSTER);
		}
		
		_logger.info("Adding all of the users in the roster to each of the classes in the roster with the specified role.");
		
		// If the roster is empty, there are no user-class relationships to 
		// add.
		JSONArray warningMessages = new JSONArray();
		if(! "".equals(roster)) {
			// Create the transaction.
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("Updating user-class relationships via a roster.");
			
			try {
				// Begin the transaction.
				PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
				TransactionStatus status = transactionManager.getTransaction(def);
				
				// For each line...
				String[] rosterLines = roster.split("\n");
				for(int i = 0; i < rosterLines.length; i++) {
					// Get the values.
					String[] rosterLineValues = rosterLines[i].split(InputKeys.LIST_ITEM_SEPARATOR);
					
					// Get the class' ID.
					long classId;
					try {
						classId = getJdbcTemplate().queryForLong(SQL_GET_CLASS_ID, new Object[] { rosterLineValues[0] });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parameter: " + rosterLineValues[0], e);
						transactionManager.rollback(status);
						throw new DataAccessException(e);
					}
					
					// Get the user's ID.
					long userId;
					try {
						userId = getJdbcTemplate().queryForLong(SQL_GET_USER_ID, new Object[] { rosterLineValues[1] });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameter: " + rosterLineValues[1], e);
						transactionManager.rollback(status);
						throw new DataAccessException(e);
					}
					
					// Get the database ID for the class role.
					long classRoleId;
					try {
						classRoleId = ClassRoleCache.instance().lookup(rosterLineValues[2]);
					}
					catch(CacheMissException e) {
						_logger.error("Unknown class role: " + rosterLineValues[2], e);
						transactionManager.rollback(status);
						throw new DataAccessException(e);
					}
					
					// Add the user to the class.
					try {
						getJdbcTemplate().update(SQL_INSERT_USER_CLASS, new Object[] { classId, userId, classRoleId });
					}
					catch(org.springframework.dao.DataIntegrityViolationException e) {
						warningMessages.put(rosterLineValues[1] + " is already associated with the class " + rosterLineValues[0]);
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_INSERT_USER_CLASS + "' with parameters: " + 
								classId + ", " + userId + ", " + classRoleId, e);
						transactionManager.rollback(status);
						throw new DataAccessException(e);
					}
				}
					
				// Commit the transaction.
				try {
					transactionManager.commit(status);
				}
				catch(TransactionException e) {
					_logger.error("Error while committing the transaction.", e);
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
			}
			catch(TransactionException e) {
				_logger.error("Error while attempting to rollback the transaction.");
				throw new DataAccessException(e);
			}
		}
		
		awRequest.addToReturn(ClassRosterUpdateRequest.KEY_WARNING_MESSAGES, warningMessages, true); 
	}
}