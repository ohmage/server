package org.ohmage.domain;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.jee.servlet.RequestServlet;

/**
 * This class represents all of the information recorded by a request.
 * 
 * @author John Jenkins
 */
public class AuditInformation {
	private static final Logger LOGGER = Logger.getLogger(AuditInformation.class);
	
	private static final String JSON_KEY_REQUEST_TYPE = "request_type";
	private static final String JSON_KEY_URI = "uri";
	private static final String JSON_KEY_CLIENT = "client";
	private static final String JSON_KEY_DEVICE_ID = "device_id";
	private static final String JSON_KEY_RESPONSE = "response";
	private static final String JSON_KEY_RECEIVED_MILLIS = "received_millis";
	private static final String JSON_KEY_RESPONDED_MILLIS = "responded_millis";
	private static final String JSON_KEY_DB_TIMESTAMP = "timestamp";
	private static final String JSON_KEY_PARAMETERS = "request_parameters";
	private static final String JSON_KEY_EXTRAS = "extra_data";
	
	private final RequestServlet.RequestType requestType;
	private final String uri;
	private final String client;
	private final String deviceId;
	private final JSONObject response;
	
	private final long receivedMillis;
	private final long respondedMillis;
	private final Timestamp dbTimestamp;
	
	private final Map<String, List<String>> parameters;
	private final Map<String, List<String>> extras;
	
	/**
	 * Creates a new AuditInformation object that contains the final values of
	 * much of the information for an audit.
	 * 
	 * @param requestType The 
	 * 					  {@link org.ohmage.jee.servlet.RequestServlet.RequestType}
	 * 					  from this request.
	 * 
	 * @param uri The URI that supplied for this request.
	 * 
	 * @param client The client parameter that was given in this request.
	 * 
	 * @param deviceId The device ID parameter that was given in this request.
	 * 
	 * @param response The JSONObject response to the requester.
	 * 
	 * @param receivedMillis The milliseconds since epoch at which this request
	 * 						 was made.
	 * 
	 * @param respondedMillis The milliseconds since epoch at which time this
	 * 						  request had been completely responded to.
	 * 
	 * @param dbTimestamp A Timestamp representing the date and time at which
	 * 					  the database had recorded this audit.
	 */
	public AuditInformation(
			RequestServlet.RequestType requestType, 
			String uri, 
			String client, 
			String deviceId, 
			JSONObject response, 
			long receivedMillis, 
			long respondedMillis, 
			Timestamp dbTimestamp) {
		
		this.requestType = requestType;
		this.uri = uri;
		this.client = client;
		this.deviceId = deviceId;
		this.response = response;
		
		this.receivedMillis = receivedMillis;
		this.respondedMillis = respondedMillis;
		this.dbTimestamp = dbTimestamp;
		
		this.parameters = new HashMap<String, List<String>>();
		this.extras = new HashMap<String, List<String>>();
	}
	
	/**
	 * Adds a "parameter" item to the list of parameters in this request.
	 * 
	 * @param key The key for this parameter. Multiple parameters with the same
	 * 			  key are allowed.
	 * 
	 * @param value A new or another value for this key.
	 */
	public void addParameter(String key, String value) {
		if(key == null) {
			throw new IllegalArgumentException("The key cannot be null.");
		}
		
		List<String> values = parameters.get(key);
		if(values == null) {
			values = new LinkedList<String>();
			parameters.put(key, values);
		}
		values.add(value);
	}
	
