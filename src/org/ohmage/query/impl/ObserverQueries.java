package org.ohmage.query.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.ohmage.domain.Observer;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IObserverQueries;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
public class ObserverQueries extends Query implements IObserverQueries {
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource
	 *        The DataSource to use when querying the database.
	 */
	private ObserverQueries(DataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ohmage.query.IObserverQueries#createProbe(org.ohmage.domain.Observer)
	 */
	@Override
	public void createObserver(
			final String username,
			final Observer observer) 
			throws DataAccessException {
		
		if(observer == null) {
			throw new IllegalArgumentException("The observer is null.");
		}

		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating an observer.");

		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager =
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			final String observerSql =
				"INSERT INTO observer (" +
						"observer_id, " +
						"version, " +
						"name, " +
						"description, " +
						"version_string, " +
						"user_id) " +
					"VALUES (?, ?, ?, ?, ?, " +
						"(SELECT id FROM user WHERE username = ?))";

			// Create the probe.
			// FIXME: Doesn't do sanitization of the input.
			PreparedStatementCreator observerCreator =
				new PreparedStatementCreator() {

					@Override
					public PreparedStatement createPreparedStatement(
							final Connection connection)
							throws SQLException {
						
						PreparedStatement ps =
							connection.prepareStatement(
								observerSql,
								new String[] { "id" });
						
						ps.setString(1, observer.getId());
						ps.setLong(2, observer.getVersion());
						ps.setString(3, observer.getName());
						ps.setString(4, observer.getDescription());
						ps.setString(5, observer.getVersionString());
						ps.setString(6, username);
						
						return ps;
					}

				};
				
			KeyHolder observerKeyHolder = new GeneratedKeyHolder();
			
			try {
				getJdbcTemplate().update(observerCreator, observerKeyHolder);
			}
			catch(org.springframework.dao.DataAccessException e) {
				throw new DataAccessException(
					"Error executing SQL '" + 
						observerSql + 
						"' with parameters: " +
						observer.getId() + ", " +
						observer.getVersion() + ", " +
						observer.getName() + ", " +
						observer.getDescription() + ", " +
						observer.getVersionString(),
					e);
			}
			
			long observerKey = observerKeyHolder.getKey().longValue();
			
			final String streamSql =
				"INSERT INTO observer_stream (" +
					"stream_id, " +
					"version, " +
					"name, " +
					"description, " +
					"with_timestamp, " +
					"with_location, " +
					"stream_schema, " +
					"observer_id)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			for(Stream stream : observer.getObservers().values()) {
				try {
					getJdbcTemplate().update(
						streamSql,
						new Object[] {
							stream.getId(),
							stream.getVersion(),
							stream.getName(),
							stream.getDescription(),
							stream.getWithTimestamp(),
							stream.getWithLocation(),
							stream.getSchema().toString(),
							observerKey
						});
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException(
						"Error executing SQL '" + 
							streamSql + 
							"' with parameters: " +
							stream.getId() + ", " +
							stream.getVersion() + ", " +
							stream.getName() + ", " +
							stream.getDescription() + ", " +
							stream.getWithTimestamp() + ", " +
							stream.getWithLocation() + ", " +
							stream.getSchema().toString() + ", " +
							observerKey,
						e);
				}
			}

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

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#doesObserverExist(java.lang.String)
	 */
	@Override
	public boolean doesObserverExist(
			final String observerId)
			throws DataAccessException {
		
		String sql = 
			"SELECT EXISTS(SELECT id FROM observer WHERE observer_id = ?)";
		
		try {
			return 
				getJdbcTemplate().queryForObject(
					sql, 
					new Object[] { observerId },
					new SingleColumnRowMapper<Long>()) != 0;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sql +
					"' with parameter: " +
					observerId);
		}
	}
}