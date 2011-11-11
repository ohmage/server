package org.ohmage.test.config;

import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;

/**
 * This class is responsible for testing the configuration API.
 * 
 * @author John Jenkins
 */
public class ConfigApiTest {
	private final OhmageApi api;
	
	/**
	 * Creates a tester for the configuration API.
	 * 
	 * @param api The ohmage API object that should already be setup with a
	 * 			  connection to a server.
	 */
	public ConfigApiTest(final OhmageApi api) {
		this.api = api;
	}
	
	/**
	 * Test the server configuration call.
	 * 
	 * @throws ApiException Thrown if there is a bug in the library.
	 * 
	 * @throws IllegalStateException Thrown if the test fails.
	 */
	public void test() throws ApiException {
		// If a call can be made and an 
		try {
			api.getServerConfiguration();
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Valid request.", e);
		}
	}
}