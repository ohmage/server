package org.ohmage.servlet;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.StreamBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * <p>
 * The controller for all requests for streams and their data.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(StreamServlet.ROOT_MAPPING)
@SessionAttributes(
	{
		AuthFilter.ATTRIBUTE_AUTHORIZATION_TOKEN,
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN,
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM
	})
public class StreamServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/streams";
	
	/**
	 * The path and parameter key for stream IDs.
	 */
	public static final String KEY_STREAM_ID = "id";
	/**
	 * The path and parameter key for stream versions.
	 */
	public static final String KEY_STREAM_VERSION = "version";
	/**
	 * The path and parameter key for stream versions.
	 */
	public static final String KEY_STREAM_NAME = "name";
	/**
	 * The path and parameter key for stream versions.
	 */
	public static final String KEY_STREAM_DESCRIPTION = "description";
	/**
	 * The path and parameter key for stream versions.
	 */
	public static final String KEY_STREAM_DEFINITION = "definition";
	
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(StreamServlet.class.getName());
	
	/**
	 * The usage in this class is entirely static, so there is no need to
	 * instantiate it.
	 */
	private StreamServlet() {
		// Do nothing.
	}
	
	/**
	 * Creates a new stream.
	 * 
	 * @param token
	 *        The user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the authentication token was provided as a
	 *        parameter.
	 * 
	 * @param streamBuilder
	 *        A builder to use to create this new stream.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public static @ResponseBody void createStream(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@RequestBody
			final Stream.Builder streamBuilder) {
		
		LOGGER.log(Level.INFO, "Creating a stream creation request.");
		
		LOGGER
			.log(
				Level.FINE,
				"Ensuring that an authentication token is given.");
		if(token == null) {
			throw
				new InvalidArgumentException(
					"The authentication token is missing.");
		}
		LOGGER
			.log(
				Level.FINE,
				"Ensuring that the authentication token is a parameter.");
		if(! tokenIsParam) {
			throw
				new InvalidArgumentException(
					"The authentication token must be a parameter, " +
						"not just a header.");
		}
		
		LOGGER.log(Level.FINE, "Setting the owner of the stream.");
		streamBuilder.setOwner(token.getUsername());
		
		LOGGER
			.log(
				Level.INFO,
				"Creating the stream and adding it to the database.");
		StreamBin.getInstance().addStream(streamBuilder.build());
	}
	
	/**
	 * Returns a list of visible stream IDs.
	 * 
	 * @return A list of visible stream IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getStreamIds() {
		LOGGER.log(Level.INFO, "Creating a stream ID read request.");

		return StreamBin.getInstance().getStreamIds();
	}
	
	/**
	 * Returns a list of versions for the given stream.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody List<Long> getStreamVersions(
		@PathVariable(KEY_STREAM_ID) final String streamId) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read the versions of a stream: " +
					streamId);
		
		return StreamBin.getInstance().getStreamVersions(streamId);	
	}
	
	/**
	 * Returns the definition for a given stream.
	 * 
	 * @param streamId The stream's unique identifier.
	 * 
	 * @param streamVersion The version of the stream.
	 * 
	 * @return The stream definition.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}/{" + KEY_STREAM_VERSION + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Stream getStreamDefinition(
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a stream definition: " +
					streamId + ", " +
					streamVersion);
		
		LOGGER.log(Level.INFO, "Retrieving the stream.");
		Stream result =
			StreamBin
				.getInstance()
				.getStream(streamId, streamVersion);

		LOGGER.log(Level.FINE, "Ensuring that a stream was found.");
		if(result == null) {
			throw
				new UnknownEntityException(
					"The stream ID-verion pair is unknown.");
		}

		LOGGER.log(Level.INFO, "Returning the stream.");
		return result;
	}
	
	/**
	 * Updates an existing stream with a new version.
	 * 
	 * @param token
	 *        The token of the user that is attempting to update the stream.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was a parameter to the request.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @param streamBuilder
	 *        A builder to use to create this new stream, which should be based
	 *        on the stream that is being updated.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}",
		method = RequestMethod.POST)
	public static @ResponseBody void updateStream(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@RequestBody
			final Stream.Builder streamBuilder) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to update a stream with a new version: " +
					streamId);
		
		LOGGER.log(Level.FINE, "Validating the parameters.");
		LOGGER
			.log(
				Level.FINER,
				"Ensuring that an authentication token is given.");
		if(token == null) {
			throw
				new InvalidArgumentException(
					"The authentication token is missing.");
		}
		LOGGER
			.log(
				Level.FINER,
				"Ensuring that the authentication token is a parameter.");
		if(! tokenIsParam) {
			throw
				new InvalidArgumentException(
					"The authentication token must be a parameter, " +
						"not just a header.");
		}
		
		LOGGER.log(Level.INFO, "Retrieving the latest version of the stream.");
		Stream latestSchema =
			StreamBin.getInstance().getLatestStream(streamId);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the new version of the stream is greater " +
					"than all existing ones.");
		long latestVersion = latestSchema.getVersion();
		if(latestVersion >= streamBuilder.getVersion()) {
			throw
				new InvalidArgumentException(
					"The new version of this schema must be greater than " +
						"the existing latest version of " +
						latestVersion +
						".");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user updating the stream is the owner " +
					"of the original stream.");
		if(! latestSchema.getOwner().equals(token.getUsername())) {
			throw
				new InsufficientPermissionsException(
					"Only the owner of this schema may update it.");
		}
		
		LOGGER
			.log(
				Level.FINE,
				"Setting the request user as the owner of this new stream.");
		streamBuilder.setOwner(token.getUsername());
		
		LOGGER.log(Level.INFO, "Saving the updated stream.");
		StreamBin.getInstance().addStream(streamBuilder.build());
	}
}