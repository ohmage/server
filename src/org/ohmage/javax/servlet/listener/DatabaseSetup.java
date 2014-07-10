/*******************************************************************************
 * Copyright 2013 Open mHealth
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
package org.ohmage.javax.servlet.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.ohmage.bin.BinController;

/**
 * <p>
 * Sets up the database.
 * </p>
 *
 * <p>
 * This must be called after the {@link ConfigurationFileImport} listener to
 * ensure that specialized configuration options have been accounted for.
 * </p>
 *
 * @author John Jenkins
 */
public class DatabaseSetup implements ServletContextListener {
	/**
	 * A {@link Logger} for this class.
	 */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(DatabaseSetup.class.getName());

	/**
	 * The key that denotes which BinController class to use.
	 */
	public static final String PROPERTY_KEY_DATABASE_CLASS = "db.class";

	/**
	 * The BinController object to use to control the connection to the
	 * database.
	 */
	private BinController binController = null;

	/**
	 * Default constructor.
	 */
	public DatabaseSetup() {
		// Do nothing.
	}

	/**
	 * Setup the connection to the database. See the configuration file to
	 * change which database is setup.
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		LOGGER.info("Setting up the bin controller.");

		// Get the properties.
		LOGGER.debug("Retreiving the ohmage properties.");
		Properties properties = ConfigurationFileImport.getCustomProperties();

		// If the database class property is missing, this is a critical error.
		LOGGER.trace("Verifying that the database class is present.");
		if(! properties.containsKey(PROPERTY_KEY_DATABASE_CLASS)) {
			LOGGER.error("The database class is missing from the properties: " +
						PROPERTY_KEY_DATABASE_CLASS);
			throw
				new IllegalStateException(
					"The database class is missing from the properties: " +
						PROPERTY_KEY_DATABASE_CLASS);
		}

		// Get the class string.
		String binControllerClassString =
			properties.getProperty(PROPERTY_KEY_DATABASE_CLASS);
		LOGGER.trace("Retrieved the database class: " + binControllerClassString);

		// Create and store the bin controller.
		try {
			LOGGER.debug("Initializing the bin controller.");
			binController =
				(BinController) Class
					.forName(binControllerClassString)
					.getConstructor(Properties.class)
					.newInstance(properties);
			LOGGER.info("Successfully initialized the bin controller: " +
					binControllerClassString);
		}
		catch(
			ClassNotFoundException |
			SecurityException |
			NoSuchMethodException |
			IllegalArgumentException |
			InstantiationException |
			IllegalAccessException |
			InvocationTargetException
			e) {

			LOGGER.error("The bin controller could not be created: " +
						binControllerClassString,
					e);
			throw
				new IllegalStateException(
					"There was a problem creating the bin controller.",
					e);
		}
	}

	/**
	 * Closes the connection to the database.
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		if(binController != null) {
			LOGGER.info("Shutting down the bin controller.");
			binController.shutdown();
		}
	}
}
