package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.StreamBin;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.UnknownEntityException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all requests for schemas and their data.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(SchemaServlet.ROOT_MAPPING)
public class SchemaServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/schemas";
	
	/**
	 * The path and parameter key for schema IDs.
	 */
	public static final String KEY_SCHEMA_ID = "id";
	/**
	 * The path and parameter key for schema versions.
	 */
	public static final String KEY_SCHEMA_VERSION = "version";
	/**
	 * The name of the parameter for querying for specific values.
	 */
	public static final String KEY_QUERY = "query";
	
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(SchemaServlet.class.getName());
	
	/**
	 * Returns a list of visible schema IDs.
	 * 
	 * @param query
	 *        A value that should appear in either the name or description.
	 * 
	 * @return A list of visible schema IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getSchemaIds(
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		
		LOGGER.log(Level.INFO, "Creating a schema ID read request.");
		 
		LOGGER.log(Level.FINE, "Creating the result object.");
		List<String> result = new ArrayList<String>();
		
		LOGGER.log(Level.INFO, "Retrieving the stream IDs.");
		result.addAll(StreamBin.getInstance().getStreamIds(query));
		
		LOGGER.log(Level.INFO, "Returning the schema IDs.");
		return result;
	}
	
	/**
	 * Returns a list of versions for the given schema.
	 * 
	 * @param schemaId
	 *        The schema's unique identifier.
	 * 
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_SCHEMA_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody List<Long> getSchemaVersions(
		@PathVariable(KEY_SCHEMA_ID) final String schemaId,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read the versions of a schema: " +
					schemaId);
		 
		LOGGER.log(Level.FINE, "Creating the result object.");
		List<Long> result = new ArrayList<Long>();
		
		LOGGER
			.log(Level.FINE, "Determining if this schema ID is a stream ID.");
		if(StreamBin.getInstance().exists(schemaId, null)) {
			LOGGER.log(Level.INFO, "The schema ID is a stream ID.");
			LOGGER.log(Level.INFO, "Retrieving the stream versions.");
			result
				.addAll(
					StreamBin
						.getInstance()
						.getStreamVersions(schemaId, query));
		}

		LOGGER.log(Level.INFO, "Returning the schema versions.");
		return result;
	}
	
	/**
	 * Returns the definition for a given schema.
	 * 
	 * @param schemaId The schema's unique identifier.
	 * 
	 * @param schemaVersion The version of the schema.
	 * 
	 * @return The schema definition.
	 */
	@RequestMapping(
		value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Schema getSchemaDefinition(
		@PathVariable(KEY_SCHEMA_ID) final String schemaId,
		@PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a schema definition: " +
					schemaId + ", " +
					schemaVersion);
		 
		LOGGER.log(Level.FINE, "Creating the result object.");
		Schema result = null;

		LOGGER.log(Level.INFO, "Attempting to read the schema as a stream.");
		result = StreamBin.getInstance().getStream(schemaId, schemaVersion);

		LOGGER.log(Level.FINE, "Ensuring that a schema was found.");
		if(result == null) {
			throw
				new UnknownEntityException(
					"The schema ID-verion pair is unknown.");
		}

		LOGGER.log(Level.INFO, "Returning the schema.");
		return result;
	}
}