package org.ohmage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.AuditInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.jee.servlet.RequestServlet;
import org.ohmage.jee.servlet.RequestServlet.RequestType;
import org.ohmage.validator.AuditValidators.ResponseType;
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
 * This class is responsible for creating and reading request audits.
 * 
 * @author John Jenkins
 */
public class AuditDaos extends Dao {
	// Retrieves the audit ID for all audits.
	private static final String SQL_GET_AUDIT_IDS =
		"SELECT id " +
		"FROM audit";
	
	// Retrieves the audit ID for all audits with a specified request type.
	private static final String SQL_GET_AUDIT_IDS_WITH_TYPE =
		"SELECT a.id " +
		"FROM audit a, audit_request_type art " +
		"WHERE art.request_type = ?" +
		"AND art.id = a.request_type_id";
	
	// Retrieves the audit ID for all audits with a specified URI.
	private static final String SQL_GET_AUDIT_IDS_WITH_URI = 
		"SELECT id " +
		"FROM audit " +
		"WHERE uri = ?";
	
	// Retrieves the audit ID for all audits with a specified URI.
	private static final String SQL_GET_AUDIT_IDS_WITH_CLIENT = 
		"SELECT id " +
		"FROM audit " +
		"WHERE client = ?";
	
	// Retrieves the audit ID for all audits with a specified URI.
	private static final String SQL_GET_AUDIT_IDS_WITH_DEVICE_ID = 
		"SELECT id " +
		"FROM audit " +
		"WHERE device_id = ?";
	
	// Retrieves the audit ID for all audits whose response was "success".
	private static final String SQL_GET_AUDIT_IDS_WITH_SUCCESS_RESPONSE =
		"SELECT id " +
		"FROM audit " +
		"WHERE response like '%\"result\":\"success\"%'";
	
	// Retrieves the audit ID for all audits whose response was "success".
	private static final String SQL_GET_AUDIT_IDS_WITH_FAILURE_RESPONSE =
		"SELECT id " +
		"FROM audit " +
		"WHERE response like '%\"result\":\"failure\"%'";
	
	// Retrieves the audit ID for all audits whose response was "success".
	private static final String SQL_GET_AUDIT_IDS_WITH_FAILURE_RESPONSE_WITH_CODE =
		"SELECT id " +
		"FROM audit " +
		"WHERE response like '%\"result\":\"failure\"%' " +
		"AND response like '%\"code\":\"' ? '\"%'";
	
	// Retrieves the audit ID for all audits made on or after some date.
	private static final String SQL_GET_AUDIT_IDS_ON_OR_AFTER_DATE =
		"SELECT id " +
		"FROM audit " +
		"WHERE db_timestamp >= ?";
	
	// Retrieves the audit ID for all audits made on or before some date.
	private static final String SQL_GET_AUDIT_IDS_ON_OR_BEFORE_DATE = 
		"SELECT id " +
		"FROM audit " +
		"WHERE db_timestamp <= ?";
	
	// Retrieves the audit ID for all audits made between two dates, inclusive.
	private static final String SQL_GET_AUDIT_IDS_ON_OR_BETWEEN_DATES = 
		"SELECT id " +
		"FROM audit " +
		"WHERE db_timestamp >= ? " +
		"AND db_timestamp <= ?";
	
	// Retrieves all the information about a single audit entry.
	private static final String SQL_GET_AUDIT_INFORMATION_FROM_ID =
		"SELECT art.request_type, a.uri, a.client, a.device_id, a.response, a.received_millis, a.respond_millis, a.db_timestamp " +
		"FROM audit a, audit_request_type art " +
		"WHERE a.request_type_id = art.id " +
		"AND a.id = ?";
	
	// Retrieves all of the parameters for an audit.
	private static final String SQL_GET_AUDIT_PARAMETERS =
		"SELECT param_key, param_value " +
		"FROM audit_parameter " +
		"WHERE audit_id = ? ";
	
	// Retrieves all of the extras for an audit.
	private static final String SQL_GET_AUDIT_EXTRAS =
		"SELECT extra_key, extra_value " +
		"FROM audit_extra " +
		"WHERE audit_id = ? ";
	
	// Creates a new audit entry.
	private static final String SQL_INSERT_AUDIT =
		"INSERT INTO audit(request_type_id, uri, client, device_id, response, received_millis, respond_millis) " +
		"VALUES ((" +
			"SELECT id " +
			"FROM audit_request_type " +
			"WHERE request_type = ?" +
		"), ?, ?, ?, ?, ?, ?)";
	
