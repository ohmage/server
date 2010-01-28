package edu.ucla.cens.awserver.service;

import java.util.Map;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Service a data upload for prompt responses or mobility data by dispatching to a DAO for persistence.
 * 
 * @author selsky
 */
public class UploadService implements Service {
	private Map<String, Dao> _daoMap;
	
	/**
	 * @throws IllegalArgumentException if the provided map is null or empty or does not contain values for the keys mobility or 
	 * prompt 
	 */
	public UploadService(Map<String, Dao> daoMap) {
		if(null == daoMap || daoMap.isEmpty()) {
			throw new IllegalArgumentException("a non-null Map of DAOs is required");			
		}
		if(null == daoMap.get("prompt")) {
			throw new IllegalArgumentException("no DAO found for prompt");
		}
		if(null == daoMap.get("mobility")) {
			throw new IllegalArgumentException("no DAO found for mobility");
		}
		
		_daoMap = daoMap;
	}
	
	/**
	 * Dispatches to a DAO depending on the request type.
	 */
	public void execute(AwRequest awRequest) {
		try {
		
			_daoMap.get(awRequest.getAttribute("requestType")).execute(awRequest);
			
		} catch(DataAccessException dae) {
			
			throw new ServiceException(dae);
		}
	}
}
