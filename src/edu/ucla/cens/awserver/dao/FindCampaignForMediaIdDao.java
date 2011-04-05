package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;

/**
 * @author selsky
 */
public class FindCampaignForMediaIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindCampaignForMediaIdDao.class);
	
	private String _sql = "SELECT c.urn"
		  	             + " FROM campaign c, survey_response sr, prompt_response pr, user u"
		  	             + " WHERE pr.response = ?"
		  	             + " AND pr.survey_response_id = sr.id" 
		  	             + " AND sr.campaign_id = c.id"
		  	             + " AND sr.user_id = u.id"
		  	             + " AND u.login_id = ?";
	
	public FindCampaignForMediaIdDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Finds the campaign URN for the media id and user in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try { 
			// FIXME -- this should be returning a single row so all that's needed is a query() that returns an Object
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					new Object[] { ((MediaQueryAwRequest) awRequest).getMediaId(), ((MediaQueryAwRequest) awRequest).getUserNameRequestParam() },
					new SingleColumnRowMapper())
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ ((MediaQueryAwRequest) awRequest).getMediaId() , dae);
			throw new DataAccessException(dae);
		}
	}
}
