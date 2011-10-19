package org.ohmage.query;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Abstract base class for any 'upload' Query that needs to manage duplicate
 * uploads.
 * 
 * @author Joshua Selsky
 */
public class AbstractUploadQuery extends Query {
	private static final int MYSQL_DUPLICATE_KEY_ERROR_CODE = 1062;
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public AbstractUploadQuery(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * @return true if the Throwable represents an SQLException and the error code in the SQLException is 1062 (the MySQL error 
	 * code for dupes).
	 * @return false otherwise 
	 */
	protected boolean isDuplicate(Throwable t) {
		 return (t.getCause() instanceof SQLException) && (((SQLException) t.getCause()).getErrorCode() == MYSQL_DUPLICATE_KEY_ERROR_CODE);
	}
}
