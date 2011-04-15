package edu.ucla.cens.awserver.dao;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Checks that the user has sufficient permissions to create a new campaign.
 * 
 * @author John Jenkins
 */
public class CampaignCreationValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignCreationValidationDao.class);
	
	private static final String SQL = "SELECT campaign_creation_privilege" +
									  " FROM user" +
									  " WHERE login_id=?";
	
	/**
	 * Creates a new DAO to check if the user has permissions to create a new
	 * campaign.
	 * 
	 * @param dataSource The DataSource to run our queries against.
	 */
	public CampaignCreationValidationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Checks that the user has sufficient permissions to create a new
	 * campaign.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String username = awRequest.getUser().getUserName();

		@SuppressWarnings("rawtypes")
		List result;
		try {
			result = getJdbcTemplate().query(SQL, new Object[] { username }, new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + username, dae);
			throw new DataAccessException(dae);
		}
		
		if(! ((Boolean) result.get(0))) {
			awRequest.setFailedRequest(true);
		}
	}

}
