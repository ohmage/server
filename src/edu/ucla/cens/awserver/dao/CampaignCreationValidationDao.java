package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

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
	@SuppressWarnings("unchecked")
	@Override
	public void execute(AwRequest awRequest) {
		String username = awRequest.getUser().getUserName();
		
		List<Boolean> result = (List<Boolean>) getJdbcTemplate().query(SQL, new Object[] { username }, new RowMapper() {
			@Override
			public Object mapRow(ResultSet rs, int line) throws SQLException {
				return rs.getBoolean(0);
			}
		});
		
		if(! result.get(0)) {
			throw new DataAccessException("User cannot create new campaigns.");
		}
	}

}
