package org.ohmage.query.impl;

import javax.sql.DataSource;

import org.ohmage.domain.Probe;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IProbeQueries;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class is responsible for creating, reading, updating, and deleting
 * probe information.
 * 
 * @author John Jenkins
 */
public class ProbeQueries extends Query implements IProbeQueries {
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource
	 *        The DataSource to use when querying the database.
	 */
	private ProbeQueries(DataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ohmage.query.IProbeQueries#createProbe(org.ohmage.domain.Probe)
	 */
	@Override
	public void createProbe(final Probe probe) throws DataAccessException {
		if(probe == null) {
			throw new IllegalArgumentException("The probe is null.");
		}

		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a probe.");

		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager =
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);

			// TODO: Create the probe.

			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error while committing the transaction.",
					e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException(
				"Error while attempting to rollback the transaction.",
				e);
		}
	}
}