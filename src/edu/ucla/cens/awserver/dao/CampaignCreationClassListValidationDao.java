package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * DAO for checking the list of classes associated with a campaign creation
 * request. Checks to ensure that the user has the ability to create campaigns
 * associated with the list of classes and that the classes exist.
 * 
 * @author John Jenkins
 */
public class CampaignCreationClassListValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignCreationClassListValidationDao.class);
	
	private static final String SQL_CLASS_COUNT = "SELECT count(*) " +
												  "FROM class " +
												  "WHERE urn=?";
	
	private static final String SQL_USER_IN_CLASS = "SELECT count(*) " +
													"FROM user u, class c, user_class uc " +
													"WHERE c.urn=? " +
													"AND u.login_id=? " +
													"AND c.id = uc.class_id " +
													"AND u.id = uc.user_id";
	
	/**
	 * Sets up the data source for this DAO.
	 * 
	 * @param dataSource The data source that will be used to query the
	 * 					 database for information.
	 */
	public CampaignCreationClassListValidationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to ensure that this user has the appropriate permissions to
	 * create a class associated with the list of classes and that those
	 * classes exist.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		CampaignCreationAwRequest request;
		try {
			request = (CampaignCreationAwRequest) awRequest;
		}
		catch(ClassCastException e) {
			_logger.error("Checking class list on a non-CampaignCreationAwRequest object.");
			throw new DataAccessException("Invalid request.");
		}
		
		String[] classes = request.getCommaSeparatedListOfClasses().split(",");
		String userLogin = request.getUser().getUserName();
		for(int i = 0; i < classes.length; i++) {
			int numClasses;
			try {
				numClasses = getJdbcTemplate().queryForInt(SQL_CLASS_COUNT, new Object[] { classes[i] });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_CLASS_COUNT + "' with parameter: " + classes[i], dae);
				throw new DataAccessException(dae);
			}
			if(numClasses == 0) {
				awRequest.setFailedRequest(true);
			}
			
			int userBelongs;
			try {
				userBelongs = getJdbcTemplate().queryForInt(SQL_USER_IN_CLASS, new Object [] { classes[i], userLogin });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_USER_IN_CLASS + "' with parameters: " + classes[i] + ", " + userLogin, dae);
				throw new DataAccessException(dae);
			}
			if(userBelongs == 0) {
				awRequest.setFailedRequest(true);
			}
		}
	}
}
