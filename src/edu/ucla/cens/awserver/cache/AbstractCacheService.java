package edu.ucla.cens.awserver.cache;

import edu.ucla.cens.awserver.dao.ParameterLessDao;

/**
 * Holds a ParameterLessDao for data access.
 * 
 * @author selsky
 */
public abstract class AbstractCacheService implements CacheService {
	protected ParameterLessDao _dao;
	
	/**
	 * @throws IllegalArgumentException if the provided Dao is null
	 */
	public AbstractCacheService(ParameterLessDao dao) {
		if(null == dao) {
			throw new IllegalArgumentException("a Dao is required");
		}
		
		_dao = dao;
	}
	
}
