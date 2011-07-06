package org.ohmage.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The superclass for all DAOs, this class contains the DataSource with which 
 * it was built as well as a JDBC template which is the most common field that
 * subclasses will use to access the database.
 * 
 * All subclasses must be Singletons as subsequent invocations of a constructor
 * will throw an IllegalStateException.
 * 
 * @author John Jenkins
 */
public abstract class Dao {
	private boolean initialized = false;
	
	protected final DataSource dataSource;
	protected final JdbcTemplate jdbcTemplate;
	
	/**
	 * Builds this DAO by keeping track of the DataSource that was used to
	 * create it and creates a JDBC template for subclasses to use when 
	 * querying the database.
	 * 
	 * All subclasses must be Singletons in that all subsequent invocations of
	 * this constructor will throw an IllegalStateException. 
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 * 
	 * @throws IllegalArgumentException Thrown if the 'dataSource' is null.
	 * 
	 * @throws IllegalStateException Thrown if someone attempts to instantiate
	 * 								 this class again. All subclasses must be
	 * 								 Singletons.
	 */
	protected Dao(DataSource dataSource) {
		if(dataSource == null) {
			throw new IllegalArgumentException("The data source cannot be null.");
		}
		else if(initialized) {
			throw new IllegalStateException("A DAO should be built exactly once by Spring when the server is initialized.");
		}
		
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
		
		initialized = true;
	}
}