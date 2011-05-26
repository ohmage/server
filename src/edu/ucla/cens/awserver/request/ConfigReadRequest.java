package edu.ucla.cens.awserver.request;

/**
 * Request for configuration reads. This request has nothing and does nothing
 * except contains a constant that references the result object in the request.
 * 
 * @author John Jenkins
 */
public class ConfigReadRequest extends ResultListAwRequest {
	public static final String RESULT = "config_read_request_config_information";
}
