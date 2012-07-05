package org.ohmage.query.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.DataStream.MetaData;
import org.ohmage.domain.Location;
import org.ohmage.domain.Observer;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.IObserverQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
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
		
		if(username == null) {
			throw new DataAccessException("The username is null.");
		}
		if(observer == null) {
			throw new DataAccessException("The observer is null.");
		}

		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating an observer.");

		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager =
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Observer creation SQL.
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

			// Observer creation statement with parameters.
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
				
			// The auto-generated key for the observer.
			KeyHolder observerKeyHolder = new GeneratedKeyHolder();
			
			// Create the observer.
			try {
				getJdbcTemplate().update(observerCreator, observerKeyHolder);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
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
			
			// Stream creation SQL.
			final String streamSql =
				"INSERT INTO observer_stream (" +
					"stream_id, " +
					"version, " +
					"name, " +
					"description, " +
					"with_id, " +
					"with_timestamp, " +
					"with_location, " +
					"stream_schema)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			// For each stream, insert it and link it to the observer.
			for(final Stream stream : observer.getStreams().values()) {
				// This stream's creation statement with its parameters.
				PreparedStatementCreator streamCreator =
					new PreparedStatementCreator() {

						@Override
						public PreparedStatement createPreparedStatement(
								final Connection connection)
								throws SQLException {
							
							PreparedStatement ps =
								connection.prepareStatement(
									streamSql,
									new String[] { "id" });
							
							ps.setString(1, stream.getId());
							ps.setLong(2, stream.getVersion());
							ps.setString(3, stream.getName());
							ps.setString(4, stream.getDescription());
							ps.setBoolean(5, stream.getWithId());
							ps.setBoolean(6, stream.getWithTimestamp());
							ps.setBoolean(7, stream.getWithLocation());
							ps.setString(8, stream.getSchema().toString());
							
							return ps;
						}

					};
					
				// This stream's auto-generated key.
				KeyHolder streamKeyHolder = new GeneratedKeyHolder();
				
				// Create the stream.
				try {
					getJdbcTemplate().update(streamCreator, streamKeyHolder);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException(
						"Error executing SQL '" + 
							streamSql + 
							"' with parameters: " +
							stream.getId() + ", " +
							stream.getVersion() + ", " +
							stream.getName() + ", " +
							stream.getDescription() + ", " +
							stream.getWithId() + ", " +
							stream.getWithTimestamp() + ", " +
							stream.getWithLocation() + ", " +
							stream.getSchema().toString(),
						e);
				}
				
				// Link creation SQL.
				final String linkSql =
					"INSERT INTO observer_stream_link (" +
						"observer_id, " +
						"observer_stream_id) " +
					"VALUES (?, ?)";
				
				// Link the stream to the observer.
				try {
					getJdbcTemplate().update(
						linkSql,
						new Object[] { 
							observerKeyHolder.getKey().longValue(),
							streamKeyHolder.getKey().longValue()
						}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException(
						"Error executing SQL '" + 
							linkSql + 
							"' with parameters: " +
							observerKeyHolder.getKey().longValue() + ", " +
							streamKeyHolder.getKey().longValue(),
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

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getOwner(java.lang.String)
	 */
	@Override
	public String getOwner(
			final String observerId) 
			throws DataAccessException {
		
		if(observerId == null) {
			return null;
		}
		
		String sql = 
			"SELECT DISTINCT(u.username) " +
			"FROM user u, observer o " +
			"WHERE u.id = o.user_id " +
			"AND o.observer_id = ?";
		
		try {
			return 
				getJdbcTemplate().queryForObject(
					sql, 
					new Object[] { observerId },
					String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
					"Multiple observers have the same ID: " + observerId,
					e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"There was an error executing SQL '" +
					sql +
					"' with parameter: " +
					observerId,
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getObserver(java.lang.String, long)
	 */
	@Override
	public Observer getObserver(
			final String id, 
			final Long version)
			throws DataAccessException {
		
		if(id == null) {
			return null;
		}
		
		StringBuilder observerSql =
			new StringBuilder(
				"SELECT " +
					"id, " +
					"observer_id, " +
					"version, " +
					"name, " +
					"description, " +
					"version_string " +
				"FROM observer o " +
				"WHERE o.observer_id = ? ");
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(id);
		
		if(version == null) {
			observerSql.append("ORDER BY version DESC LIMIT 1");
		}
		else {
			observerSql.append("AND o.version = ?");
			parameters.add(version);
		}
		
		final Long observerId;
		final Observer.Builder observerBuilder = new Observer.Builder();
		try {
			List<Long> observerIds =
				getJdbcTemplate().query(
					observerSql.toString(), 
					parameters.toArray(),
					new RowMapper<Long> () {
						/**
						 * Maps the row of data to a new observer.
						 */
						@Override
						public Long mapRow(
								final ResultSet rs, 
								final int rowNum)
								throws SQLException {
						
							observerBuilder
								.setId(rs.getString("observer_id"))
								.setVersion(rs.getLong("version"))
								.setName(rs.getString("name"))
								.setDescription(rs.getString("description"))
								.setVersionString(
									rs.getString("version_string"));
		
							return rs.getLong("id");
						}
				
					}
				);
			
			if(observerIds.size() == 0) {
				return null;
			}
			else if(observerIds.size() > 1) {
				throw new DataAccessException(
					"Multiple observers have the same ID ('" +
						id +
						"') and version ('" +
						version +
						"').");
			}
			else {
				observerId = observerIds.get(0);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					observerSql.toString() + 
					"' with parameters: " +
					parameters,
				e);
		}
		
		String streamSql = 
			"SELECT " +
				"os.stream_id, " +
				"os.version, " +
				"os.name, " +
				"os.description, " +
				"os.with_id, " +
				"os.with_timestamp, " +
				"os.with_location, " +
				"os.stream_schema " +
			"FROM observer_stream os, observer_stream_link osl " +
			"WHERE osl.observer_id = ? " +
			"AND osl.observer_stream_id = os.id";
		
		try {
			observerBuilder.addStreams(
				getJdbcTemplate().query(
					streamSql, 
					new Object[] { observerId },
					new RowMapper<Observer.Stream>() {
						/**
						 * Maps the row of data to a new stream.
						 */
						@Override
						public Stream mapRow(
								final ResultSet rs, 
								final int rowNum)
								throws SQLException {
							
							try {
								return new Observer.Stream(
									rs.getString("stream_id"), 
									rs.getLong("version"), 
									rs.getString("name"), 
									rs.getString("description"), 
									rs.getBoolean("with_id"),
									rs.getBoolean("with_timestamp"), 
									rs.getBoolean("with_location"), 
									rs.getString("stream_schema"));
							}
							catch(DomainException e) {
								throw new SQLException(e);
							}
						}
						
					}
				)
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					streamSql + 
					"' with parameters: " +
					id + ", " + 
					version,
				e);
		}
		
		try {
			return observerBuilder.build();
		}
		catch(DomainException e) {
			throw new DataAccessException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getGreatestObserverVersion(java.lang.String)
	 */
	@Override
	public Long getGreatestObserverVersion(
			final String id)
			throws DataAccessException {
		
		String sql = 
			"SELECT MAX(version) AS max_version " +
			"FROM observer " +
			"WHERE observer_id = ?";
		
		try {
			return
				getJdbcTemplate().queryForLong(sql, new Object[] { id });
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sql + 
					"' with parameter: " +
					id,
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getStream(java.lang.String, java.lang.String, long)
	 */
	@Override
	public Stream getStream(
			final String observerId,
			final String streamId,
			final Long streamVersion) 
			throws DataAccessException {
		
		if(observerId == null) {
			return null;
		}
		else if(streamId == null) {
			return null;
		}
		
		StringBuilder streamSqlBuilder = 
			new StringBuilder(
				"SELECT " +
					"os.stream_id, " +
					"os.version, " +
					"os.name, " +
					"os.description, " +
					"os.with_id, " +
					"os.with_timestamp, " +
					"os.with_location, " +
					"os.stream_schema " +
				"FROM " +
					"observer o, " +
					"observer_stream os, " +
					"observer_stream_link osl " +
				"WHERE o.observer_id = ? " +
				"AND o.id = osl.observer_id " +
				"AND osl.observer_stream_id = os.id " +
				"AND os.stream_id = ? ");
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(observerId);
		parameters.add(streamId);
		
		if(streamVersion == null) {
			streamSqlBuilder.append("ORDER BY os.version LIMIT 1 ");
		}
		else {
			streamSqlBuilder.append("AND os.version = ? ");
			parameters.add(streamVersion);
		}
		
		streamSqlBuilder.append("GROUP BY os.stream_id, os.version");
		
		try {
			return 
				getJdbcTemplate().queryForObject(
					streamSqlBuilder.toString(), 
					parameters.toArray(),
					new RowMapper<Observer.Stream>() {
						/**
						 * Maps the row of data to a new stream.
						 */
						@Override
						public Stream mapRow(
								final ResultSet rs, 
								final int rowNum)
								throws SQLException {
							
							try {
								return new Observer.Stream(
									rs.getString("stream_id"), 
									rs.getLong("version"), 
									rs.getString("name"), 
									rs.getString("description"), 
									rs.getBoolean("with_id"),
									rs.getBoolean("with_timestamp"), 
									rs.getBoolean("with_location"), 
									rs.getString("stream_schema"));
							}
							catch(DomainException e) {
								throw new SQLException(e);
							}
						}
						
					}
				);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() == 0) {
				return null;
			}
			
			throw new DataAccessException(
				"Multiple streams in an observer ('" +
					observerId +
					"') have the same ID ('" + 
					streamId +
					"') and version ('" +
					streamVersion +
					"').",
				e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					streamSqlBuilder.toString() + 
					"' with parameters: " +
					parameters,
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getGreatestObserverVersion(java.lang.String)
	 */
	@Override
	public Long getGreatestStreamVersion(
			final String observerId,
			final String streamId)
			throws DataAccessException {
		
		String sql = 
			"SELECT MAX(os.version) AS max_version " +
			"FROM observer o, observer_stream os, observer_stream_link osl " +
			"WHERE o.observer_id = ? " +
			"AND os.stream_id = ? " +
			"AND o.id = osl.observer_id " +
			"AND osl.observer_stream_id = os.id";
		
		try {
			return
				getJdbcTemplate().queryForLong(
					sql, 
					new Object[] { observerId, streamId });
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sql + 
					"' with parameter: " +
					observerId + ", " +
					streamId,
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getObserverIdToStreamsMap()
	 */
	@Override
	public Map<String, Collection<Stream>> getObserverIdToStreamsMap()
			throws DataAccessException {
		
		final Map<String, Collection<Stream>> result = 
			new HashMap<String, Collection<Stream>>();
		
		final String sql =
			"SELECT " +
				"o.observer_id, " +
				"os.stream_id, " +
				"os.version, " +
				"os.name, " +
				"os.description, " +
				"os.with_id, " +
				"os.with_timestamp, " +
				"os.with_location, " +
				"os.stream_schema " +
			"FROM observer o, observer_stream os, observer_stream_link osl " +
			"WHERE o.id = osl.observer_id " +
			"AND os.id = osl.observer_stream_id " +
			"GROUP BY os.stream_id, os.version";
		
		try {
			getJdbcTemplate().query(
				sql,
				new RowMapper<Object>() {
					/**
					 * Adds each of the streams to their corresponding list.
					 */
					@Override
					public Object mapRow(
							final ResultSet rs, 
							final int rowNum)
							throws SQLException {

						String observerId = rs.getString("observer_id");
						
						Stream stream;
						try {
							stream = 
								new Observer.Stream(
									rs.getString("stream_id"), 
									rs.getLong("version"), 
									rs.getString("name"), 
									rs.getString("description"), 
									rs.getBoolean("with_id"),
									rs.getBoolean("with_timestamp"), 
									rs.getBoolean("with_location"), 
									rs.getString("stream_schema"));
						}
						catch(DomainException e) {
							throw new SQLException(e);
						}
						
						Collection<Stream> streams = result.get(observerId);
						if(streams == null) {
							streams = new LinkedList<Stream>();
							result.put(observerId, streams);
						}
						streams.add(stream);
						
						return null;
					}
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sql +
					"'.",
				e);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getDuplicateIds(java.lang.String, java.lang.String, java.util.Collection)
	 */
	@Override
	public Collection<String> getDuplicateIds(
			final String username,
			final String observerId,
			final String streamId,
			final Collection<String> idsToCheck)
			throws DataAccessException {
		
		int numIds = idsToCheck.size();
		if(numIds == 0) {
			return Collections.emptyList();
		}
		
		StringBuilder sqlBuilder =
			new StringBuilder(
				"SELECT osd.uid " +
				"FROM " +
					"user u, " +
					"observer o, " +
					"observer_stream os, " +
					"observer_stream_link osl, " +
					"observer_stream_data osd " +
				"WHERE u.username = ? " +
				"AND o.observer_id = ? " +
				"AND o.id = osl.observer_id " +
				"AND osl.observer_stream_id = os.id " +
				"AND os.stream_id = ? " +
				"AND u.id = osd.user_id " +
				"AND osl.id = osd.observer_stream_link_id " +
				"AND osd.uid IN ");
		
		sqlBuilder.append(StringUtils.generateStatementPList(numIds));
		
		Collection<Object> parameters = 
			new ArrayList<Object>(idsToCheck.size() + 2);
		parameters.add(username);
		parameters.add(observerId);
		parameters.add(streamId);
		parameters.addAll(idsToCheck);
		
		try {
			return
				getJdbcTemplate().query(
					sqlBuilder.toString(),
					parameters.toArray(),
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + 
					sqlBuilder.toString() +
					"' with parameters: " +
					parameters,
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#storeData(java.lang.String, java.util.Collection)
	 */
	@Override
	public void storeData(
			final String username,
			final Observer observer,
			final Collection<DataStream> data)
			throws DataAccessException {
		
		String sql =
			"INSERT INTO observer_stream_data (" +
				"user_id, " +
				"observer_stream_link_id, " +
				"uid, " +
				"time, " +
				"time_offset, " +
				"time_zone, " +
				"location_timestamp, " +
				"location_latitude, " +
				"location_longitude, " +
				"location_accuracy, " +
				"location_provider, " +
				"data) " +
			"VALUES (" +
				"(SELECT id FROM user WHERE username = ?), " +
				"(" +
					"SELECT osl.id " +
					"FROM " +
						"observer o, " +
						"observer_stream os, " +
						"observer_stream_link osl " +
					"WHERE o.observer_id = ? " +
					"AND o.version = ? " +
					"AND os.stream_id = ? " +
					"AND os.version = ? " +
					"AND o.id = osl.observer_id " +
					"AND os.id = osl.observer_stream_id" +
				"), " +
				"?, " +
				"?, " +
				"?, " +
				"?, " +
				"?, " +
				"?, " +
				"?, " +
				"?, " +
				"?, " +
				"?)";
		
		List<Object[]> args = new ArrayList<Object[]>(data.size());
		for(DataStream currData : data) {
			MetaData metaData = currData.getMetaData();
			String id = null;
			DateTime timestamp = null;
			Location location = null;
			if(metaData != null) {
				id = metaData.getId();
				timestamp = metaData.getTimestamp();
				location = metaData.getLocation();
			}
			
			try {
				args.add(
					new Object[] {
						username,
						observer.getId(),
						observer.getVersion(),
						currData.getStream().getId(),
						currData.getStream().getVersion(),
						id,
						(timestamp == null) ? null : timestamp.getMillis(),
						(timestamp == null) ? null : timestamp.getZone().getOffset(null),
						(timestamp == null) ? null : timestamp.getZone().getID(),
						(location == null) ? null : (new DateTime(location.getTime(), location.getTimeZone())).toString(),
						(location == null) ? null : location.getLatitude(),
						(location == null) ? null : location.getLongitude(),
						(location == null) ? null : location.getAccuracy(),
						(location == null) ? null : location.getProvider(),
						currData.getBinaryData()
					}
				);
			}
			catch(DomainException e) {
				throw new DataAccessException(
					"Could not get the binary data.",
					e);
			}
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Inserting stream data.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().batchUpdate(sql, args);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + sql +"'.", 
					e);
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
	 * @see org.ohmage.query.IObserverQueries#readData(org.ohmage.domain.Observer.Stream, java.lang.Long, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
	 */
	@Override
	public List<DataStream> readData(
			final Stream stream,
			final String username,
			final String observerId,
			final Long observerVersion,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn) 
			throws DataAccessException {
		
		StringBuilder builder = 
			new StringBuilder(
				"SELECT " +
					"osd.uid, " +
					"osd.time, " +
					"osd.time_zone, " +
					"osd.location_timestamp, " +
					"osd.location_latitude, " +
					"osd.location_longitude, " +
					"osd.location_accuracy, " +
					"osd.location_provider, " +
					"osd.data " +
				"FROM " +
					"user u, " +
					"observer o, " +
					"observer_stream os, " +
					"observer_stream_link osl, " +
					"observer_stream_data osd " +
				"WHERE u.username = ? " +
					"AND o.observer_id = ? " +
					"AND os.stream_id = ? " +
					"AND os.version = ? " +
					"AND o.id = osl.observer_id " +
					"AND os.id = osl.observer_stream_id " +
					"AND osl.id = osd.observer_stream_link_id " +
					"AND u.id = osd.user_id");
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(username);
		parameters.add(observerId);
		parameters.add(stream.getId());
		parameters.add(stream.getVersion());
		
		if(observerVersion != null) {
			builder.append(" AND o.version = ?");
			parameters.add(observerVersion);
		}
		
		if(startDate != null) {
			builder.append(" AND (osd.time + osd.time_offset) >= ?");
			parameters.add(startDate.getMillis());
		}
		
		if(endDate != null) {
			builder.append(" AND (osd.time + osd.time_offset) <= ?");
			parameters.add(endDate.getMillis());
		}
		
		builder.append(" ORDER BY osd.time");
		builder
			.append(" LIMIT ")
			.append(numToSkip)
			.append(", ")
			.append(numToReturn);
		
		try {
			return
				getJdbcTemplate().query(
					builder.toString(),
					parameters.toArray(),
					new RowMapper<DataStream>() {
						/**
						 * Decodes the resulting data into a data stream.
						 */
						@Override
						public DataStream mapRow(
								final ResultSet rs, 
								final int rowNum)
								throws SQLException {
							
							MetaData.Builder metaDataBuilder =
								new MetaData.Builder();
							
							String id = rs.getString("osd.uid");
							if(id != null) {
								metaDataBuilder.setId(id);
							}
							
							Long time = rs.getLong("osd.time");
							if(time != null) {
								metaDataBuilder.setTimestamp(
									new DateTime(
										time,
										DateTimeZone.forID(
											rs.getString("osd.time_zone"))));
							}
							
							String locationTimestampString = 
								rs.getString("location_timestamp");
							if(locationTimestampString != null) {
								Location location;
								try {
									location =
										new Location(
											ISODateTimeFormat
												.dateTime()
												.parseDateTime(
													rs.getString(
														"osd.location_timestamp")),
											rs.getDouble("osd.location_latitude"),
											rs.getDouble("osd.location_longitude"),
											rs.getDouble("osd.location_accuracy"),
											rs.getString("osd.location_provider"));
								}
								catch(IllegalArgumentException e) {
									throw new SQLException(
										"The timestamp in the database is corrupted.",
										e);
								}
								catch(NullPointerException e) {
									throw new SQLException(
										"A double in the database is corrupted.",
										e);
								}
								catch(DomainException e) {
									throw new SQLException(
										"Could not create the location object.",
										e);
								}
								
								metaDataBuilder.setLocation(location);
							}
							
							try {
								return new DataStream(
									stream, 
									metaDataBuilder.build(), 
									rs.getBytes("osd.data"));
							}
							catch(DomainException e) {
								throw new SQLException(
									"Could not create the data stream.",
									e);
							}
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + 
					builder.toString() + 
					"' with parameters: " +
					parameters,
				e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#updateObserver(java.lang.String, org.ohmage.domain.Observer, java.util.Collection)
	 */
	@Override
	public void updateObserver(
			final String username,
			final Observer observer,
			final Map<String, Long> unchangedStreamIds)
			throws DataAccessException {
		
		if(username == null) {
			throw new DataAccessException("The username is null.");
		}
		if(observer == null) {
			throw new DataAccessException("The observer is null.");
		}
		if(unchangedStreamIds == null) {
			throw new DataAccessException(
				"The collection of unchanged IDs is null.");
		}

		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating an observer.");

		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager =
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Observer creation SQL.
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

			// Observer creation statement with parameters.
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
				
			// The auto-generated key for the observer.
			KeyHolder observerKeyHolder = new GeneratedKeyHolder();
			
			// Create the observer.
			try {
				getJdbcTemplate().update(observerCreator, observerKeyHolder);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
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
			
			// Get the observer ID.
			long observerDbId = observerKeyHolder.getKey().longValue();
			
			// Create the map of stream IDs to their versions. The initial part
			// of the map will be based on the pre-existing streams.
			Map<String, Long> streamIdAndVersions = 
				new HashMap<String, Long>(unchangedStreamIds);
			
			// Stream creation SQL.
			final String streamSql =
				"INSERT INTO observer_stream (" +
					"stream_id, " +
					"version, " +
					"name, " +
					"description, " +
					"with_id, " +
					"with_timestamp, " +
					"with_location, " +
					"stream_schema)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			// For each stream, insert it and link it to the observer.
			List<Object[]> streamArgs = 
				new ArrayList<Object[]>(unchangedStreamIds.size());
			for(final Stream stream : observer.getStreams().values()) {
				// Get the stream's ID.
				String streamId = stream.getId();
				
				// If the stream already exists in the map of stream IDs and
				// versions, then skip it.
				if(streamIdAndVersions.containsKey(streamId)) {
					continue;
				}
				// Otherwise, add it to the map and then add it to the 
				// database.
				else {
					streamIdAndVersions.put(streamId, stream.getVersion());
				}
				
				Object[] currArgs = new Object[8];
				
				currArgs[0] = streamId;
				currArgs[1] = stream.getVersion();
				currArgs[2] = stream.getName();
				currArgs[3] = stream.getDescription();
				currArgs[4] = stream.getWithId();
				currArgs[5] = stream.getWithTimestamp();
				currArgs[6] = stream.getWithLocation();
				currArgs[7] = stream.getSchema().toString();
				
				streamArgs.add(currArgs);
			}
			
			// Create the streams.
			try {
				getJdbcTemplate().batchUpdate(streamSql, streamArgs);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + streamSql + "'.",
					e);
			}
			
			// Observer-stream link SQL.
			final String observerStreamSql =
				"INSERT INTO observer_stream_link(" +
					"observer_id, " +
					"observer_stream_id" +
				") " +
				"VALUES (" +
					"?, " +
					"(" +
						"SELECT id " +
						"FROM observer_stream os " +
						"WHERE os.stream_id = ? " +
						"AND os.version = ?" +
					")" +
				")";
			
			// Add all of the streams that need to be associated to a list of
			// arguments.
			List<Object[]> args = 
				new ArrayList<Object[]>(streamIdAndVersions.size());
			for(String streamId : streamIdAndVersions.keySet()) {
				Object[] currArgs = new Object[3];
				
				currArgs[0] = observerDbId;
				currArgs[1] = streamId;
				currArgs[2] = streamIdAndVersions.get(streamId);
				
				args.add(currArgs);
			}
			
			// Associate the streams with the new observer in a batch.
			try {
				getJdbcTemplate().batchUpdate(observerStreamSql, args);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + observerStreamSql +"'.", 
					e);
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
}