package org.ohmage.dao;

import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.jee.servlet.RequestServlet;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
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
	// Creates a new audit entry.
	private static final String SQL_INSERT_AUDIT =
		"INSERT INTO audit(uuid, request_type_id, uri, client, device_id, response, received_millis, respond_millis) " +
		"VALUES (?, (" +
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
			"WHERE uuid = ?" +
		"), ?, ?)";
	
	// Adds an extra from the HTTP request's header to an audit.
	private static final String SQL_INSERT_EXTRA =
		"INSERT INTO audit_extra(audit_id, extra_key, extra_value) " +
		"VALUES ((" +
			"SELECT id " +
			"FROM audit " +
			"WHERE uuid = ?" +
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
	public static void createAudit(RequestServlet.RequestType requestType, String uri, String client, String deviceId,
			Map<String, String[]> parameters, Map<String, String[]> extras, String response, long receivedMillis, long respondMillis) {
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
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			String uuid = UUID.randomUUID().toString();
			
			// Insert the audit entry.
			try {
				instance.jdbcTemplate.update(SQL_INSERT_AUDIT, new Object[] { uuid, requestType.name().toLowerCase(), uri, 
						client, deviceId, response, receivedMillis, respondMillis });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_AUDIT + "' with parameters: " +
						uuid + ", " + requestType.name().toLowerCase() + ", " + uri + ", " + client + ", " + deviceId + ", " + 
						response + ", " + receivedMillis + ", " + respondMillis, e);
			}
			
			// Add all of the parameters.
			if(parameters != null) {
				for(String key : parameters.keySet()) {
					for(String value : parameters.get(key)) {
						try {
							instance.jdbcTemplate.update(SQL_INSERT_PARAMETER, uuid, key, value);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_PARAMETER + "' with parameters: " +
									uuid + ", " + key + ", " + value, e);
						}
					}
				}
			}
			
			// Add all of the extras.
			if(extras != null) {
				for(String key : extras.keySet()) {
					for(String value : extras.get(key)) {
						try {
							instance.jdbcTemplate.update(SQL_INSERT_EXTRA, uuid, key, value);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_EXTRA + "' with parameters: " +
									uuid + ", " + key + ", " + value, e);
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
}