	// Adds a parameter to an audit.
	private static final String SQL_INSERT_PARAMETER =
		"INSERT INTO audit_parameter(audit_id, param_key, param_value) " +
		"VALUES ((" +
			"SELECT id " +
			"FROM audit " +
			"WHERE id = ?" +
		"), ?, ?)";
	
	// Adds an extra from the HTTP request's header to an audit.
	private static final String SQL_INSERT_EXTRA =
		"INSERT INTO audit_extra(audit_id, extra_key, extra_value) " +
		"VALUES ((" +
			"SELECT id " +
			"FROM audit " +
			"WHERE id = ?" +
		"), ?, ?)";
	
	private static AuditDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private AuditDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Creates an audit entry with the parameterized information. Not all 
	 * information is required; see the specific parameters for details.
	 * 
	 * @param requestType The RequestType of the request. Required.
	 * 
	 * @param uri The URI of the request. Required.
	 * 
	 * @param client The value of the client parameter. Not required.
	 * 
	 * @param deviceId An unique identifier for each device. Not required.
	 * 
	 * @param parameters A map of parameter keys to all of their values. Not
	 * 					 required.
	 * 
	 * @param extras A map of keys from the HTTP request header to their 
	 * 				 values.
	 * 
	 * @param response A string that should have the format of a JSONObject
	 * 				   indicating whether or not the request succeed or failed.
	 * 				   If the request succeed, that is all that needs to be
	 * 				   passed; passing the data that was returned to the 
	 * 				   requesting user would create too much duplicate data and
	 * 				   may leak private information. If the request failed, the
	 * 				   error code and error text should be included in this 
	 * 				   JSONObject string.
	 * 
	 * @param receivedMillis A millisecond-level epoch-based time at which the
	 * 						 request was received. This should be obtained by
	 * 						 the same mechanism as 'respondMillis'. Required.
	 *  
	 * @param respondMillis A millisecond-level epoch-based time at which the
	 * 						request was received. This should be obtained by
	 * 						the same mechanism as 'receivedMillis'. Required.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are null.
	 */
	public static void createAudit(
			final RequestServlet.RequestType requestType, 
			final String uri, 
			final String client, 
			final String deviceId,
			final Map<String, String[]> parameters, 
			final Map<String, String[]> extras, 
			final String response, 
			final long receivedMillis, 
			final long respondMillis) throws DataAccessException {
		
		if(requestType == null) {
			throw new IllegalArgumentException("The request type is required and cannot be null.");
		}
		else if(uri == null) {
			throw new IllegalArgumentException("The request URI is required and cannot be null.");
		}
		else if(response == null) {
			throw new IllegalArgumentException("The response is required and cannot be null.");
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a request audit.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Create a key holder that will be responsible for referencing 
			// which row was just inserted.
			KeyHolder keyHolder = new GeneratedKeyHolder();
			
			// Insert the audit entry.
			try {
				instance.getJdbcTemplate().update(
						new PreparedStatementCreator() {
							@Override
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps = connection.prepareStatement(
									SQL_INSERT_AUDIT, 
									new String[] {"id"}
								);
								
								ps.setString(1, requestType.name().toLowerCase());
								ps.setString(2, uri);
								ps.setString(3, client);
								ps.setString(4, deviceId);
								ps.setString(5, response);
								ps.setLong(6, receivedMillis);
								ps.setLong(7, respondMillis);
								
								return ps;
							}
						}, 
						keyHolder);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + SQL_INSERT_AUDIT + "' with parameters: " +
							requestType.name().toLowerCase() + ", " + 
							uri + ", " + 
							client + ", " + 
							deviceId + ", " + 
							response + ", " + 
							receivedMillis + ", " + 
							respondMillis, 
						e);
			}
			
			// Add all of the parameters.
			if(parameters != null) {
				for(String key : parameters.keySet()) {
					for(String value : parameters.get(key)) {
						try {
							instance.getJdbcTemplate().update(
									SQL_INSERT_PARAMETER, 
									keyHolder.getKey().longValue(), 
									key, 
									value);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException(
									"Error while executing SQL '" + SQL_INSERT_PARAMETER + "' with parameters: " +
										keyHolder.getKey().longValue() + ", " + 
										key + ", " + 
										value, 
									e);
						}
					}
				}
			}
			
