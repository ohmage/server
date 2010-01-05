package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * DAO for checking campaign existence. The subdomain from the initial request URI is used to attempt lookup of a campaign id. 
 * 
 * @author selsky
 */
public class CampaignExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignExistsDao.class);
	private static final String _selectSql = "select id from campaign where subdomain = ?";
	
	/**
	 * 
	 */
	public CampaignExistsDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Checks the db for the existence of a user represented by a user name and a subdomain found in the AwRequest.
	 * If a user is found, the campaigns that user belongs to are placed in the AwRequest payload Map.
	 */
	public void execute(AwRequest request) {
		_logger.info("executing campaign existence check against subdomain " + request.getPayload().get("subdomain"));
		
		JdbcTemplate template = new JdbcTemplate(getDataSource());
		
		try {

			Map<String, Object> map = request.getPayload();
			map.put("results", template.query(_selectSql, 
					                          new Object[]{map.get("subdomain")}, 
					                          new QueryRowMapper()));
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			throw new DataAccessException(dae); // wrap the Spring exception and re-throw in order to avoid dependencies
			                                    // on the Spring Exception in case we want to replace the data layer
		}
	}

	/**
	 * Maps a query ResultSet to a TestResult object. Used by JdbcTemplate. 
	 * 
	 * @author selsky
	 */
	public class QueryRowMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException { // The Spring classes will wrap this exception
			                                                                 // in a Spring DataAccessException
			CampaignExistsResult cer = new CampaignExistsResult();
			int campaignId = rs.getInt(1);
			cer.setCampaignId(campaignId);
			return cer;
		}
	}
	
	/**
	 * Container used for query results.
	 * 
	 * @author selsky
	 */
	public class CampaignExistsResult {
		private int _campaignId;
		
		public int getCampaignId() {
			return _campaignId;
		}
		public void setCampaignId(int campaignId) {
			_campaignId = campaignId;
		}
	}
}
