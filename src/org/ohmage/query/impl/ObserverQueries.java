package org.ohmage.query.impl;

import java.io.IOException;
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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
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
							ps.setObject(5, stream.getWithId());
							ps.setObject(6, stream.getWithTimestamp());
							ps.setObject(7, stream.getWithLocation());
							
							try {
								ps.setString(
									8, 
									stream
										.getSchema()
										.readValueAsTree()
										.toString());
							}
							catch(JsonParseException e) {
								throw new SQLException(
									"The schema is not valid JSON.",
									e);
							}
							catch(IOException e) {
								throw new SQLException(
									"Could not read the schema string.",
									e);
							}
							
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
	 * @see org.ohmage.query.IObserverQueries#getObservers(java.lang.String, java.lang.Long, long, long)
	 */
	public List<Observer> getObservers(
			final String id,
			final Long version,
			final long numToSkip,
			final long numToReturn)
			throws DataAccessException {
		
		// Build the SQL that will get all of the observers visible to the 
		// requesting user.
		StringBuilder observerSql =
			new StringBuilder(
				"SELECT " +
					"id, " +
					"observer_id, " +
					"version, " +
					"name, " +
					"description, " +
					"version_string " +
				"FROM observer o");

		List<String> whereClauses = new LinkedList<String>();
		List<Object> parameters = new LinkedList<Object>();
		
		// Add the ID if it is present.
		if(id != null) {
			whereClauses.add("o.observer_id = ?");
			parameters.add(id);
		}
		
		// Add the version if present.
		if(version != null) {
			whereClauses.add("o.version = ?");
			parameters.add(version);
		}
		
		// Add the WHERE clauses to the query.
		boolean firstPass = true;
		for(String whereClause : whereClauses) {
			if(firstPass) {
				observerSql.append(" WHERE ");
				firstPass = false;
			}
			else {
				observerSql.append(" AND ");
			}
			
			observerSql.append(whereClause);
		}
		
		observerSql.append(" ORDER BY o.observer_id ASC, o.version DESC");
		
		observerSql.append(" LIMIT ?, ?");
		parameters.add(numToSkip);
		parameters.add(numToReturn);
		
		final Map<Long, Observer.Builder> observerBuilders = 
			new HashMap<Long, Observer.Builder>();
		try {
			getJdbcTemplate().query(
				observerSql.toString(),
				parameters.toArray(),
				new RowMapper<Object> () {
					/**
					 * Maps the row of data to a new observer builder.
					 */
					@Override
					public Object mapRow(
							final ResultSet rs, 
							final int rowNum)
							throws SQLException {
					
						Observer.Builder observerBuilder = 
							new Observer.Builder();
						
						observerBuilder
							.setId(rs.getString("observer_id"))
							.setVersion(rs.getLong("version"))
							.setName(rs.getString("name"))
							.setDescription(rs.getString("description"))
							.setVersionString(
								rs.getString("version_string"));
	
						observerBuilders.put(
							rs.getLong("id"), 
							observerBuilder);
						
						return null;
					}
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					observerSql +
					"' with parameters: " +
					parameters,
				e);
		}
		
		final String streamSql = 
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
		
		for(Long dbId : observerBuilders.keySet()) {
			try {
				observerBuilders
				.get(dbId)
				.addStreams(
					getJdbcTemplate().query(
						streamSql, 
						new Object[] { dbId },
						new RowMapper<Observer.Stream>() {
							/**
							 * Maps the row of data to a new stream.
							 */
							@Override
							public Stream mapRow(
									final ResultSet rs, 
									final int rowNum)
									throws SQLException {
								
								// Because the with_* values are optional and
								// may be null, they must be retrieve in this
								// special way.
								Boolean withId, withTimestamp, withLocation;
								withId = rs.getBoolean("with_id");
								if(rs.wasNull()) {
									withId = null;
								}
								withTimestamp =
									rs.getBoolean("with_timestamp");
								if(rs.wasNull()) {
									withTimestamp = null;
								}
								withLocation = rs.getBoolean("with_location");
								if(rs.wasNull()) {
									withLocation = null;
								}
								
								try {
									return new Observer.Stream(
										rs.getString("stream_id"), 
										rs.getLong("version"), 
										rs.getString("name"), 
										rs.getString("description"), 
										withId,
										withTimestamp, 
										withLocation, 
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
						"' with parameter: " +
						dbId,
					e);
			}
		}
		
		ArrayList<Observer> result = 
			new ArrayList<Observer>(observerBuilders.size());
		for(Observer.Builder observerBuilder : observerBuilders.values()) {
			try {
				result.add(observerBuilder.build());
			}
			catch(DomainException e) {
				throw new DataAccessException(
					"There was a problem building an observer.",
					e);
			}
		}
		
		return result;
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
	 * @see org.ohmage.query.IObserverQueries#getStreams(java.lang.String, java.lang.Long, java.lang.String, java.lang.Long, long, long)
	 */
	@Override
	public Map<String, Collection<Observer.Stream>> getStreams(
			final String username,
			final String observerId,
			final Long observerVersion,
			final String streamId,
			final Long streamVersion,
			final long numToSkip,
			final long numToReturn)
			throws DataAccessException {
		
		if(numToReturn == 0) {
			return Collections.emptyMap();
		}
		
		// Create the default SQL.
		StringBuilder sqlBuilder = 
			new StringBuilder(
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
				"FROM " +
					"observer o, " +
					"observer_stream os, " +
					"observer_stream_link osl " +
				"WHERE o.id = osl.observer_id " +
				"AND os.id = osl.observer_stream_id");

		// Create the default set of parameters.
		List<Object> parameters = new LinkedList<Object>();
		
		if(username != null) {
			sqlBuilder
				.append(
					" AND EXISTS (" +
						"SELECT osd.id " +
						"FROM user u, observer_stream_data osd " +
						"WHERE u.username = ? " +
						"AND u.id = osd.user_id " +
						"AND osl.id = osd.observer_stream_link_id" +
					")"
				);
			parameters.add(username);
		}
			
		// If querying about the observer's ID, add that WHERE clause and
		// add the parameter.
		if(observerId != null) {
			sqlBuilder.append(" AND o.observer_id = ?");
			parameters.add(observerId);
		}
		
		// If querying about the observer's version, add that WHERE clause 
		// and add the parameter.
		if(observerVersion != null) {
			sqlBuilder.append(" AND o.version = ?");
			parameters.add(observerVersion);
		}

		// If querying about the stream's ID add the WHERE clause and the
		// parameter.
		if(streamId != null) {
			sqlBuilder.append(" AND os.stream_id = ?");
			parameters.add(streamId);
		}

		// If querying about the stream's version add the WHERE clause and the
		// parameter.
		if(streamVersion != null) {
			sqlBuilder.append(" AND os.version = ?");
			parameters.add(streamVersion);
		}
		
		// Add ordering to facilitate paging.
		sqlBuilder
			.append(
				" ORDER BY o.observer_id, o.version, os.stream_id, os.version");
		
		// Add the limits for paging.
		sqlBuilder.append(" LIMIT ?, ?");
		parameters.add(numToSkip);
		parameters.add(numToReturn);
		
		// Query for the results and add them to the result map.
		final Map<String, Collection<Observer.Stream>> result =
			new HashMap<String, Collection<Observer.Stream>>();
		try {
			getJdbcTemplate().query(
				sqlBuilder.toString(), 
				parameters.toArray(),
				new RowMapper<Object>() {
					/**
					 * Maps the row of data to a new stream.
					 */
					@Override
					public Object mapRow(
							final ResultSet rs, 
							final int rowNum)
							throws SQLException {
						
						// Get the observer's ID.
						String observerId = rs.getString("observer_id");
						
						// Get the streams from the result.
						Collection<Observer.Stream> streams =
							result.get(observerId);
						
						// If no collection exists for that observer ID yet,
						// create a new collection and add it.
						if(streams == null) {
							streams = new LinkedList<Observer.Stream>();
							result.put(observerId, streams);
						}
						
						// Because the with_* values are optional and
						// may be null, they must be retrieve in this
						// special way.
						Boolean withId, withTimestamp, withLocation;
						withId = rs.getBoolean("with_id");
						if(rs.wasNull()) {
							withId = null;
						}
						withTimestamp =
							rs.getBoolean("with_timestamp");
						if(rs.wasNull()) {
							withTimestamp = null;
						}
						withLocation = rs.getBoolean("with_location");
						if(rs.wasNull()) {
							withLocation = null;
						}
						
						// Add the stream to its respective result list.
						try {
							streams
								.add(
									new Observer.Stream(
										rs.getString("stream_id"), 
										rs.getLong("version"), 
										rs.getString("name"), 
										rs.getString("description"), 
										withId,
										withTimestamp, 
										withLocation, 
										rs.getString("stream_schema")));
						}
						catch(DomainException e) {
							throw new SQLException(e);
						}
						
						// Return nothing as it will never be used.
						return null;
					}
					
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					sqlBuilder.toString() + 
					"' with parameters: " +
					parameters,
				e);
		}
		
		// Return the result.
		return result;
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
				"time_adjusted, " +
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
			
			Long time = (timestamp == null) ? null : timestamp.getMillis();
			Integer timeOffset = 
				(timestamp == null) ? 
					null : 
					timestamp.getZone().getOffset(timestamp);
			Long timeAdjusted =
				(timestamp == null) ? null : time + timeOffset;
			String timeZoneId = 
				(timestamp == null) ? null : timestamp.getZone().getID();
			
			args.add(
				new Object[] {
					username,
					observer.getId(),
					observer.getVersion(),
					currData.getStream().getId(),
					currData.getStream().getVersion(),
					id,
					time,
					timeOffset,
					timeAdjusted,
					timeZoneId,
					(location == null) ? null : (new DateTime(location.getTime(), location.getTimeZone())).toString(),
					(location == null) ? null : location.getLatitude(),
					(location == null) ? null : location.getLongitude(),
					(location == null) ? null : location.getAccuracy(),
					(location == null) ? null : location.getProvider(),
					currData.getData().toString()
				}
			);
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
	 * @see org.ohmage.query.IObserverQueries#readData(org.ohmage.domain.Observer.Stream, java.lang.String, java.lang.String, java.lang.Long, org.joda.time.DateTime, org.joda.time.DateTime, boolean, long, long)
	 */
	@Override
	public List<DataStream> readData(
			final Stream stream,
			final String username,
			final String observerId,
			final Long observerVersion,
			final DateTime startDate,
			final DateTime endDate,
			final boolean chronological,
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
					"observer_stream_data osd " +
				// We use sub-queries here instead of a single join because it
				// makes the sorting much faster. A single join creates a giant
				// table of all data points multiplied by the number of users, 
				// the number of observers, the number of streams, and the
				// number of links between observers and streams, then that
				// list is sorted. Sub-queries prune out the data to only the
				// subset of all data points for a given user, then that list
				// is sorted.
				"WHERE " +
					"osd.user_id = (" +
						"SELECT id " +
						"FROM user " +
						"WHERE username = ?" +
					")" +
					"AND osd.observer_stream_link_id IN (" +
						"SELECT osl.id " +
						"FROM " +
							"observer o, " +
							"observer_stream os, " +
							"observer_stream_link osl " +
						"WHERE " +
							"o.observer_id = ? " +
							"AND os.stream_id = ? " +
							"AND os.version = ? " +
							"AND o.id = osl.observer_id " +
							"AND os.id = osl.observer_stream_id");
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(username);
		parameters.add(observerId);
		parameters.add(stream.getId());
		parameters.add(stream.getVersion());
		
		// If the observer's version is specified, add it to the sub-query.
		if(observerVersion != null) {
			builder.append(" AND o.version = ?)");
			parameters.add(observerVersion);
		}
		// Otherwise, end the subquery.
		else {
			builder.append(')');
		}
		
		// If a start date is given, add it to the overall query.
		if(startDate != null) {
			builder.append(" AND osd.time_adjusted >= ?");
			parameters.add(startDate.getMillis());
		}

		// If an end date is given, add it to the overall query.
		if(endDate != null) {
			builder.append(" AND osd.time_adjusted <= ?");
			parameters.add(endDate.getMillis());
		}
		
		// Add the ordering based on whether or not these should be 
		// chronological or reverse chronological.
		builder
			.append(
				" ORDER BY osd.time " + ((chronological) ? "ASC" : "DESC"));
		
		// Limit the number of results based on the paging.
		builder
			.append(" LIMIT ")
			.append(numToSkip)
			.append(", ")
			.append(numToReturn);
		
		// Create a JSON factory, which will be used by each data point to
		// deserialize its data into a JsonNode.
		final JsonFactory jsonFactory = new MappingJsonFactory();
		
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
							
							JsonNode data;
							try {
								JsonParser parser =
									jsonFactory
										.createJsonParser(
											rs.getString("osd.data"));
								data = parser.readValueAsTree();
							}
							catch(JsonParseException e) {
								throw new SQLException(
									"The data in the database is invalid: " +
										id,
									e);
							}
							catch(IOException e) {
								throw new SQLException(
									"There was a problem reading the data: " +
										id,
									e);
							}
							
							try {
								return new DataStream(
									stream, 
									metaDataBuilder.build(), 
									data);
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
					"stream_schema) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			// Observer-stream link SQL.
			final String observerStreamSql =
				"INSERT INTO observer_stream_link(" +
					"observer_id, " +
					"observer_stream_id" +
				") " +
				"VALUES (" +
					"?, " +
					"?" +
				")";
			
			// If all we are doing is grabbing the database ID for the stream,
			// then this will take care of that. Note: The observer's ID must
			// be put in twice to help get the correct version.
			final String getStreamIdSql =
				"SELECT os.id " +
					"FROM " +
						"observer o, " +
						"observer_stream os, " +
						"observer_stream_link osl " +
					"WHERE o.observer_id = ? " +
					"AND o.version = " +
						"(" +
							"SELECT version " +
							"FROM observer " +
							"WHERE observer_id = ? " +
							"ORDER BY version DESC " +
							"LIMIT 1, 1" +
						") " +
					"AND os.stream_id = ? " +
					"AND os.version = ? " +
					"AND o.id = osl.observer_id " +
					"AND os.id = osl.observer_stream_id";
			
			// For each stream, insert it and link it to the observer.
			for(final Stream stream : observer.getStreams().values()) {
				// Get the stream's ID.
				final String streamId = stream.getId();
				
				// The stream's database identifier.
				Long streamDbId;
				
				// If the stream already exists in the map of stream IDs and
				// versions, then get the stream's database ID for linking.
				if(unchangedStreamIds.containsKey(streamId)) {
					// Attempt to get the stream's database ID.
					try {
						streamDbId =
							getJdbcTemplate()
								.queryForLong(
									getStreamIdSql,
									new Object[] {
										observer.getId(),
										observer.getId(),
										streamId,
										stream.getVersion()
									}
								);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Error executing SQL '" +
								getStreamIdSql +
								"' with parameters: " +
								observer.getId() + ", " +
								observer.getId() + ", " +
								streamId + ", " +
								stream.getVersion(),
							e);
					}
				}
				// Otherwise, add the new stream and retain its database ID.
				else {
					// Get the stream's schema as a string.
					final String schema;
					try {
						schema = stream.getSchema().readValueAsTree().toString();
					}
					catch(JsonProcessingException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Error parsing the schema.",
							e);
					}
					catch(IOException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Error reading the schema.",
							e);
					}
					
					// Stream creation statement with parameters.
					PreparedStatementCreator streamCreator =
						new PreparedStatementCreator() {
							/**
							 * Create the stream insertion statement.
							 */
							@Override
							public PreparedStatement createPreparedStatement(
									final Connection connection)
									throws SQLException {
								
								PreparedStatement ps =
									connection.prepareStatement(
										streamSql,
										new String[] { "id" });
								
								ps.setString(1, streamId);
								ps.setLong(2, stream.getVersion());
								ps.setString(3, stream.getName());
								ps.setString(4, stream.getDescription());
								ps.setBoolean(5, stream.getWithId());
								ps.setBoolean(6, stream.getWithTimestamp());
								ps.setBoolean(7, stream.getWithLocation());
								ps.setString(8, schema);
								
								return ps;
							}
	
						};
						
					// The auto-generated key for the observer.
					KeyHolder streamKeyHolder = new GeneratedKeyHolder();
					
					// Create the observer.
					try {
						getJdbcTemplate().update(streamCreator, streamKeyHolder);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
							"Error executing SQL '" + 
								streamSql + 
								"' with parameters: " +
								streamId + ", " +
								stream.getVersion() + ", " +
								stream.getName() + ", " +
								stream.getDescription() + ", " +
								stream.getWithId() + ", " +
								stream.getWithTimestamp() + ", " +
								stream.getWithLocation() + ", " +
								schema,
							e);
					}
					
					// Get the observer ID.
					streamDbId = streamKeyHolder.getKey().longValue();
				}
				
				// Link the stream to the observer.
				try {
					getJdbcTemplate()
						.update(
							observerStreamSql, 
							new Object[] { observerDbId, streamDbId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException(
						"Error executing SQL '" +
							observerStreamSql +
							"' with parameters: " +
							observerDbId + ", " +
							streamDbId,
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
}