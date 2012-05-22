package org.ohmage.query.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
					transactionManager.rollback(status);
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

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IObserverQueries#getObserver(java.lang.String, long)
	 */
	@Override
	public Observer getObserver(
			final String id, 
			final long version)
			throws DataAccessException {
		
		if(id == null) {
			return null;
		}
		
		String observerSql =
			"SELECT observer_id, version, name, description, version_string " +
			"FROM observer o " +
			"WHERE o.observer_id = ? " +
			"AND o.version = ?";
		
		String streamSql = 
			"SELECT " +
				"os.stream_id, " +
				"os.version, " +
				"os.name, " +
				"os.description, " +
				"os.with_timestamp, " +
				"os.with_location, " +
				"os.stream_schema " +
			"FROM observer o, observer_stream os " +
			"WHERE o.observer_id = ? " +
			"AND o.version = ? " +
			"AND o.id = os.observer_id";
		
		final List<Observer.Stream> streams;
		try {
			streams = 
				getJdbcTemplate().query(
					streamSql, 
					new Object[] { id, version },
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
			return getJdbcTemplate().queryForObject(
				observerSql, 
				new Object[] { id, version },
				new RowMapper<Observer> () {
					/**
					 * Maps the row of data to a new observer.
					 */
					@Override
					public Observer mapRow(
							final ResultSet rs, 
							final int rowNum)
							throws SQLException {
						
						try {
							return new Observer(
								rs.getString("observer_id"),
								rs.getLong("version"),
								rs.getString("name"),
								rs.getString("description"),
								rs.getString("version_string"),
								streams);
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
				"Error executing SQL '" +
					observerSql + 
					"' with parameters: " +
					id + ", " + 
					version,
				e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					observerSql + 
					"' with parameters: " +
					id + ", " + 
					version,
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
			final long streamVersion) 
			throws DataAccessException {
		
		if(observerId == null) {
			return null;
		}
		else if(streamId == null) {
			return null;
		}
		
		String streamSql = 
			"SELECT " +
				"stream_id, " +
				"version, " +
				"name, " +
				"description, " +
				"with_timestamp, " +
				"with_location, " +
				"stream_schema " +
			"FROM observer o, observer_stream os " +
			"WHERE o.observer_id = ? " +
			"AND o.id = os.observer_id " +
			"AND os.stream_id = ? " +
			"AND os.version = ?";
		
		try {
			return 
				getJdbcTemplate().queryForObject(
					streamSql, 
					new Object[] { observerId, streamId, streamVersion },
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
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" +
					streamSql + 
					"' with parameters: " +
					observerId + ", " +
					streamId + ", " +
					streamVersion,
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
			final Collection<DataStream> data)
			throws DataAccessException {
		
		String sql =
			"INSERT INTO observer_stream_data (" +
				"stream_id, " +
				"user_id, " +
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
				"(SELECT id FROM observer_stream WHERE stream_id = ?), " +
				"(SELECT id FROM user WHERE username = ?), " +
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
			DateTime timestamp = null;
			Location location = null;
			if(metaData != null) {
				timestamp = metaData.getTimestamp();
				
				location = metaData.getLocation();
			}
				
			args.add(
				new Object[] {
					currData.getStream().getId(),
					username,
					(timestamp == null) ? null : timestamp.getMillis(),
					(timestamp == null) ? null : timestamp.getZone().getOffset(null),
					(timestamp == null) ? null : timestamp.getZone().getID(),
					(location == null) ? null : (new DateTime(location.getTime(), location.getTimeZone())).toString(),
					(location == null) ? null : location.getLatitude(),
					(location == null) ? null : location.getLongitude(),
					(location == null) ? null : location.getAccuracy(),
					(location == null) ? null : location.getProvider(),
					new String(currData.getBinaryData())
				}
			);
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a request audit.");
		
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
					"observer_stream os, " +
					"observer_stream_data osd");
		List<Object> parameters = new LinkedList<Object>();
		
		if(observerVersion != null) {
			builder.append(", observer o");
		}
		
		builder.append(
			"WHERE u.username = ? " +
			"AND osd.user_id = u.id " +
			"AND os.stream_id = ? " +
			"AND os.version = ? " +
			"AND osd.stream_id = os.id");
		parameters.add(username);
		parameters.add(stream.getId());
		parameters.add(stream.getVersion());
		
		if(observerVersion != null) {
			builder.append(
				" " +
				"AND o.observer_id = ? " +
				"AND o.version = ? " +
				"AND os.observer_id = o.id");
			parameters.add(observerId);
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
		
		builder.append(" LIMIT " + numToSkip + ", " + numToReturn);
		
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
									rs.getBytes("data"));
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
			throw new DataAccessException(e);
		}
	}
}