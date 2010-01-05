package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

/**
 * Provides base classes with access to a JDBC DataSource.
 * 
 * @author selsky
 */
public abstract class AbstractDao implements Dao {
	
	private DataSource _dataSource;
    
	public AbstractDao(DataSource dataSource) {
		if(null == dataSource) {
			throw new IllegalArgumentException("a non-null DataSource is required");
		}
		
		_dataSource = dataSource;
	}
	
	protected DataSource getDataSource() {
		return _dataSource;
	}
}
