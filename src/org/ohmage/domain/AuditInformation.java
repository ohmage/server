package org.ohmage.domain;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.jee.servlet.RequestServlet;

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
	
	public void addExtra(String key, String value) {
		if(key == null) {
			throw new IllegalArgumentException("The key cannot be null.");
		}
		
		List<String> values = extras.get(key);
		if(values == null) {
			values = new LinkedList<String>();
			parameters.put(key, values);
		}
		values.add(value);
	}
	
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