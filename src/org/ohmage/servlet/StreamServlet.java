package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all requests for streams and their data.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(StreamServlet.ROOT_MAPPING)
public class StreamServlet extends OhmageServlet {
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
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @param streamBuilder
	 *        A builder to use to create this new stream.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public static @ResponseBody Stream createStream(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@RequestBody
			final Stream.Builder streamBuilder) {

		LOGGER.log(Level.INFO, "Creating a stream creation request.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

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
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
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
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@RequestBody
			final Stream.Builder streamBuilder) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to update a stream with a new version: " +
					streamId);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

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
		        "Setting the ID of the new stream to the ID of the old " +
		            "stream.");
		streamBuilder.setSchemaId(streamId);

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
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
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
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion,
		@RequestBody final List<StreamData.Builder> dataBuilders) {

		LOGGER.log(Level.INFO, "Storing some new stream data.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

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
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
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
	public static ResponseEntity<MultiValueResult<? extends StreamData>> getData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion) {

		LOGGER.log(Level.INFO, "Retrieving some stream data.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		LOGGER.log(Level.INFO, "Finding the requested data.");
		MultiValueResult<? extends StreamData> result =
    		StreamDataBin
                .getInstance()
                .getStreamData(user.getUsername(), streamId, streamVersion);

		LOGGER.log(Level.INFO, "Setting the response headers.");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Count", Integer.toString(result.count()));

		LOGGER.log(Level.INFO, "Returning the response object.");
		return
		    new ResponseEntity<MultiValueResult<? extends StreamData>>(
		        result,
		        headers,
		        HttpStatus.OK);
	}

    /**
     * Retrieves a point.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
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
        method = RequestMethod.GET)
    public static @ResponseBody StreamData getPoint(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_STREAM_ID) final String streamId,
        @PathVariable(KEY_STREAM_VERSION) final Long streamVersion,
        @PathVariable(KEY_STREAM_POINT_ID) final String pointId) {

        LOGGER.log(Level.INFO, "Retrieving a specific stream data point.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Returning the stream data.");
        return
            StreamDataBin
                .getInstance()
                .getStreamData(
                    user.getUsername(),
                    streamId,
                    streamVersion,
                    pointId);
    }

    /**
     * Deletes a point.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
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
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_STREAM_ID) final String streamId,
        @PathVariable(KEY_STREAM_VERSION) final Long streamVersion,
        @PathVariable(KEY_STREAM_POINT_ID) final String pointId) {

        LOGGER.log(Level.INFO, "Deleting a specific stream data point.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

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