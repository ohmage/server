package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * DAO for checking the list of classes associated with a campaign creation
 * request. Checks to ensure that the user has the ability to create campaigns
 * associated with the list of classes and that the classes exist.
 * 
 * @author John Jenkins
 */
public class ClassListValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(ClassListValidationDao.class);
	
	private static final String SQL_CLASS_COUNT = "SELECT EXISTS(" +
												  	"SELECT * " +
												  	"FROM class " +
												  	"WHERE urn = ?" +
												  ")";
	
	private static final String SQL_USER_IN_CLASS = "SELECT EXISTS(" +
														"SELECT * " +
														"FROM user u, class c, user_class uc " +
														"WHERE c.urn = ? " +
														"AND u.login_id = ? " +
														"AND c.id = uc.class_id " +
														"AND u.id = uc.user_id" +
													")";
	
	private boolean _required;
	
	/**
	 * Sets up the data source for this DAO.
	 * 
	 * @param dataSource The data source that will be used to query the
	 * 					 database for information.
	 */
	public ClassListValidationDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Checks to ensure that this user has the appropriate permissions to
	 * create a class associated with the list of classes and that those
	 * classes exist.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of classes.
		String classesAsString;
		try {
			classesAsString = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				_logger.error("Missing required class URN list in toProcess map.");
				throw new DataAccessException(e);
			}
			else {
				_logger.info("No class list in the toProcess map, so skipping service validation.");
				return;
			}
		}
		
		// For each class in the list,
		String[] classes = classesAsString.split(InputKeys.LIST_ITEM_SEPARATOR);
		String userLogin = awRequest.getUser().getUserName();
		for(int i = 0; i < classes.length; i++) {
			// Ensure that the class exists.
			try {
				if(getJdbcTemplate().queryForInt(SQL_CLASS_COUNT, new Object[] { classes[i] }) == 0) {
					_logger.info("Class does not exist: " + classes[i]);
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_CLASS_COUNT + "' with parameter: " + classes[i], dae);
				throw new DataAccessException(dae);
			}
			
			// Ensure that the user is in the class.
			try {
				if(getJdbcTemplate().queryForInt(SQL_USER_IN_CLASS, new Object [] { classes[i], userLogin }) == 0) {
					_logger.error("User does not belong to this class: " + classes[i]);
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_USER_IN_CLASS + "' with parameters: " + classes[i] + ", " + userLogin, dae);
				throw new DataAccessException(dae);
			}
		}
	}
}