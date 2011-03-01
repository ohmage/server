package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Service a data upload; dispatch to a DAO for persistence.
 * 
 * This class is completely redundant with SimpleQueryService.
 * 
 * @author selsky
 */
public class DaoService implements Service {
	private Dao _dao;
	
	/**
	 * @throws IllegalArgumentException if the provided Dao is null
	 */
	public DaoService(Dao dao) {
		if(null == dao) {
			throw new IllegalArgumentException("a non-null DAO is required");			
		}
		_dao = dao;
	}
	
	/**
	 * Simply dispatches to a DAO for request processing.
	 */
	public void execute(AwRequest awRequest) {
		try {
		
			_dao.execute(awRequest);
			
		} catch(DataAccessException dae) {
			
			throw new ServiceException(dae);
		}
	}
}
