package org.ohmage.query.impl;

import java.util.HashMap;
import java.util.Map;

import org.ohmage.query.IQueryResultsMap;

/**
 * The results of a query containing the total number of results that matched
 * the query as well as the actual map of results which may be fewer than the
 * total number of results due to paging.
 * 
 * @author John Jenkins
 *
 * @param <K> The type of object acting as the key.
 * 
 * @param <V> The type of object acting as the value.
 */
public final class QueryResultsMap<K, V> 
		extends QueryResults 
		implements IQueryResultsMap<K, V> {
	
	private final Map<K, V> results;
	
	/**
	 * Builds a new QueryResultMap object.
	 * 
	 * @author John Jenkins
	 *
	 * @param <T> The type of object acting as the key.
	 * 
	 * @param <S> The type of object acting as the value.
	 */
	public static class QueryResultMapBuilder<T, S> extends QueryResultsBuilder {
		private Map<T, S> results;
		
		/**
		 * Creates a builder with no elements.
		 */
		public QueryResultMapBuilder() {
			this(0, null);
		}
		
		/**
		 * Creates a builder with a default number of total results.
		 * 
		 * @param totalNumResults The initial total number of results.
		 */
		public QueryResultMapBuilder(final long totalNumResults) {
			this(totalNumResults, null);
		}
		
		/**
		 * Creates a builder with a preliminary map of results. If the 
		 * preliminary map is null, the initial map will be empty.
		 * 
		 * @param results A preliminary map of results.
		 */
		public QueryResultMapBuilder(final Map<T, S> results) {
			if(results == null) {
				this.results = new HashMap<T, S>();
			}
			else {
				this.results = new HashMap<T, S>(results);
			}
			
			setTotalNumResults(this.results.size());
		}
		
		/**
		 * Creates a builder with a default number of total results and a
		 * preliminary map of results. If the preliminary map is null, the 
		 * initial map will be empty.
		 * 
		 * @param totalNumResults The initial total number of results.
		 * 
		 * @param results A preliminary collection of results.
		 */
		public QueryResultMapBuilder(
				final long totalNumResults, 
				final Map<T, S> results) {
			
			this(results);
			
			setTotalNumResults(totalNumResults);
		}
		
		/**
		 * Adds a result to the current map of results. This will increase the
		 * total number of results by 1 regardless of whether an existing value
		 * is replaced by this value. Null is an acceptable value, but not an
		 * acceptable key. All keys must be distinct, therefore, if a second 
		 * pair is added with the same key as an existing pair, the old value
		 * will be removed and returned and the new value will replace it. The
		 * total count will be automatically incremented because there were
		 * clearly two distinct pairs even if only the latter can be returned.
		 * 
		 * @param key The key for a new pair.
		 * 
		 * @param value The value for a new pair.
		 * 
		 * @return The previous element associated with 'key'; null if no such
		 * 		   key previously existed.
		 */
		public S addResult(final T key, final S value) {
			if(key == null) {
				throw new IllegalArgumentException("The key cannot be null.");
			}
			
			S oldValue = results.put(key, value);
			increaseTotalNumResults();
			
			return oldValue;
		}
		
		/**
		 * Adds all of the elements in the map to the result set. This may 
		 * remove existing pairs if they share the same key as a previous pair.
		 * The total count will be incremented by the 'results' parameter 
		 * regardless of whether or not previous pairs were removed.
		 * 
		 * @param results The map to merge with the current set of results.
		 */
		public void addAllResults(final Map<T, S> results) {
			results.putAll(results);
			increaseTotalNumResults(results.size());
		}
		
		/**
		 * Creates the QueryResultMap object based on the state of this 
		 * builder.
		 * 
		 * @return The QueryResultMap object.
		 */
		public QueryResultsMap<T, S> getQueryResult() {
			long actualTotalNumResults;
			if(getTotalNumResults() < results.size()) {
				actualTotalNumResults = results.size();
			}
			else {
				actualTotalNumResults = getTotalNumResults();
			}
			
			return new QueryResultsMap<T, S>(actualTotalNumResults, results);
		}
	}

	/**
	 * Creates a new QueryResultsMap object with a map of a results and total
	 * number of results. This is protected, and the class is final. Therefore, 
	 * may only be called by the QueryResultMapBuilder. Therefore, it is 
	 * assumed that the given parameters are valid.
	 * 
	 * @param totalNumResults The total number of results which must be greater
	 * 						  than or equal to the results map.
	 * 
	 * @param results The map of results.
	 */
	protected QueryResultsMap(
			final long totalNumResults,
			final Map<K, V> results) {
		
		super(totalNumResults);
		
		this.results = new HashMap<K, V>(results);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IQueryResultList#results()
	 */
	@Override
	public Map<K, V> getResults() {
		return new HashMap<K, V>(results);
	}
}