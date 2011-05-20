package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Checks to make sure that the survey key exists and belongs to the currently logged-in user.
 * 
 * @author Joshua Selsky
 */
public class SurveyKeyExistsForLoggedInUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyKeyExistsForLoggedInUserDao.class);
	
	private static final String SQL = "SELECT EXISTS (SELECT * " +
									  "FROM survey_response, user " +
									  "WHERE survey_response.id = ? AND user.login_id = ? AND survey_response.user_id = user.id)";
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public SurveyKeyExistsForLoggedInUserDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to make sure that the survey key belongs to the logged-in user.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String surveyKey = (String) awRequest.getToValidate().get(InputKeys.SURVEY_KEY);
		String userName = awRequest.getUser().getUserName();
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { surveyKey, userName }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with the parameters: " + surveyKey + ", " + userName, dae);
			throw new DataAccessException(dae);
		}
	}
}
