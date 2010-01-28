package edu.ucla.cens.awserver.controller;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Test controller for dispatch to a DAO for round-trip (browser-database-browser) testing . This class and it's associated 
 * configuration (web.xml) should be removed. Do not follow the design of this class. Check out ControllerImpl for the 
 * correct implementation of a Controller i.e., dispatch to Validators and Services to handle the request instead of dispatching
 * directly to a DAO.
 * 
 * @author selsky
 */
public class TestDbController implements Controller {
    private Dao _dao;
	
    /**
     * Default no-arg constructor.
     */
    public TestDbController() {
    	
    }
	
    /**
     * Set the DAO that will be used by the execute method.
     */
    public void setDao(Dao dao) {
    	if(null == dao) {
    		throw new IllegalArgumentException("null Dao not allowed");
    	}
    	_dao = dao;
    }
    	
    /**
     * Dispatch to instance variable DAO to run a test query.
     */
	public void execute(AwRequest awRequest) {
		_dao.execute(awRequest);
	}
}
