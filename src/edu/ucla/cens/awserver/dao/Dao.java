package edu.ucla.cens.awserver.dao;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Dao - Data Access Object: interact with the database. 
 * 
 * @author selsky
 */
public interface Dao {

	public void execute(AwRequest request);
	
}
