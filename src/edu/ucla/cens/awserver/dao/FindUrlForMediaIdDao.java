package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UrlPrivacyState;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;

/**
 * DAO for looking up a URL and its associated privacy state from url_based_resource and survey_response.
 * 
 * @author Joshua Selsky
 */
public class FindUrlForMediaIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindUrlForMediaIdDao.class);
	
	private String _sql = "SELECT url_based_resource.url, survey_response_privacy_state.privacy_state "
			               + "FROM url_based_resource, survey_response, survey_response_privacy_state, prompt_response "
			               + "WHERE url_based_resource.uuid = ? "
			               + "AND prompt_response.response = url_based_resource.uuid "
			               + "AND survey_response.privacy_state_id = survey_response_privacy_state.id "
			               + "AND prompt_response.survey_response_id = survey_response.id";
	
	public FindUrlForMediaIdDao(DataSource dataSource) {
		super(dataSource);
	}
		
	@Override
	public void execute(AwRequest awRequest) {
		try {
		
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql,
					new Object[] { ((MediaQueryAwRequest) awRequest).getMediaId() }, // FIXME
					new RowMapper() {
					    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					    	return new UrlPrivacyState(rs.getString(1), rs.getString(2));
					    }
					})
			);
		
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ ((MediaQueryAwRequest) awRequest).getMediaId() , dae);
			throw new DataAccessException(dae);
		}
	}
}
