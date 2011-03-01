package edu.ucla.cens.awserver.cache;

/**
 * Provide data lookup functionality against a cache.
 * 
 * @author selsky
 */
public interface CacheService {
	
	Object lookup(Object key);
	
	boolean containsKey(Object key);
	
}
