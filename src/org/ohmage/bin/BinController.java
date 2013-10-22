package org.ohmage.bin;

import java.util.Properties;

/**
 * <p>
 * The driver class for all of the bins.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class BinController {
	/**
	 * The property key for the server address.
	 */
	public static final String PROPERTY_KEY_SERVER_ADDRESS =
		"db.server.address";
	
	/**
	 * The property key for the server port.
	 */
	public static final String PROPERTY_KEY_SERVER_PORT = "db.server.port";
	
	/**
	 * The property key for the name of the database.
	 */
	public static final String PROPERTY_KEY_DATABASE_NAME = "db.name";

	/**
	 * The property key for the database username.
	 */
	public static final String PROPERTY_KEY_DATABASE_USERNAME = "db.username";
	
	/**
	 * The property key for the database password.
	 */
	public static final String PROPERTY_KEY_DATABASE_PASSWORD = "db.password";
	
	/**
	 * The address of the database server.
	 */
	private final String address;
	
	/**
	 * The port for the database server.
	 */
	private final int port;
	
	/**
	 * The name of the database to use.
	 */
	private final String name;
	
	/**
	 * The username of the user to use to connect to the database. This may be
	 * null if none was supplied.
	 */
	private final String username;
	
	/**
	 * The password of the user to use to connect to the database. This may be
	 * null if none was supplied.
	 */
	private final String password;
	
	/**
	 * The singular instance of this bin controller.
	 */
	private static BinController instance;

	/**
	 * <p>
	 * Reads the configuration properties and extracts the necessary
	 * information.
	 * </p>
	 * 
	 * <p>
	 * Database-specific implementations should sub-subclass this class to
	 * setup their connections to the database and then instantiate their
	 * respective bin objects.
	 * </p>
	 * 
	 * @param properties
	 *        The {@link Properties} object that may or may not contain the
	 *        information about how to setup the connection to the database. If
	 *        it isn't included, the implementation-specific details will be
	 *        used.
	 *        
	 * @see #getDefaultServerAddress()
	 * @see #getDefaultServerPort()
	 * @see #getDefaultDatabaseName()
	 */
	protected BinController(final Properties properties) {
		// Sanitize the properties parameter.
		Properties tProperties = properties;
		if(tProperties == null) {
			tProperties = new Properties();
		}
		
		// Get the server address.
		String address =
			tProperties.getProperty(PROPERTY_KEY_SERVER_ADDRESS);
		if(address == null) {
			address = getDefaultServerAddress();
		}
		this.address = address;
		
		// Get the server port.
		int port;
		String serverPortString = 
			tProperties.getProperty(PROPERTY_KEY_SERVER_PORT);
		if(serverPortString == null) {
			port = getDefaultServerPort();
		}
		else {
			try {
				port = Integer.decode(serverPortString);
			}
			catch(NumberFormatException e) {
				throw
					new IllegalArgumentException(
						"The server port is not a number.",
						e);
			}
		}
		this.port = port;
		
		// Get the database name.
		String name = tProperties.getProperty(PROPERTY_KEY_DATABASE_NAME);
		if(name == null) {
			name = getDefaultDatabaseName();
		}
		this.name = name;
		
		// Get the username.
		username = tProperties.getProperty(PROPERTY_KEY_DATABASE_USERNAME);
		
		// Get the password.
		password = tProperties.getProperty(PROPERTY_KEY_DATABASE_PASSWORD);
		
		// Set the instance of the bin controller.
		instance = this;
	}
	
	/**
	 * Returns the database address.
	 * 
	 * @return The database address.
	 */
	public String getDatabaseAddress() {
		return address;
	}
	
	/**
	 * Returns the database port.
	 * 
	 * @return The database port.
	 */
	public int getDatabasePort() {
		return port;
	}
	
	/**
	 * Returns the database name.
	 * 
	 * @return The database name.
	 */
	public String getDatabaseName() {
		return name;
	}
	
	/**
	 * Returns the username to use to connect to the database.
	 * 
	 * @return The username to use to connect to the database. This may be
	 *         null.
	 */
	public String getDatabaseUsername() {
		return username;
	}
	
	/**
	 * Returns the password to use to connect to the database.
	 * 
	 * @return The password to use to connect to the database. This may be
	 *         null.
	 */
	public String getDatabasePassword() {
		return password;
	}
	
	/**
	 * Returns the instance of this bin controller.
	 * 
	 * @return The instance of this bin controller.
	 */
	public static BinController getInstance() {
		return instance;
	}

	/**
	 * Shut down the current instance, which should include closing database
	 * connections among other things.
	 */
	public abstract void shutdown();
	
	/**
	 * The default address to use for the database.
	 * 
	 * @return The default database server address.
	 */
	protected abstract String getDefaultServerAddress();
	
	/**
	 * The default port to use for the database.
	 *  
	 * @return The default database server port.
	 */
	protected abstract int getDefaultServerPort();
	
	/**
	 * The default name to use for the database when connecting to it.
	 * 
	 * @return The default database name.
	 */
	protected abstract String getDefaultDatabaseName();
}