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
	private String _sql = "SELECT c.name, cc.version, cc.xml" +
		                  " FROM campaign c, campaign_configuration cc" +
		                  " WHERE c.name = ?";
	
	public FindCampaignConfigurationDao(DataSource dataSource, SurveyMapBuilder builder) {
		super(dataSource);
		if(null == builder) {
			throw new IllegalArgumentException("a SurveyMapBuilder is required");
		}
		_surveyBuilder = builder;
	}
	
	/**
	 * Returns a list of campaign configurations.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			awRequest.setResultList(getJdbcTemplate().query(_sql, new Object[] {awRequest.getCampaignName()}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					String campaignName = rs.getString(1);
					String campaignVersion = rs.getString(2);
					String xml = rs.getString(3);
					return new Configuration(campaignName, campaignVersion, _surveyBuilder.buildFrom(xml), xml);
				}
		    }));
			
		} catch (org.springframework.dao.DataAccessException dae) {
				
			_logger.error("an exception occurred running the sql '" + _sql + "' " + dae.getMessage());
			throw new DataAccessException(dae);
		}
	}
}
