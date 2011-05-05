package edu.ucla.cens.awserver.cache;

import javax.sql.DataSource;

public abstract class StringAndIdCache {
	
	/**
	 * Inner class for handling the results of a query for the running states
	 * and their respective IDs.
	 *  
	 * @author John Jenkins
	 */
	protected static class StringAndId {
		public int _id;
		public String _string;
		
		public StringAndId(int id, String string) {
			_id = id;
			_string = string;
		}
	}
	
	// The DataSource to use when querying the database.
	protected static DataSource _dataSource;
	
	/**
	 * Sets the dataSource for this object. This can only be called Spring on
	 * startup.
	 * 
	 * @complexity O(1)
	 * 
	 * @param dataSource The DataSource for this object to use when getting
	 * 					 the list of running states and their IDs. Cannot be
	 * 					 null.
	 * 
	 * @throws IllegalArgumentException Thrown if the dataSource has not yet
	 * 									been set and someone has passed in a
	 * 									null value.
	 */
	public synchronized void setDataSource(DataSource dataSource) {
		if(dataSource == null) {
			throw new IllegalArgumentException("A non-null DataSource is required.");
		} 
		
		_dataSource = dataSource;
	}
}