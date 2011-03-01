package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Abstract DAO for subclass access to a JdbcTemplate.
 * 
 * @author selsky
 */
public abstract class AbstractParameterLessDao implements ParameterLessDao {
	protected JdbcTemplate _jdbcTemplate;
	
	public AbstractParameterLessDao(DataSource dataSource) {
		if(null == dataSource) {
			throw new IllegalArgumentException("dataSource cannot be null");
		}
		_jdbcTemplate = new JdbcTemplate(dataSource);
	}
}
