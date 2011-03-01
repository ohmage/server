package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.CampaignNameVersion;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;

/**
 * @author selsky
 */
public class FindCampaignForMediaIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindCampaignForMediaIdDao.class);
	
	private String _sql = "SELECT c.name, cc.version"
		  	             + " FROM campaign c, campaign_configuration cc, survey_response sr, prompt_response pr, user u"
		  	             + " WHERE pr.response = ?"
		  	             + " AND pr.survey_response_id = sr.id" 
		  	             + " AND sr.campaign_configuration_id = cc.id"
		  	             + " AND cc.campaign_id = c.id"
		  	             + " AND sr.user_id = u.id"
		  	             + " AND u.login_id = ?";
	
	public FindCampaignForMediaIdDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Sets the campaign name and campaign version in the request using the imageId from the request as a paramter to the SQL.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] { ((MediaQueryAwRequest) awRequest).getMediaId(), ((MediaQueryAwRequest) awRequest).getUserNameRequestParam() },
					new RowMapper() {
						public Object mapRow(ResultSet rs, int index) throws SQLException {
							return new CampaignNameVersion(rs.getString(1), rs.getString(2));
						}
					})
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ ((MediaQueryAwRequest) awRequest).getMediaId() , dae);
			throw new DataAccessException(dae);
		}
	}
}
