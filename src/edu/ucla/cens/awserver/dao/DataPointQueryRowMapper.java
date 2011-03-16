package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.DataPointQueryResult;

/**
 * @author selsky
 */
public class DataPointQueryRowMapper implements RowMapper {
	
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		DataPointQueryResult result = new DataPointQueryResult();
		result.setPromptId(rs.getString(1));
		result.setPromptType(rs.getString(2));
		result.setResponse(rs.getObject(3));
		
		Object o = rs.getObject(4);
		if(null == o) {
			result.setRepeatableSetIteration(null);	
		} else {
			result.setRepeatableSetIteration(rs.getInt(4));
		}
		
		result.setRepeatableSetId(rs.getString(5));
		result.setTimestamp(rs.getString(6));
		result.setTimezone(rs.getString(7));
		result.setLocationStatus(rs.getString(8));
		result.setLocation(rs.getString(9));
		result.setSurveyId(rs.getString(10));
		return result;
	}
}
