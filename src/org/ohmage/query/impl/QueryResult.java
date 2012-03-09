package org.ohmage.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ohmage.exception.DomainException;
import org.ohmage.query.IQueryResult;

/**
 * The results of a query containing the total number of results that matched
 * the query as well as the actual list of results which may be fewer than the
 * total number of results due to paging.
 * 
 * @author John Jenkins
 *
 * @param <T> The type of object returned from the result.
 */
public class QueryResult<T> implements IQueryResult<T> {
	private final long totalNumResults;
	private final List<T> results;
	
	/**
	 * Creates a QueryResult object.
	 * 
	 * @author John Jenkins
	 *
	 * @param <T> The type of QueryResult object to build.
	 */
	public static class QueryResultBuilder<T> {
		private long totalNumResults;
		private List<T> results;
		
		/**
		 * Creates a builder with no elements.
		 */
		public QueryResultBuilder() {
			totalNumResults = 0;
			results = new LinkedList<T>();
		}
		
		/**
		 * Creates a builder with a default number of total results.
		 * 
		 * @param totalNumResults The total number of results.
		 */
		public QueryResultBuilder(final long totalNumResults) {
			this.totalNumResults = totalNumResults;
			results = new LinkedList<T>();
		}
		
		/**
		 * Creates a builder with a preliminary collection of results. If the
		 * preliminary collection is null, the initial list will be empty.
		 * 
		 * @param results A preliminary collection of results.
		 */
		public QueryResultBuilder(final Collection<T> results) {
			if(results == null) {
				this.results = new LinkedList<T>();
			}
			else {
				this.results = new LinkedList<T>(results);
			}
			
			totalNumResults = this.results.size();
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
		public QueryResultBuilder(
				final long totalNumResults,
				final Collection<T> results) {
			
			this.totalNumResults = totalNumResults;
			
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
			totalNumResults++;
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
			totalNumResults += results.size();
		}
		
		/**
		 * Returns the current total number of results.
		 * 
		 * @return The current total number of results.
		 */
		public long getTotalNumResults() {
			return totalNumResults;
		}
		
		/**
		 * Sets the total number of results.
		 * 
		 * @param totalNumResults The total number of results.
		 */
		public void setTotalNumResults(final long totalNumResults) {
			this.totalNumResults = totalNumResults;
		}
		
		/**
		 * Increments the total number of results by 1.
		 */
		public void increaseTotalNumResults() {
			totalNumResults++;
		}
		
		/**
		 * Increases the total number of results by the value given.
		 * 
		 * @param amountToIncrease The amount to increase the total number of
		 * 						   results.
		 */
		public void increaseTotalNumResults(final long amountToIncrease) {
			totalNumResults += amountToIncrease;
		}
		
		/**
		 * Creates the QueryResult. The list of results is the list that has
		 * been aggregated in the builder. The total number of results is the
		 * greater of the currently set number of results and the length of the
		 * list of results. This is because you cannot have a total number of
		 * results be less than the actual number of results.
		 * 
		 * @return A QueryResult object.
		 * 
		 * @throws DomainException There was an error creating the QueryResult
		 * 						   object.
		 */
		public QueryResult<T> getQueryResult() throws DomainException {
			long actualTotalNumResults;
			if(totalNumResults < results.size()) {
				actualTotalNumResults = results.size();
			}
			else {
				actualTotalNumResults = totalNumResults;
			}
			
			return new QueryResult<T>(actualTotalNumResults, results);
		}
	}
	
	/**
	 * Creates a new QueryResult object with the total number of results and
	 * the list of results.
	 * 
	 * @param totalNumResults The total number of results from the query.
	 * 
	 * @param results The actual results being returned.
	 * 
	 * @throws DomainException The list of results was null or the total number
	 * 						   of results is less than the length of the list 
	 * 						   of results.
	 */
	public QueryResult(
			final long totalNumResults,
			final List<T> results) 
			throws DomainException {
		
		if(results == null) {
			throw new DomainException("The list of results is null.");
		}
		else if(totalNumResults < results.size()) {
			throw new DomainException(
					"The total number of results is less than the actual number of results.");
		}
		
		this.totalNumResults = totalNumResults;
		this.results = new ArrayList<T>(results);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IQueryResult#getTotalNumResults()
	 */
	@Override
	public long getTotalNumResults() {
		return totalNumResults;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IQueryResult#results()
	 */
	@Override
	public List<T> getResults() {
		return new ArrayList<T>(results);
	}
}