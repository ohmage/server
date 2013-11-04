package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	 * The path and parameter key for stream point IDs.
	 */
	public static final String KEY_STREAM_POINT_ID = "point_id";
	/**
	 * The name of the parameter for querying for specific values.
	 */
	public static final String KEY_QUERY = "query";
	
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
	public static @ResponseBody Stream createStream(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@RequestBody
			final Stream.Builder streamBuilder) {
		
		LOGGER.log(Level.INFO, "Creating a stream creation request.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER.log(Level.FINE, "Setting the owner of the stream.");
		streamBuilder.setOwner(user.getUsername());
		
		LOGGER.log(Level.FINE, "Building the updated stream.");
		Stream result = streamBuilder.build();
		
		LOGGER.log(Level.INFO, "Saving the new stream.");
		StreamBin.getInstance().addStream(result);
		
		LOGGER.log(Level.INFO, "Returning the updated stream.");
		return result;
	}
	
	/**
	 * Returns a list of visible stream IDs.
	 * 
	 * @param search
	 *        A value that should appear in either the name or description.
	 * 
	 * @return A list of visible stream IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getStreamIds(
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		
		LOGGER.log(Level.INFO, "Creating a stream ID read request.");

		return StreamBin.getInstance().getStreamIds(query);
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
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read the versions of a stream: " +
					streamId);
		
		return StreamBin.getInstance().getStreamVersions(streamId, query);	
	}
	
	/**
	 * Returns the definition for a given stream.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @param streamVersion
	 *        The version of the stream.
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
	public static @ResponseBody Stream updateStream(
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
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
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
		if(! latestSchema.getOwner().equals(user.getUsername())) {
			throw
				new InsufficientPermissionsException(
					"Only the owner of this schema may update it.");
		}
		
		LOGGER
			.log(
				Level.FINE,
				"Setting the request user as the owner of this new stream.");
		streamBuilder.setOwner(user.getUsername());
		
		LOGGER.log(Level.FINE, "Building the updated stream.");
		Stream result = streamBuilder.build();
		
		LOGGER.log(Level.INFO, "Saving the updated stream.");
		StreamBin.getInstance().addStream(result);
		
		LOGGER.log(Level.INFO, "Returning the updated stream.");
		return result;
	}
	
	/**
	 * Stores data points.
	 * 
	 * @param streamId
	 *        The stream's unique identifier.
	 * 
	 * @param streamVersion
	 *        The version of the stream.
	 * 
	 * @param data
	 *        The list of data points to save.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}/{" + KEY_STREAM_VERSION + "}/data",
		method = RequestMethod.POST)
	public static @ResponseBody void storeData(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion,
		@RequestBody final List<StreamData.Builder> dataBuilders) {
		
		LOGGER.log(Level.INFO, "Storing some new stream data.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);

		LOGGER.log(Level.INFO, "Retrieving the stream.");
		Stream stream =
			StreamBin
				.getInstance()
				.getStream(streamId, streamVersion);
		
		LOGGER.log(Level.FINE, "Ensuring that a stream was found.");
		if(stream == null) {
			throw
				new UnknownEntityException(
					"The stream ID-verion pair is unknown.");
		}
		
		LOGGER.log(Level.INFO, "Validating the data.");
		List<StreamData> data = new ArrayList<StreamData>(dataBuilders.size());
		for(StreamData.Builder dataBuilder : dataBuilders) {
			data.add(dataBuilder.setOwner(user.getUsername()).build(stream));
		}
		
		LOGGER.log(Level.INFO, "Storing the validated data.");
		StreamDataBin.getInstance().addStreamData(data);
	}
	
	/**
	 * Retrieves the data for the requesting user.
	 * 
	 * @param authenticationToken
	 *        The requesting user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the requesting user's authentication token was a
	 *        parameter.
	 * 
	 * @param authorizationToken
	 *        An authorization token sent by the requesting user.
	 * 
	 * @param streamId
	 *        The unique identifier of the stream whose data is being
	 *        requested.
	 * 
	 * @param streamVersion
	 *        The version of the stream whose data is being requested.
	 * 
	 * @return The data that conforms to the request parameters.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}/{" + KEY_STREAM_VERSION + "}/data",
		method = RequestMethod.GET)
	public static @ResponseBody MultiValueResult<? extends StreamData> getData(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken authenticationToken,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHORIZATION_TOKEN)
			final AuthorizationToken authorizationToken,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion) {
		
		LOGGER.log(Level.INFO, "Retrieving some stream data.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user =
			AuthFilter
				.retrieveUserFromAuth(
					authorizationToken, 
					authenticationToken, 
					tokenIsParam);
		
		LOGGER.log(Level.INFO, "Finding and returning the requested data.");
		return
			StreamDataBin
				.getInstance()
				.getStreamData(user.getUsername(), streamId, streamVersion);
	}
	
	/**
	 * Deletes a point.
	 * 
	 * @param authenticationToken
	 *        The requesting user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the requesting user's authentication token was a
	 *        parameter.
	 * 
	 * @param authorizationToken
	 *        An authorization token sent by the requesting user.
	 * 
	 * @param streamId
	 *        The unique identifier of the stream whose data is being
	 *        requested.
	 * 
	 * @param streamVersion
	 *        The version of the stream whose data is being requested.
	 * 
	 * @param pointId
	 *        The unique identifier for a specific point.
	 * 
	 * @return The data that conforms to the request parameters.
	 */
	@RequestMapping(
		value =
			"{" + KEY_STREAM_ID + "}" +
			"/" +
			"{" + KEY_STREAM_VERSION + "}" +
			"/data" +
			"/" +
			"{" + KEY_STREAM_POINT_ID + "}",
		method = RequestMethod.DELETE)
	public static @ResponseBody void deletePoint(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken authenticationToken,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHORIZATION_TOKEN)
			final AuthorizationToken authorizationToken,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion,
		@PathVariable(KEY_STREAM_POINT_ID) final String pointId) {
		
		LOGGER.log(Level.INFO, "Retrieving a specific stream data point.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user =
			AuthFilter
				.retrieveUserFromAuth(
					authorizationToken, 
					authenticationToken, 
					tokenIsParam);
		
		LOGGER.log(Level.INFO, "Deleting the stream data.");
		StreamDataBin
			.getInstance()
			.deleteStreamData(
				user.getUsername(),
				streamId,
				streamVersion,
				pointId);
	}
}