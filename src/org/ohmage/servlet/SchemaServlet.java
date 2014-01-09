package org.ohmage.servlet;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.MultiValueResultAggregation;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.user.User;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
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
public class SchemaServlet extends OhmageServlet {
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
     * The path and parameter key for a data point.
     */
    public static final String KEY_POINT_ID = "point_id";
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
     * @return A list of visible schema IDs.
     */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<String>> getSchemaIds(
		@RequestParam(value = KEY_QUERY, required = false) final String query,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn,
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER.log(Level.INFO, "Creating a schema ID read request.");

        LOGGER.log(Level.INFO, "Retrieving the stream IDs.");
        MultiValueResult<String> streamIds =
            StreamBin
                .getInstance()
                .getStreamIds(query, 0, numToSkip + numToReturn);

        LOGGER.log(Level.INFO, "Retrieving the survey IDs.");
        MultiValueResult<String> surveyIds =
            SurveyBin
                .getInstance()
                .getSurveyIds(query, 0, numToSkip + numToReturn);

        LOGGER.log(Level.FINE, "Building the result.");
        MultiValueResultAggregation.Aggregator<String> aggregator =
            new MultiValueResultAggregation.Aggregator<String>(streamIds);
        aggregator.add(surveyIds);
        MultiValueResultAggregation<String> ids =
            aggregator.build(numToSkip, numToReturn);

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    numToSkip,
                    numToReturn,
                    Collections.<String, String>emptyMap(),
                    ids,
                    rootUrl + ROOT_MAPPING);

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<String>> result =
            new ResponseEntity<MultiValueResult<String>>(
                ids,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the schema IDs.");
        return result;
	}

	/**
	 * Returns a list of versions for the given schema.
	 *
	 * @param schemaId
	 *        The schema's unique identifier.
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
		value = "{" + KEY_SCHEMA_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<Long>> getSchemaVersions(
		@PathVariable(KEY_SCHEMA_ID) final String schemaId,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn,
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read the versions of a schema: " +
					schemaId);

		LOGGER.log(Level.INFO, "Retrieving the stream versions.");
		MultiValueResult<Long> streamVersions =
		    StreamBin
                .getInstance()
                .getStreamVersions(
                    schemaId,
                    query,
                    0,
                    numToSkip + numToReturn);

        LOGGER.log(Level.INFO, "Retrieving the survey versions.");
        MultiValueResult<Long> surveyVersions =
            StreamBin
                .getInstance()
                .getStreamVersions(schemaId,
                    query,
                    0,
                    numToSkip + numToReturn);

        LOGGER.log(Level.FINE, "Building the result.");
        MultiValueResultAggregation.Aggregator<Long> aggregator =
            new MultiValueResultAggregation.Aggregator<Long>(streamVersions);
        aggregator.add(surveyVersions);
        MultiValueResultAggregation<Long> versions =
            aggregator.build(numToSkip, numToReturn);

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    numToSkip,
                    numToReturn,
                    Collections.<String, String>emptyMap(),
                    versions,
                    rootUrl + ROOT_MAPPING);

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<Long>> result =
            new ResponseEntity<MultiValueResult<Long>>(
                versions,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the schema IDs.");
        return result;
	}

	/**
     * Returns the definition for a given schema.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
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

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        Schema result;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a stream.");
            result =
                StreamBin.getInstance().getStream(schemaId, schemaVersion);
        }
        else if(SurveyBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a survey.");
            result =
                SurveyBin.getInstance().getSurvey(schemaId, schemaVersion);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID-verion pair is unknown.");
        }

		LOGGER.log(Level.INFO, "Returning the schema.");
		return result;
	}

    /**
     * Returns the data corresponding to the schema ID and version.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @param startDate
     *        The earliest date for a given point.
     *
     * @param endDate
     *        The latest date for a given point.
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
     * @return The data corresponding to the schema ID and version.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}/data",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<?>> getData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion,
        @RequestParam(value = PARAM_START_DATE, required = false)
            final String startDate,
        @RequestParam(value = PARAM_END_DATE, required = false)
            final String endDate,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn,
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request for schema data: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.FINE, "Parsing the start and end dates, if given.");
        DateTime startDateObject =
            (startDate == null) ?
                null :
                OHMAGE_DATE_TIME_FORMATTER.parseDateTime(startDate);
        DateTime endDateObject =
            (endDate == null) ?
                null :
                OHMAGE_DATE_TIME_FORMATTER.parseDateTime(endDate);

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        MultiValueResult<?> data;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a stream.");
            data =
                StreamDataBin
                    .getInstance()
                    .getStreamData(
                        user.getUsername(),
                        schemaId,
                        schemaVersion,
                        startDateObject,
                        endDateObject,
                        null,
                        numToSkip,
                        numToReturn);
        }
        else if(SurveyBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a survey.");
            data =
                SurveyResponseBin
                    .getInstance()
                    .getSurveyResponses(
                        user.getUsername(),
                        schemaId,
                        schemaVersion,
                        null,
                        startDateObject,
                        endDateObject,
                        null,
                        numToSkip,
                        numToReturn);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID-verion pair is unknown.");
        }

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    numToSkip,
                    numToReturn,
                    Collections.<String, String>emptyMap(),
                    data,
                    rootUrl + ROOT_MAPPING);

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<?>> result =
            new ResponseEntity<MultiValueResult<?>>(
                data,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the data.");
        return result;
    }

    /**
     * Returns a specific data point corresponding to the schema ID and
     * version.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @param pointId
     *        The unique identifier for a specific point.
     *
     * @return The data corresponding to the schema ID and version and point
     *         ID.
     */
    @RequestMapping(
        value =
            "{" + KEY_SCHEMA_ID + "}" +
            "/" +
            "{" + KEY_SCHEMA_VERSION + "}" +
            "/" +
            "data" +
            "/" +
            "{" + KEY_POINT_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody OhmageDomainObject getPoint(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion,
        @PathVariable(KEY_POINT_ID) final String pointId) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request for a specific schema data point: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

        LOGGER.log(Level.INFO, "Retrieving the data.");
        OhmageDomainObject result;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a stream.");
            result =
                StreamDataBin
                    .getInstance()
                    .getStreamData(
                        user.getUsername(),
                        schemaId,
                        schemaVersion,
                        pointId);
        }
        else if(SurveyBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a survey.");
            result =
                SurveyResponseBin
                    .getInstance()
                    .getSurveyResponse(
                        user.getUsername(),
                        schemaId,
                        schemaVersion,
                        pointId);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID-verion pair is unknown.");
        }

        LOGGER.log(Level.INFO, "Returning the data point.");
        return result;
    }
}