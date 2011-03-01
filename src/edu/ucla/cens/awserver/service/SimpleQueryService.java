package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Service that dispatches directly to a DAO without performing any pre- or post-processing.
 * 
 * @author selsky
 */
public class SimpleQueryService extends AbstractDaoService {

    public SimpleQueryService(Dao dao) {
    	super(dao);
    }
	
	public void execute(AwRequest awRequest) {
		
		try {
		
			getDao().execute(awRequest);
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}
}
