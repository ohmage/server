package org.ohmage.query.impl;

import org.ohmage.query.IQueryResults;

public abstract class QueryResults implements IQueryResults {
	private final long totalNumResults;
	
	protected static class QueryResultsBuilder {
		private long totalNumResults;
		
		public QueryResultsBuilder() {
			totalNumResults = 0;
		}
		
		public QueryResultsBuilder(final long totalNumResults) {
			this.totalNumResults = totalNumResults;
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
	}
	
	/**
	 * Sets up this result query with the total number of results.
	 * 
	 * @param totalNumResults The total number of results.
	 */
	protected QueryResults(final long totalNumResults) {
		this.totalNumResults = totalNumResults;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IQueryResults#getTotalNumResults()
	 */
	@Override
	public long getTotalNumResults() {
		return totalNumResults;
	}
}