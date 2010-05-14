package edu.ucla.cens.awserver.cache;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.ParameterLessDao;
import edu.ucla.cens.awserver.domain.UserRole;

/**
 * Simple cache for user roles. This class does not use synchronization as it is assumed that once the user roles are loaded, they
 * will not change.
 * 
 * TODO: add reload functionality (and synchronization) (new interface: ReloadableCacheService)
 * 
 * @author selsky
 */
public class UserRoleCacheService extends AbstractCacheService {
	private Map<Integer, UserRole> _cache;
	private static final Logger _logger = Logger.getLogger(UserRoleCacheService.class);
		
	/**
	 * Initialize the local cache using the provided Dao.
	 * 
	 * @throws IllegalArgumentException if the provided Dao is null
	 */
	public UserRoleCacheService(ParameterLessDao dao) {
		super(dao);
		init();
	}
	
	/**
	 * @throws IllegalStateException if no user roles are found in the database
	 */
	private void init() {
		_cache = new TreeMap<Integer, UserRole> ();
		
		List<?> userRoles = _dao.execute();
		
		int listSize = userRoles.size();
		if(listSize < 1) {
			throw new IllegalStateException("database has incorrect state - no user roles found");
		}
		
		for(int i = 0; i < listSize; i++) {
			UserRole ur = (UserRole) userRoles.get(i);
			_cache.put(ur.getId(), ur);
		}
		
		_logger.info("user role cache loaded with " + listSize + " roles");
	}
	
	/**
	 * @return the String representation of the user role defined by the provided key
	 */
	@Override
	public Object lookup(Object key) {
		
		return _cache.get(key).getName(); // no copy because Strings are immutable
	}
}
