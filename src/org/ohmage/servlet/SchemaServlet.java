package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.user.User;
import org.ohmage.servlet.filter.AuthFilter;
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

        LOGGER.log(Level.INFO, "Retrieving the survey IDs.");
        result.addAll(SurveyBin.getInstance().getSurveyIds(query));

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

		LOGGER.log(Level.INFO, "Retrieving the versions.");
        if(StreamBin.getInstance().exists(schemaId, null)) {
            LOGGER.log(Level.INFO, "The schema is a stream.");
            result
                .addAll(
                    StreamBin
                        .getInstance()
                        .getStreamVersions(schemaId, query));
        }
        else if(SurveyBin.getInstance().exists(schemaId, null)) {
            LOGGER.log(Level.INFO, "The schema is a survey.");
            result
                .addAll(
                    StreamBin
                        .getInstance()
                        .getStreamVersions(schemaId, query));
        }
        else {
            throw new UnknownEntityException("The schema is unknown.");
        }

		LOGGER.log(Level.INFO, "Returning the schema versions.");
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
     * @return The data corresponding to the schema ID and version.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}/data",
        method = RequestMethod.GET)
    public static @ResponseBody MultiValueResult<?> getData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion) {

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

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        MultiValueResult<?> result;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a stream.");
            result =
                StreamDataBin
                    .getInstance()
                    .getStreamData(
                        user.getUsername(),
                        schemaId,
                        schemaVersion);
        }
        else if(SurveyBin.getInstance().exists(schemaId, schemaVersion)) {
            LOGGER.log(Level.INFO, "The schema is a survey.");
            result =
                SurveyResponseBin
                    .getInstance()
                    .getSurveyResponses(
                        user.getUsername(),
                        schemaId,
                        schemaVersion,
                        null);
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