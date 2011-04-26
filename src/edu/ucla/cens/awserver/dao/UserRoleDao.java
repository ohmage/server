package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UserRoleImpl;

/**
 * Dao for retrieving the roles out of the user_role table.
 * 
 * @author selsky
 */
public class UserRoleDao implements ParameterLessDao {
	private JdbcTemplate _jdbcTemplate;
	private String _sql = "select id, role from user_role";
	private static Logger logger = Logger.getLogger(UserRoleDao.class);
	
	/**
	 * @throws IllegalArgumentException if the provided DataSource is null
	 */
	public UserRoleDao(DataSource dataSource) {
		_jdbcTemplate = new JdbcTemplate(dataSource); 
	}
	
	/**
	 * @return a list of all of the user roles found in the database
	 */
	@Override
	public List<?> execute() {
		try {
			
			return _jdbcTemplate.query(_sql, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new UserRoleImpl(rs.getInt(1), rs.getString(2));
				}
			});
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			logger.error("an error occurred when attempting to run the following SQL: " + _sql);
			throw new DataAccessException(dae.getMessage());
		
		}
	}
}
