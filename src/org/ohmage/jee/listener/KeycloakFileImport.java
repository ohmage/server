/*******************************************************************************
 * Copyright 2016 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * <p>
 * Attempts to read the keycloak.json file, if it exists, and stores it.
 * </p>
 * 
 * <p>
 * This must be called after the {@link ConfigurationFileImport} listener to
 * ensure that the keycloak.json location is available.
 * </p>
 *
 * @author Steve Nolen
 */
public class KeycloakFileImport
	implements ServletContextListener {
	
	/**
	 * The configuration property used to lookup the keycloak.json
	 * location.
	 */
	private static final String KEYCLOAK_CONFIG_PROPERTY_NAME = "keycloak.config";
	
	/**
	 * The default file location if one isn't found in the config.
	 * We don't really expect to find something here, but it is important to have a
	 * default to ensure we avoid a NPE when attempting to load it later.
	 */
	private static final String KEYCLOAK_FILE_DEFAULT = 
			"WEB-INF/config/keycloak.json";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(KeycloakFileImport.class.getName());
	
	/**
	 * Default constructor.
	 */
	public KeycloakFileImport() {
		// Do nothing.
	}

	/**
	 * Find the keycloak.json file and attempt set up the cache which holds keycloak properties.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		Properties properties = ConfigurationFileImport.getCustomProperties();
		String keycloakFileLocation = (String) properties.get(KEYCLOAK_CONFIG_PROPERTY_NAME);
		File keycloakFile;

		if (keycloakFileLocation != null) {
			keycloakFile = new File(keycloakFileLocation);
		}
		else {
			keycloakFile = new File(
					event.getServletContext().getRealPath("/") + 
					KEYCLOAK_FILE_DEFAULT);
		}

		ObjectMapper mapper = new ObjectMapper();

		try {
			TypeReference<Map<String, String>> ref = new TypeReference<Map<String, String>>() { };
			Map<String,String> map = mapper.readValue(keycloakFile, ref);
			// SN: commented as KeycloakCache has not been added.
			//KeycloakCache.setCache(map);
			LOGGER
			.log(
					Level.INFO, 
					"The keycloak.json file was loaded from: " +
							keycloakFile.getAbsolutePath());
		}
		// The keycloak.json file was not found.
		catch(FileNotFoundException e) {
			LOGGER
			.log(
					Level.WARNING, 
					"The keycloak.json file was not found at location: " +
							keycloakFile.getAbsolutePath(),
							e);
		}
		// There was an error reading the keycloak.json file.
		catch(IOException e) {
			LOGGER
			.log(
					Level.WARNING, 
					"There was an error reading the keycloak.json file: " +
							keycloakFile.getAbsolutePath(),
							e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Do nothing.
	}
}