	/**
	 * Returns an unmodifiable collection of the values associated with some 
	 * key.
	 * 
	 * @param key The key whose values are to be returned.
	 * 
	 * @return A unmodifiable Collection of the value associated with the key.
	 */
	public Collection<String> getParameters(String key) {
		List<String> values = parameters.get(key);
		
		if(values != null) {
			return Collections.unmodifiableCollection(values);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Adds an "extra" item to the list of extras in this request.
	 * 
	 * @param key The key for this extra. Multiple extras with the same key are
	 * 			  allowed.
	 * 
	 * @param value A new or another value for this key.
	 */
	public void addExtra(String key, String value) {
		if(key == null) {
			throw new IllegalArgumentException("The key cannot be null.");
		}
		
		List<String> values = extras.get(key);
		if(values == null) {
			values = new LinkedList<String>();
			extras.put(key, values);
		}
		values.add(value);
	}
	
	/**
	 * Returns an unmodifiable collection of the values associated with some 
	 * key.
	 * 
	 * @param key The key whose values are to be returned.
	 * 
	 * @return A unmodifiable Collection of the value associated with the key.
	 */
	public Collection<String> getExtras(String key) {
		List<String> values = extras.get(key);
		
		if(values != null) {
			return Collections.unmodifiableCollection(values);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns the request type for this HTTP request.
	 * 
	 * @return The request type for this HTTP request.
	 * 
	 * @see org.ohmage.jee.servlet.RequestServlet.RequestType
	 */
	public final RequestServlet.RequestType getRequestType() {
		return requestType;
	}

	/**
	 * Returns the URI that was used for this HTTP request.
	 * 
	 * @return The URI that was used for this HTTP request.
	 */
	public final String getUri() {
		return uri;
	}

	/**
	 * Returns the client value that was supplied with this request. This may
	 * be null.
	 * 
	 * @return The client value that was supplied with this request. This may
	 * 		   be null.
	 */
	public final String getClient() {
		return client;
	}

	/**
	 * Returns the device ID that was supplied with this request. This may be
	 * null.
	 * 
	 * @return The device ID that was supplied with this request. This may be
	 * 		   null.
	 */
	public final String getDeviceId() {
		return deviceId;
	}

	/**
	 * Returns the JSONObject that was returned to the requester. If the 
	 * request failed, it should also include an error code and error text. If
	 * the request simply contained an unknown URI or invalid response type,
	 * the error code and error text may be meaningless. If the request was
	 * successful, this will simply be JSON indicating that. It may be that 
	 * this JSON was not actually returned to the user and some attachment was;
	 * in this case, you will still see the JSON indicating success instead of
	 * what was actually returned to the user.
	 * 
	 * @return A JSONObject representing the response.
	 */
	public final JSONObject getResponse() {
		return response;
	}

	/**
	 * Returns the milliseconds since epoch representing the time at which this
	 * request was received.
	 * 
	 * @return The milliseconds since epoch representing the time at which this
	 * 		   response was received.
	 */
	public final long getReceivedMillis() {
		return receivedMillis;
	}

	/**
	 * Returns the milliseconds since epoch representing the time at which we
	 * responded to this request.
	 * 
	 * @return The milliseconds since epoch representing the time at which we
	 * 		   responded to this request.
	 */
	public final long getRespondedMillis() {
		return respondedMillis;
	}

	/**
	 * Returns the date and time at which the database inserted this record.
	 * 
	 * @return The date and time at which the database inserted this record.
	 */
	public final Timestamp getDbTimestamp() {
		return dbTimestamp;
	}

	/**
	 * A JSONObject representing the available information from this audit.
	 * 
	 * @return A JSONObject representing this object. If there is an error 
	 * 		   building this object, null is returned.
	 */
	public JSONObject toJson() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_REQUEST_TYPE, requestType.toString());
			result.put(JSON_KEY_URI, uri);
			result.put(JSON_KEY_CLIENT, client);
			result.put(JSON_KEY_DEVICE_ID, deviceId);
			result.put(JSON_KEY_RESPONSE, response);
			
			result.put(JSON_KEY_RECEIVED_MILLIS, receivedMillis);
			result.put(JSON_KEY_RESPONDED_MILLIS, respondedMillis);
			result.put(JSON_KEY_DB_TIMESTAMP, dbTimestamp.toString());
			
			result.put(JSON_KEY_PARAMETERS, parameters);
			result.put(JSON_KEY_EXTRAS, extras);
			
			return result;
		}
		catch(JSONException e) {
			LOGGER.error("Error building the JSONObject.", e);
			return null;
		}
	}
}