			// Add all of the extras.
			if(extras != null) {
				for(String key : extras.keySet()) {
					for(String value : extras.get(key)) {
						try {
							instance.getJdbcTemplate().update(
									SQL_INSERT_EXTRA, 
									keyHolder.getKey().longValue(), 
									key, 
									value);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException(
									"Error while executing SQL '" + SQL_INSERT_EXTRA + "' with parameters: " +
										keyHolder.getKey().longValue() + ", " + 
										key + ", " + 
										value, 
									e);
						}
					}
				}
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Retrieves the unique ID for all audits.
	 * 
	 * @return A list of audit IDs.
	 */
	public static List<Long> getAllAudits() throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_TYPE + "'", e);
		}
	}
	
	/**
	 * Retrieves the unique ID for all audits with a specific HTTP request 
	 * type.
	 * 
	 * @param requestType The HTTP request type. One of
	 * 					  {@link org.ohmage.jee.servlet.RequestServlet.RequestType}.
	 * 
	 * @return A list of audit IDs.
	 */
	public static List<Long> getAllAuditsWithRequestType(RequestServlet.RequestType requestType) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_TYPE, new Object[] { requestType.name().toLowerCase() }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_TYPE + "' with parameter: " + requestType.name().toLowerCase(), e);
		}
	}
	
	/**
	 * Retrieves the unique ID for all audits with a specific URI.
	 * 
	 * @param uri The URI.
	 * 
	 * @return A list of audit IDs.
	 */
	public static List<Long> getAllAuditsWithUri(String uri) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_URI, new Object[] { uri }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_URI + "' with parameter: " + uri, e);
		}
	}
	
	/**
	 * Retrieves the unique ID for all audits with a specific client.
	 * 
	 * @param client The client.
	 * 
	 * @return A list of audit IDs.
	 */
	public static List<Long> getAllAuditsWithClient(String client) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_CLIENT, new Object[] { client }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_CLIENT + "' with parameter: " + client, e);
		}
	}
	
	/**
	 * Retrieves the unique ID for all audits with a specific device ID.
	 * 
	 * @param deviceId The device's ID.
	 * 
	 * @return A list of audit IDs.
	 */
	public static List<Long> getAllAuditsWithDeviceId(String deviceId) throws DataAccessException{
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_DEVICE_ID, new Object[] { deviceId }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_DEVICE_ID + "' with parameter: " + deviceId, e);
		}
	}
	
	/**
	 * Retrieves the unique ID for all audits whose response was one of 
	 * {@link org.ohmage.validator.AuditValidators.ResponseType}. If the 
	 * response type is 
	 * {@link org.ohmage.validator.AuditValidators.ResponseType#FAILURE}, then
	 * it can be further limited by only those audits with a specific error
	 * code. If 'errorCode' is null all audits of failed requests will be 
	 * returned.
	 * 
	 * @param responseTypes The resulting response returned to the user, one of
	 * 						{@link org.ohmage.validator.AuditValidators.ResponseType}.
	 * 
	 * @param errorCode The error code to further limit the results if the 
	 * 					'responseTypes' is
	 * 					{@link org.ohmage.validator.AuditValidators.ResponseType#FAILURE}.
	 * 					If this is null, all failed request audits will be
	 * 					returned.
	 * 
	 * @return A list of audit IDs.
	 */
	public static List<Long> getAllAuditsWithResponse(ResponseType responseType, final String errorCode) throws DataAccessException {
		if(ResponseType.SUCCESS.equals(responseType)) {
			try {
				return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_SUCCESS_RESPONSE, new SingleColumnRowMapper<Long>());
			}
			catch(org.springframework.dao.DataAccessException e) {
				throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_DEVICE_ID + "'.", e);
			}
		}
		else if(ResponseType.FAILURE.equals(responseType)) {
			if(errorCode == null) {
				try {
					return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_FAILURE_RESPONSE, new SingleColumnRowMapper<Long>());
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_DEVICE_ID + "'.", e);
				}
			}
			else {
				try {
					return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_WITH_FAILURE_RESPONSE_WITH_CODE, new Object[] { errorCode }, new SingleColumnRowMapper<Long>());
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_WITH_FAILURE_RESPONSE_WITH_CODE + "'.", e);
				}
			}
		}
		else {
			throw new DataAccessException("Unknown response type: " + responseType.toString());
		}
	}
	
	/**
	 * Retrieves the unique IDs for all audits that were recorded on or after
	 * some date.
	 * 
	 * @param date The date.
	 * 
	 * @return A list of unique audit IDs.
	 */
	public static List<Long> getAllAuditsOnOrAfterDate(Date date) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_ON_OR_AFTER_DATE, new Object[] { date }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_ON_OR_AFTER_DATE + "' with parameter: " + date, e);
		}
	}
	
	/**
	 * Retrieves the unique IDs for all audits that were recorded on or before
	 * some date.
	 * 
	 * @param date The date.
	 * 
	 * @return A list of unique audit IDs.
	 */
	public static List<Long> getAllAuditsOnOrBeforeDate(Date date) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_ON_OR_BEFORE_DATE, new Object[] { date }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_ON_OR_BEFORE_DATE + "' with parameter: " + date, e);
		}
	}
	
	/**
	 * Retrieves the unique IDs for all audits that were recorded on or between
	 * two dates.
	 * 
	 * @param startDate The earlier of the two date to which the audits will be
	 * 					searched. 
	 * 
	 * @param endDate The latter of the two dates to which the audits will be
	 * 				  searched.
	 * 
	 * @return A list of unique audit IDs.
	 */
	public static List<Long> getAllAuditsOnOrBetweenDates(Date startDate, Date endDate) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_AUDIT_IDS_ON_OR_BETWEEN_DATES, new Object[] { startDate, endDate }, new SingleColumnRowMapper<Long>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_IDS_ON_OR_BETWEEN_DATES + "' with parameters: " + 
					startDate + ", " + endDate, e);
		}
	}
	
	/**
	 * Retrieves all the information about a list of audit IDs.
	 * 
	 * @param auditIds The unique identifiers for some audits.
	 * 
	 * @return Returns a list of AuditInformation objects, one for each audit
	 * 		   ID.
	 */
	public static List<AuditInformation> readAuditInformation(final List<Long> auditIds) throws DataAccessException {
		if(auditIds == null) {
			return new LinkedList<AuditInformation>();
		}
		
		final List<AuditInformation> result = new ArrayList<AuditInformation>(auditIds.size());
		
		for(Long auditId : auditIds) {
			try {
				final AuditInformation auditInformation = instance.getJdbcTemplate().queryForObject(
						SQL_GET_AUDIT_INFORMATION_FROM_ID, 
						new Object[] { auditId },
						new RowMapper<AuditInformation>() {
							@Override
							public AuditInformation mapRow(ResultSet rs, int rowNum) throws SQLException {
								RequestType requestType;
								try {
									requestType = RequestType.valueOf(rs.getString("request_type").toUpperCase());
								}
								catch(IllegalArgumentException e) {
									requestType = RequestType.UNKNOWN;
								}
								
								JSONObject response;
								try {
									response = new JSONObject(rs.getString("response"));
								}
								catch(JSONException e) {
									response = new JSONObject();
								}

								return new AuditInformation(
										requestType,
										rs.getString("uri"),
										rs.getString("client"),
										rs.getString("device_id"),
										response,
										rs.getLong("received_millis"),
										rs.getLong("respond_millis"),
										rs.getTimestamp("db_timestamp"));
							}
						}
				);
				
				// Micro inner class to deal with getting key-value pairs from
				// the database.
				class KeyValuePair {
					private String key;
					private String value;
					
					public KeyValuePair(String key, String value) {
						this.key = key;
						this.value = value;
					}
				}
				
				// Add all of the parameters.
				try {
					final List<KeyValuePair> parameters = instance.getJdbcTemplate().query(
							SQL_GET_AUDIT_PARAMETERS, 
							new Object[] { auditId }, 
							new RowMapper<KeyValuePair>() {
								@Override
								public KeyValuePair mapRow(ResultSet rs, int rowNum) throws SQLException {
									return new KeyValuePair(rs.getString("param_key"), rs.getString("param_value"));
								}
							}
					);
					for(KeyValuePair parameter : parameters) {
						auditInformation.addParameter(parameter.key, parameter.value);
					}
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_PARAMETERS + "' with parameter: " + auditId, e);
				}
				
				// Add all of the extras.
				try {
					final List<KeyValuePair> extras = instance.getJdbcTemplate().query(
							SQL_GET_AUDIT_EXTRAS, 
							new Object[] { auditId }, 
							new RowMapper<KeyValuePair>() {
								@Override
								public KeyValuePair mapRow(ResultSet rs, int rowNum) throws SQLException {
									return new KeyValuePair(rs.getString("extra_key"), rs.getString("extra_value"));
								}
							}
					);
					for(KeyValuePair extra : extras) {
						auditInformation.addExtra(extra.key, extra.value);
					}
				}
				catch(org.springframework.dao.DataAccessException e) {
					throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_EXTRAS + "' with parameter: " + auditId, e);
				}
				
				// Add the audit information to the result.
				result.add(auditInformation);
			}
			catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
				throw new DataAccessException("The audit ID does not exist: " + auditId, e);
			}
			catch(org.springframework.dao.DataAccessException e) {
				throw new DataAccessException("Error executing SQL '" + SQL_GET_AUDIT_INFORMATION_FROM_ID + "' with parameter: " + 
						auditId, e);
			}
		}
		
		return result;
	}
}