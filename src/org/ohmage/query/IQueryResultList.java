package org.ohmage.query;

import java.util.List;

/**
 * Records the total number of results that matched the query as well as a list
 * of the actual results from the query which may be less than the total number
 * of results due to paging.
 * 
 * @author John Jenkins
 *
 * @param <T> The type of object returned from this query.
 */
public interface IQueryResultList<T> extends IQueryResults {
	/**
	 * Returns the list of results in the order they were received from the
	 * database.
	 * 
	 * @return The list of results in the order they were received from the
	 * 		   database.
	 */
	public List<T> getResults();
}
