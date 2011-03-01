package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Provides base classes with access to a JDBC DataSource and Spring JdbcTemplate.
 * 
 * @author selsky
 */
public abstract class AbstractDao implements Dao {	
	private DataSource _dataSource;
	private JdbcTemplate _jdbcTemplate;
    
	/**
	 * Initializes a JdbcTempalte using the provided DataSource.
	 * 
	 * @throws IllegalArgumentException if the provided DataSource is null.
	 */
	public AbstractDao(DataSource dataSource) {
		if(null == dataSource) {
			throw new IllegalArgumentException("a non-null DataSource is required");
		}
		
		_dataSource = dataSource;
		_jdbcTemplate = new JdbcTemplate(_dataSource);
	}
	
	protected DataSource getDataSource() {
		return _dataSource;
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		return _jdbcTemplate;
	}
}
