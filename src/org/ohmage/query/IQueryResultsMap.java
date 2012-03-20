package org.ohmage.query;

import java.util.Map;

/**
 * Encompasses the results of a query. The parent class will handle the total
 * number of results and this class specifically contains the actual results.
 * 
 * @author John Jenkins
 *
 * @param <T> The type of the key for the map.
 * 
 * @param <S> The type of the value for the map.
 */
public interface IQueryResultsMap<T, S> extends IQueryResults {
	/**
	 * Returns a map of results from the database.
	 * 
	 * @return The map of results from the database.
	 */
	public Map<T, S> getResults();
}
