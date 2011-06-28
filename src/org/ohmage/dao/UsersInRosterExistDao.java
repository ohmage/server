package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Validates that each of the users in the roster exist.
 * 
 * @author John Jenkins
 */
public class UsersInRosterExistDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UsersInRosterExistDao.class);
	
	private static final String SQL_GET_USER_EXISTS = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	private final boolean _required;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 * 
	 * @param required Whether or not the roster must be present.
	 */
	public UsersInRosterExistDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Parses each of the usernames from the request and checks the database to
	 * ensure that they all exist.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the roster value from the request.
		String roster;
		try {
			roster = (String) awRequest.getToProcessValue(InputKeys.ROSTER);
		}
		catch(IllegalArgumentException outerException) {
			if(_required) {
				throw new DataAccessException("Missing required key: " + InputKeys.ROSTER);
			}
			else {
				return;
			}
		}
		
		// If the roster is empty, there are no usernames to check.
		if(! "".equals(roster)) {
			// For each line...
			String[] rosterLines = roster.split("\n");
			for(int i = 0; i < rosterLines.length; i++) {
				// Get the values.
				String[] rosterLineValues = rosterLines[i].split(InputKeys.LIST_ITEM_SEPARATOR);
				
				// Get the username.
				String username = rosterLineValues[1];
				
				try {
					// If the user doesn't exist, fail the request.
					if(! (Boolean) getJdbcTemplate().queryForObject(SQL_GET_USER_EXISTS, new Object[] { username }, Boolean.class)) {
						awRequest.setFailedRequest(true);
					}
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USER_EXISTS + "' with parameter: " + username, e);
					throw new DataAccessException(e);
				}
			}
		}
	}
}