package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Service that dispatches directly to a DAO without performing any pre- or post-processing.
 * 
 * @author selsky
 */
public class SimpleQueryService extends AbstractDaoService {

	/**
     * Creates and instance of this class and passes dao to the super class constructor.  
     */
    public SimpleQueryService(Dao dao) {
    	super(dao);
    }
	
    /**
     * Dispatches to DAO.
     */
	public void execute(AwRequest awRequest) {
		getDao().execute(awRequest);
	}
}
