package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks that each of the classes in the roster exist.
 * 
 * @author John Jenkins
 */
public class ClassesInClassRosterExistDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(ClassesInClassRosterExistDao.class);
	
	private static final String SQL_GET_CLASS_EXISTS = 
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	private final boolean _required;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 * 
	 * @param required Whether or not the roster must be present.
	 */
	public ClassesInClassRosterExistDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Parses each of the classes from the request and checks the database to
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
		
		// If the roster is empty, there are no classes to check.
		if(! "".equals(roster)) {
			// For each line...
			String[] rosterLines = roster.split("\n");
			for(int i = 0; i < rosterLines.length; i++) {
				// Get the values.
				String[] rosterLineValues = rosterLines[i].split(InputKeys.LIST_ITEM_SEPARATOR);
				
				// Get the class' ID.
				String classId = rosterLineValues[0];
				
				try {
					// If the class doesn't exist, fail the request.
					if(! (Boolean) getJdbcTemplate().queryForObject(SQL_GET_CLASS_EXISTS, new Object[] { classId }, Boolean.class)) {
						awRequest.setFailedRequest(true);
					}
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CLASS_EXISTS + "' with parameter: " + classId, e);
					throw new DataAccessException(e);
				}
			}
		}
	}
}