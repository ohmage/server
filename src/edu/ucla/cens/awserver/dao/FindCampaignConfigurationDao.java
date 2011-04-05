package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.domain.SurveyMapBuilder;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Configuration data access object.
 * 
 * @author selsky
 */
public class FindCampaignConfigurationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindCampaignConfigurationDao.class);
	private SurveyMapBuilder _surveyBuilder;
	private String _sql = "SELECT name, description, xml, running_state, privacy_state, creation_timestamp" +
		                  " FROM campaign" +
		                  " WHERE urn = ?";
	
	public FindCampaignConfigurationDao(DataSource dataSource, SurveyMapBuilder builder) {
		super(dataSource);
		if(null == builder) {
			throw new IllegalArgumentException("a SurveyMapBuilder is required");
		}
		_surveyBuilder = builder;
	}
	
	/**
	 * For the campaign URN found in the awRequest, performs a lookup of the associated campaign configuration from the campaign
	 * table. If a configuration is found, a new Configuration object is set in the awRequest's result list.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		final String urn = awRequest.getCampaignUrn();
		try {
			awRequest.setResultList(getJdbcTemplate().query(_sql, new Object[] {awRequest.getCampaignUrn()}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					String name = rs.getString(1);
					String description = rs.getString(2);
					String xml = rs.getString(3);
					String runningState = rs.getString(4);
					String privacyState = rs.getString(5);
					String timestamp = rs.getTimestamp(6).toString();
					return new Configuration(urn, name, description, 
							runningState, privacyState, timestamp, _surveyBuilder.buildFrom(xml), xml);
				}
		    }));
			
		} catch (org.springframework.dao.DataAccessException dae) {
				
			_logger.error("an exception occurred running the sql '" + _sql + "' " + dae.getMessage());
			throw new DataAccessException(dae);
		}
	}
}
