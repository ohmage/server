package org.ohmage.query;

/**
 * This interface denotes all types of query results, and its subclasses should
 * define the types of results returned by the query.
 * 
 * @author John Jenkins
 */
public interface IQueryResults {
	/**
	 * Returns the total number of results that matched the query outside of 
	 * paging.
	 * 
	 * @return The total number of results that matched the query outside of
	 * 		   paging. 
	 */
	public abstract long getTotalNumResults();
}