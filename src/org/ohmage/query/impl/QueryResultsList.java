package org.ohmage.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ohmage.exception.DomainException;
import org.ohmage.query.IQueryResultList;

/**
 * The results of a query containing the total number of results that matched
 * the query as well as the actual list of results which may be fewer than the
 * total number of results due to paging.
 * 
 * @author John Jenkins
 *
 * @param <T> The type of object returned from the result.
 */
public final class QueryResultsList<T> 
		extends QueryResults 
		implements IQueryResultList<T> {
	
	private final List<T> results;
	
	/**
	 * Creates a QueryResultsList object.
	 * 
	 * @author John Jenkins
	 *
	 * @param <T> The type of QueryResultsList object to build.
	 */
	public static class QueryResultListBuilder<T> extends QueryResultsBuilder {
		private List<T> results;
		
		/**
		 * Creates a builder with no elements.
		 */
		public QueryResultListBuilder() {
			super();
			
			results = new LinkedList<T>();
		}
		
		/**
		 * Creates a builder with a default number of total results.
		 * 
		 * @param totalNumResults The total number of results.
		 */
		public QueryResultListBuilder(final long totalNumResults) {
			super(totalNumResults);
			
			results = new LinkedList<T>();
		}
		
		/**
		 * Creates a builder with a preliminary collection of results. If the
		 * preliminary collection is null, the initial list will be empty.
		 * 
		 * @param results A preliminary collection of results.
		 */
		public QueryResultListBuilder(final Collection<T> results) {
			if(results == null) {
				this.results = new LinkedList<T>();
			}
			else {
				this.results = new LinkedList<T>(results);
			}
			
			setTotalNumResults(this.results.size());
		}
		
		/**
		 * Creates a builder with a default number of total results and a
		 * preliminary collection of results. If the preliminary collection is
		 * null, the initial list will be empty.
		 * 
		 * @param totalNumResults The total number of results.
		 * 
		 * @param results A preliminary collection of results
		 */
		public QueryResultListBuilder(
				final long totalNumResults,
				final Collection<T> results) {
			
			super(totalNumResults);
			
			if(results == null) {
				this.results = new LinkedList<T>();
			}
			else {
				this.results = new LinkedList<T>(results);
			}
		}
		
		/**
		 * Adds a result to the current list of results. This will increase the
		 * total number of results by 1. Null is an acceptable value and will
		 * be added to the list as well as increment the total number of 
		 * results.
		 * 
		 * @param result The new result to add.
		 */
		public void addResult(final T result) {
			results.add(result);
			increaseTotalNumResults();
		}
		
		/**
		 * Adds a collection of results to the current list of results. This
		 * will increase the total number of results by the number of results
		 * given. If the collection of results is null, this call is 
		 * effectively ignored.
		 * 
		 * @param results The collection of new results.
		 */
		public void addAllResults(final Collection<T> results) {
			if(results == null) {
				return;
			}
			
			this.results.addAll(results);
			increaseTotalNumResults(results.size());
		}
		
		/**
		 * Creates the QueryResultsList. The list of results is the list that 
		 * has been aggregated in the builder. The total number of results is 
		 * the greater of the currently set number of results and the length of
		 * the list of results. This is because you cannot have a total number 
		 * of results be less than the actual number of results.
		 * 
		 * @return A QueryResultsList object.
		 */
		public QueryResultsList<T> getQueryResult() {
			long actualTotalNumResults;
			if(getTotalNumResults() < results.size()) {
				actualTotalNumResults = results.size();
			}
			else {
				actualTotalNumResults = getTotalNumResults();
			}
			
			return new QueryResultsList<T>(actualTotalNumResults, results);
		}
	}
	
	/**
	 * Creates a new QueryResultsList object with the total number of results 
	 * and the list of results. This is protected, and the class is final.
	 * Therefore, can only be called by the QueryResultListBuilder. Therefore, 
	 * it is assumed that the given parameters are valid.
	 * 
	 * @param totalNumResults The total number of results from the query.
	 * 
	 * @param results The actual results being returned.
	 * 
	 * @throws DomainException The list of results was null or the total number
	 * 						   of results is less than the length of the list 
	 * 						   of results.
	 */
	private QueryResultsList(
			final long totalNumResults,
			final List<T> results) {
		
		super(totalNumResults);
		
		this.results = new ArrayList<T>(results);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IQueryResultList#results()
	 */
	@Override
	public List<T> getResults() {
		return new ArrayList<T>(results);
	}
}