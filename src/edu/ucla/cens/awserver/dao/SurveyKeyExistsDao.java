package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Checks to make sure that the survey key exists.
 * 
 * @author Joshua Selsky
 */
public class SurveyKeyExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyKeyExistsDao.class);
	
	private static final String SQL = "SELECT EXISTS (SELECT * " +
									  "FROM survey_response " +
									  "WHERE id = ?)";
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 */
	public SurveyKeyExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks to make sure that the survey key in question exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String surveyKey = (String) awRequest.getToValidate().get(InputKeys.SURVEY_KEY);
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { surveyKey }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + surveyKey, dae);
			throw new DataAccessException(dae);
		}
	}
}
