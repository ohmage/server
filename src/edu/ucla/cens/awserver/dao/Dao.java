package edu.ucla.cens.awserver.dao;

import edu.ucla.cens.awserver.request.AwRequest;


/**
 * Data Access Object: interact with the datastore. 
 * 
 * @author selsky
 */
public interface Dao {

	public void execute(AwRequest awRequest);
	
}
