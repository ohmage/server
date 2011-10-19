package org.ohmage.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.domain.ServerConfig;
import org.ohmage.exception.ServiceException;
import org.ohmage.service.ConfigServices;

/**
 * <p>This class is responsible for updating a class.</p>
 * <p>There are no required parameters for this call.</p>
 * 
 * @author John Jenkins
 */
public class ConfigReadRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(ConfigReadRequest.class);
	
	private ServerConfig result;
	
	/**
	 * Default constructor.
	 */
	public ConfigReadRequest() {
		super(null);
		
		result = null;
	}
	
	/**
	 * Gathers the appropriate information and stores the result in the result
	 * object.
	 */
	@Override
	public void service() {
		LOGGER.info("Gathering information about the system.");
		
		try {
			result = ConfigServices.readServerConfiguration(this);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
	
	/**
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		return new HashMap<String, String[]>();
	}
	
	/**
	 * Writes the response to the client.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing configuration read response.");
		
		respond(httpRequest, httpResponse, (result == null) ? null : result.toJson());
	}
}