package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Maps each row from a query ResultSet to a LoginResult. Used by JdbcTemplate in call-back fashion. 
 * 
 * @author selsky
 */
public class AuthenticationResultRowMapper implements RowMapper {
	
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException { // The Spring classes will wrap this exception
		                                                                 // in a Spring DataAccessException
		LoginResult lr = new LoginResult();
		lr.setUserId(rs.getInt(1));
		lr.setEnabled(rs.getBoolean(2));
		lr.setNew(rs.getBoolean(3));
		// lr.setCampaignId(rs.getInt(4));
		lr.setCampaignUrn(rs.getString(5));
		lr.setUserRoleId(rs.getInt(6));
		return lr;
	}
}