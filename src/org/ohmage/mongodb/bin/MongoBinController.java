package org.ohmage.mongodb.bin;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.mongojack.internal.MongoJackModule;
import org.ohmage.bin.BinController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * <p>
 * The data access class for all MongoDB data access objects.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoBinController extends BinController {
	/**
	 * The default server address.
	 */
	public static final String DEFAULT_SERVER_ADDRESS = "localhost";
	
	/**
	 * The default server port.
	 */
	public static final int DEFAULT_SERVER_PORT = 27017;
	
	/**
	 * The default name for the database.
	 */
	public static final String DEFAULT_DATABASE_NAME = "ohmage";
	
	/**
	 * The object mapper that should be used to parse any ohmage domain object.
	 */
	private static final ObjectMapper JSON_MAPPER;
	static {
		// Create the object mapper.
		ObjectMapper mapper = new ObjectMapper();
		
		// Create the FilterProvider.
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		filterProvider.setFailOnUnknownId(false);
		mapper.setFilters(filterProvider);
		
		// Ensure that it stores and reads enums using their ordinal value.
		mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
		
		// Finally, we must configure the mapper to work with the MongoJack
		// configuration.
		JSON_MAPPER = MongoJackModule.configure(mapper);
	}	
	
	/**
	 * The connection to the database.
	 */
	private final MongoClient mongo;

	/**
	 * Default constructor, which will create the connection to the MongoDB.
	 * 
	 * @param properties
	 *        The user-defined properties to use to setup the connection.
	 * 
	 * @throws IllegalStateException
	 *         There was a problem setting up the connection to the database.
	 */
	public MongoBinController(
		final Properties properties)
		throws IllegalStateException {
		
		super(properties);
		
		// Create the singular Mongo instance.
		try {
			// Build the server address.
			ServerAddress serverAddress =
				new ServerAddress(getDatabaseAddress(), getDatabasePort());
			
			// If a username and password were given, use them.
			String username = getDatabaseUsername();
			String password = getDatabasePassword();
			if((username != null) && (password != null)) {
				// Create the list of credentials.
				List<MongoCredential> credentials =
					new LinkedList<MongoCredential>();
				
				// Add the given credentials.
				credentials
					.add(
						MongoCredential
							.createMongoCRCredential(
								username,
								getDatabaseName(),
								password.toCharArray()));
				
				// Create the MongoDB client with the credentials.
				mongo = new MongoClient(serverAddress, credentials);
			}
			// Otherwise, build the client without credentials.
			else {
				mongo = new MongoClient(serverAddress);
			}
		}
		catch(UnknownHostException e) {
			throw
				new IllegalStateException(
					"The database could not be setup.",
					e);
		}
		
		// Ensure that all writes use the safest write concern.
		mongo.setWriteConcern(WriteConcern.REPLICA_ACKNOWLEDGED);
		
		// Instantiate the specific components.
		new MongoUserBin();
		new MongoAuthenticationTokenBin();
		new MongoStreamBin();
		new MongoStreamDataBin();
		new MongoOhmletBin();
	}
	
	/**
	 * Returns the database connection to MongoDB.
	 * 
	 * @return The database to MongoDB.
	 */
	public DB getDb() {
		// Get the connection to the database.
		return mongo.getDB(getDatabaseName());
	}
	
	/**
	 * Shuts the bin controller down.
	 */
	@Override
	public void shutdown() {
		mongo.close();
	}
	
	/**
	 * Returns the instance of this bin controller as a MongoBinController.
	 * 
	 * @return The instance of this bin controller as a MongoBinController.
	 * 
	 * @throws IllegalStateException
	 *         The bin controller was not built with a MongoBinController.
	 */
	public static MongoBinController getInstance() {
		try {
			return (MongoBinController) BinController.getInstance();
		}
		catch(ClassCastException e) {
			throw
				new IllegalStateException(
					"The bin controller is not a MongoDB bin controller.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.BinController#getDefaultServerAddress()
	 */
	@Override
	protected String getDefaultServerAddress() {
		return DEFAULT_SERVER_ADDRESS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.BinController#getDefaultServerPort()
	 */
	@Override
	protected int getDefaultServerPort() {
		return DEFAULT_SERVER_PORT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.BinController#getDefaultDatabaseName()
	 */
	@Override
	protected String getDefaultDatabaseName() {
		return DEFAULT_DATABASE_NAME;
	}
	
	/**
	 * Returns an ObjectMapper that has been pre-configured to work with domain
	 * objects.
	 * 
	 * @return An ObjectMapper that has been pre-configured to work with domain
	 *         objects.
	 */
	protected static ObjectMapper getObjectMapper() {
		return JSON_MAPPER;
	}
}