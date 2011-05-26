package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ConfigReadRequest;

/**
 * Creates a request that represents a config read request.
 * 
 * @author John Jenkins
 */
public class ConfigReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(ConfigReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ConfigReadAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Since there are no parameters, there just return a basic object.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating system information query request.");
		
		return new ConfigReadRequest();
	}

}
