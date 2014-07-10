package org.ohmage.controller;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.joda.time.DateTime;
import org.ohmage.bin.MediaBin;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.OhmletBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.user.User;
import org.ohmage.javax.servlet.filter.AuthFilter;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * The controller for all requests for streams and their data.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(StreamController.ROOT_MAPPING)
public class StreamController extends OhmageController {
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
     * The parameter key for a stream definition.
     */
    public static final String KEY_STREAM_DEFINITION = "definition";
    /**
     * The parameter for the icon.
     */
    public static final String KEY_ICON = "icon";
    /**
     * The name of the parameter for querying for specific values.
     */
    public static final String KEY_QUERY = "query";
    /**
     * The path and parameter key for stream point IDs.
     */
    public static final String KEY_STREAM_POINT_ID = "point_id";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(StreamController.class.getName());

	/**
	 * The usage in this class is entirely static, so there is no need to
	 * instantiate it.
	 */
	private StreamController() {
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
	@RequestMapping(
	    value = { "", "/" },
	    method = RequestMethod.POST,
	    consumes = "application/json")
	public static @ResponseBody Stream createStream(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@RequestBody
			final Stream.Builder streamBuilder) {

	    return createStream(authToken, streamBuilder, null);
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
     *
     * @param iconFile
     *        The file that represents the icon, which must be present if an
     *        icon is defined in the 'stream builder'.
     */
    @RequestMapping(
        value = { "", "/" },
        method = RequestMethod.POST,
        consumes = "multipart/*")
    public static @ResponseBody Stream createStream(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestPart(value = KEY_STREAM_DEFINITION, required = true)
            final Stream.Builder streamBuilder,
        @RequestPart(value = KEY_ICON, required = false)
            final MultipartFile iconFile) {

        LOGGER.info("Creating a stream creation request.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.debug("Setting the owner of the stream.");
        streamBuilder.setOwner(user.getId());

        LOGGER.debug("Checking if an icon was given.");
        Media icon = null;
        // If given, verify that it was attached as well.
        if(streamBuilder.getIconId() != null) {
            if(iconFile
                .getOriginalFilename()
                .equals(streamBuilder.getIconId())) {

                String newIconId = Media.generateUuid();
                streamBuilder.setIconId(newIconId);
                icon = new Media(newIconId, iconFile);
            }
            else {
                throw
                    new InvalidArgumentException(
                        "An icon file was referenced but not uploaded.");
            }
        }

        LOGGER.debug("Building the updated stream.");
        Stream result = streamBuilder.build();

        if(icon != null) {
            LOGGER.info("Storing the icon.");
            MediaBin.getInstance().addMedia(icon);
        }

        LOGGER.info("Saving the new stream.");
        StreamBin.getInstance().addStream(result);

        LOGGER.info("Returning the updated stream.");
        return result;
    }

    /**
     * Returns a list of visible streams.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @param numToSkip
     *        The number of streams to skip.
     *
     * @param numToReturn
     *        The number of streams to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     *
     * @return A list of visible streams.
     */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<Stream>> getStreams(
		@RequestParam(value = KEY_QUERY, required = false) final String query,
        @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP) final long numToSkip,
        @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN) final long numToReturn,
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
		    final String rootUrl) {

		LOGGER.info("Creating a stream ID read request.");

		LOGGER.info("Retrieving the stream IDs");
		MultiValueResult<Stream> ids =
		    StreamBin
		        .getInstance()
		        .getStreams(query, false, numToSkip, numToReturn);

		LOGGER.info("Building the paging headers.");
		HttpHeaders headers =
		    OhmageController
		        .buildPagingHeaders(
                        numToSkip,
                        numToReturn,
                        Collections.<String, String>emptyMap(),
                        ids,
                        rootUrl + ROOT_MAPPING);

		LOGGER.info("Creating the response object.");
		ResponseEntity<MultiValueResult<Stream>> result =
		    new ResponseEntity<MultiValueResult<Stream>>(
		        ids,
		        headers,
		        HttpStatus.OK);

        LOGGER.info("Returning the stream IDs.");
		return result;
	}

	/**
	 * Returns a list of versions for the given stream.
	 *
	 * @param streamId
	 *        The stream's unique identifier.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
	 *
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<Long>> getStreamVersions(
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query,
        @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP) final long numToSkip,
        @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN) final long numToReturn,
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER.info("Creating a request to read the versions of a stream: " +
					streamId);

		LOGGER.info("Retreiving the stream versions.");
		MultiValueResult<Long> versions =
		    StreamBin
                .getInstance()
                .getStreamVersions(
                    streamId,
                    query,
                    false,
                    numToSkip,
                    numToReturn);

        LOGGER.info("Building the paging headers.");
        HttpHeaders headers =
            OhmageController
                .buildPagingHeaders(
                        numToSkip,
                        numToReturn,
                        Collections.<String, String>emptyMap(),
                        versions,
                        rootUrl + ROOT_MAPPING);

        LOGGER.info("Creating the response object.");
        ResponseEntity<MultiValueResult<Long>> result =
            new ResponseEntity<MultiValueResult<Long>>(
                versions,
                headers,
                HttpStatus.OK);

        LOGGER.info("Returning the stream versions.");
		return result;
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

		LOGGER.info("Creating a request for a stream definition: " +
					streamId + ", " +
					streamVersion);

		LOGGER.info("Retrieving the stream.");
		Stream result =
			StreamBin
				.getInstance()
				.getStream(streamId, streamVersion, false);

		LOGGER.debug("Ensuring that a stream was found.");
		if(result == null) {
			throw
				new UnknownEntityException(
					"The stream ID-verion pair is unknown.");
		}

		LOGGER.info("Returning the stream.");
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
		@RequestBody final Stream.Builder streamBuilder) {

		LOGGER.info("Creating a request to update a stream with a new version: " +
					streamId);

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

		LOGGER.info("Retrieving the latest version of the stream.");
		Stream latestSchema =
			StreamBin.getInstance().getLatestStream(streamId, false);

    // Increase survey version by 1
    streamBuilder.setVersion(latestSchema.getVersion()+1);

		LOGGER.info("Verifying that the user updating the stream is the owner " +
					"of the original stream.");
		if(! latestSchema.getOwner().equals(user.getId())) {
			throw
				new InsufficientPermissionsException(
					"Only the owner of this schema may update it.");
		}

		LOGGER.debug("Setting the ID of the new stream to the ID of the old " +
		            "stream.");
		streamBuilder.setSchemaId(streamId);

		LOGGER.debug("Setting the request user as the owner of this new stream.");
		streamBuilder.setOwner(user.getId());

		LOGGER.debug("Building the updated stream.");
		Stream result = streamBuilder.build();

		LOGGER.info("Saving the updated stream.");
		StreamBin.getInstance().addStream(result);

		LOGGER.info("Returning the updated stream.");
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
	 * @param dataBuilders
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

		LOGGER.info("Storing some new stream data.");

        LOGGER.info("Validating the user from the token");
        User user =
            OhmageController
                .validateAuthorization(
                        authToken,
                        new Scope(
                                Scope.Type.STREAM,
                                streamId,
                                streamVersion,
                                Scope.Permission.WRITE));

		LOGGER.info("Retrieving the stream.");
		Stream stream =
			StreamBin
				.getInstance()
				.getStream(streamId, streamVersion, false);

		LOGGER.debug("Ensuring that a stream was found.");
		if(stream == null) {
			throw
				new UnknownEntityException(
					"The stream ID-version pair is unknown.");
		}

		LOGGER.info("Validating the data.");
		List<StreamData> data = new ArrayList<StreamData>(dataBuilders.size());
        Set<String> streamDataPointIds = new HashSet<String>();

		for(StreamData.Builder dataBuilder : dataBuilders) {
		    dataBuilder.setOwner(user.getId());
			data.add(dataBuilder.build(stream));
            streamDataPointIds.add(dataBuilder.getMetaData().getId());
		}

        LOGGER.info("Retrieving the duplicate IDs.");
        List<String> duplicateStreamDataPointIds =
            StreamDataBin
                .getInstance()
                .getDuplicateIds(
                        user.getId(),
                        streamId,
                        streamVersion,
                        streamDataPointIds);

        LOGGER.info("There are " + duplicateStreamDataPointIds.size() + " duplicates.");

        // Prune out the duplicates

        List<StreamData> nonDuplicatePoints = null;

        if(duplicateStreamDataPointIds.size() == data.size()) { // every point is a duplicate

            nonDuplicatePoints = Collections.<StreamData>emptyList();
            LOGGER.info("Every uploaded point was a duplicate.");

        } else if(duplicateStreamDataPointIds.size() > 0) { // there are some duplicates

            for(StreamData dataPoint : data) {
                if(! duplicateStreamDataPointIds.contains(dataPoint.getId())) {

                    if(nonDuplicatePoints == null) {
                        nonDuplicatePoints = new LinkedList<StreamData>();
                    }
                    nonDuplicatePoints.add(dataPoint);
                }
            }

        } else { // there are no duplicates

            nonDuplicatePoints = data;
        }

        if(nonDuplicatePoints.size() > 0) {
            LOGGER.info("Storing the validated data.");
            StreamDataBin.getInstance().addStreamData(nonDuplicatePoints);
        }
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
     * @param startDate
     *        The earliest date for a given point.
     *
     * @param endDate
     *        The latest date for a given point.
     *
     * @param chronological
     *        Whether or not the data should be sorted in chronological order
     *        (as opposed to reverse-chronological order).
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
	 *
	 * @return The data that conforms to the request parameters.
	 */
	@RequestMapping(
		value = "{" + KEY_STREAM_ID + "}/{" + KEY_STREAM_VERSION + "}/data",
		method = RequestMethod.GET)
	public static ResponseEntity<? extends MultiValueResult<? extends StreamData>> getData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_STREAM_ID) final String streamId,
		@PathVariable(KEY_STREAM_VERSION) final Long streamVersion,
        @RequestParam(value = PARAM_START_DATE, required = false)
            final String startDate,
        @RequestParam(value = PARAM_END_DATE, required = false)
            final String endDate,
        @RequestParam(
            value = PARAM_CHRONOLOGICAL,
            required = false,
            defaultValue = PARAM_DEFAULT_CHRONOLOGICAL)
            final boolean chronological,
        @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP) final long numToSkip,
        @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN) final long numToReturn,
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER.info("Retrieving some stream data.");

		LOGGER.info("Validating the user from the token");
		User user =
		    OhmageController
		        .validateAuthorization(
                        authToken,
                        new Scope(
                                Scope.Type.STREAM,
                                streamId,
                                streamVersion,
                                Scope.Permission.READ));

        LOGGER.debug("Parsing the start and end dates, if given.");
        DateTime startDateObject, endDateObject;
        try {
            startDateObject =
                (startDate == null) ?
                    null :
                    OHMAGE_DATE_TIME_FORMATTER.parseDateTime(startDate);
        }
        catch(IllegalArgumentException e) {
            throw new InvalidArgumentException("The start date is invalid.");
        }
        try {
            endDateObject =
                (endDate == null) ?
                    null :
                    OHMAGE_DATE_TIME_FORMATTER.parseDateTime(endDate);
        }
        catch(IllegalArgumentException e) {
            throw new InvalidArgumentException("The end date is invalid.");
        }

        LOGGER.info("Retrieving the latest version of the stream.");
        Stream latestStream =
            StreamBin.getInstance().getLatestStream(streamId, false);
        if(latestStream == null) {
            throw new UnknownEntityException("The stream is unknown.");
        }

        LOGGER.debug("Determining if the user is asking about the latest version " +
                    "of the stream.");
        boolean allowNull = latestStream.getVersion() == streamVersion;

        LOGGER.info("Gathering the applicable ohmlets.");
        Set<String> ohmletIds =
            OhmletBin
                .getInstance()
                .getOhmletIdsWhereUserCanReadStreamData(
                    user.getId(),
                    streamId,
                    streamVersion,
                    allowNull);

        LOGGER.info("Determining which users are visible to the requesting user.");
        Set<String> userIds;
        if(authToken.getAuthorizationCode() == null) {
            LOGGER.info("The auth token was granted directly to the requesting " +
                        "user; retrieving the list of user IDs that are " +
                        "visible to the requesting user.");
            userIds = OhmletBin.getInstance().getMemberIds(ohmletIds);
        }
        else {
            LOGGER.info("The auth token was granted via OAuth, so only the user " +
                        "reference by the token may be searched.");
            userIds = new HashSet<String>();
        }

        LOGGER.info("Adding the user's own user ID.");
        userIds.add(user.getId());

		LOGGER.info("Finding the requested data.");
		MultiValueResult<? extends StreamData> data =
    		StreamDataBin
                .getInstance()
                .getStreamData(
                    streamId,
                    streamVersion,
                    userIds,
                    startDateObject,
                    endDateObject,
                    null,
                    chronological,
                    numToSkip,
                    numToReturn);

        LOGGER.info("Building the paging headers.");
        HttpHeaders headers =
            OhmageController
                .buildPagingHeaders(
                        numToSkip,
                        numToReturn,
                        Collections.<String, String>emptyMap(),
                        data,
                        rootUrl + ROOT_MAPPING);

        LOGGER.info("Creating the response object.");
        ResponseEntity<MultiValueResult<? extends StreamData>> result =
            new ResponseEntity<MultiValueResult<? extends StreamData>>(
                data,
                headers,
                HttpStatus.OK);

        LOGGER.info("Returning the stream data.");
        return result;
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

        LOGGER.info("Retrieving a specific stream data point.");

        LOGGER.info("Validating the user from the token");
        User user =
            OhmageController
                .validateAuthorization(
                        authToken,
                        new Scope(
                                Scope.Type.STREAM,
                                streamId,
                                streamVersion,
                                Scope.Permission.READ));

        LOGGER.info("Returning the stream data.");
        return
            StreamDataBin
                .getInstance()
                .getStreamData(
                    user.getId(),
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

        LOGGER.info("Deleting a specific stream data point.");

        LOGGER.info("Validating the user from the token");
        User user =
            OhmageController
                .validateAuthorization(
                        authToken,
                        new Scope(
                                Scope.Type.STREAM,
                                streamId,
                                streamVersion,
                                Scope.Permission.DELETE));

        LOGGER.info("Deleting the stream data.");
        StreamDataBin
            .getInstance()
            .deleteStreamData(
                user.getId(),
                streamId,
                streamVersion,
                pointId);
    }
}
