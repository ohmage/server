package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.MediaQueryAwRequest;

/**
 * DAO for looking up URLs in url_based_resource given a media id.
 * 
 * @author selsky
 */
public class FindUrlForMediaIdDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindUrlForMediaIdDao.class);
	private String _sql = "SELECT url FROM url_based_resource WHERE uuid = ?";
	
	public FindUrlForMediaIdDao(DataSource dataSource) {
		super(dataSource);
	}
		
	@Override
	public void execute(AwRequest awRequest) {
		try {
		
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql,
					new Object[] {((MediaQueryAwRequest) awRequest).getMediaId()},
					new SingleColumnRowMapper())
			);
		
		} catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter "
				+ ((MediaQueryAwRequest) awRequest).getMediaId() , dae);
			throw new DataAccessException(dae);
		}
	}
}
