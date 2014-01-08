package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import name.jenkins.paul.john.concordia.Concordia;

import org.joda.time.DateTime;
import org.ohmage.bin.AuthenticationTokenBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.ColumnList;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.domain.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all Open mHealth requests.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(OmhServlet.ROOT_MAPPING)
public class OmhServlet extends OhmageServlet {
    /**
     * <p>
     * The Open mHealth representation of schema IDs.
     * <p>
     *
     * @author John Jenkins
     */
    private static class OmhSchemaId {
        /**
         * <p>
         * The different types of Open mHealth schema IDs based on ohmage's
         * different data types.
         * </p>
         *
         * @author John Jenkins
         */
        public static enum Type {
            /**
             * An Open mHealth schema ID based off of an ohmage stream.
             */
            STREAM,
            /**
             * An Open mHealth schema ID based off of an ohmage survey.
             */
            SURVEY;

            /**
             * Prints the type as a lower-case, user-friendly string.
             */
            @Override
            public String toString() {
                return name().toLowerCase();
            }

            /**
             * Parses some string value into a Type.
             *
             * @param value
             *        The string value to parse.
             *
             * @return The Type that is represented by the given string.
             *
             * @throws IllegalArgumentException
             *         The given string does not represent any known Type.
             */
            public static Type getType(final String value)
                throws IllegalArgumentException {

                for(Type type : values()) {
                    if(type.name().toLowerCase().equals(value)) {
                        return type;
                    }
                }

                throw
                    new IllegalArgumentException(
                        "The type is unknown: " + value);
            }
        }

        /**
         * The ohmage type that is associated with this schema ID.
         */
        public final Type type;
        /**
         * The schema ID usable within ohmage.
         */
        public final String ohmageSchemaId;

        /**
         * Builds a new OmhSchemaId based on an ohmage schema ID.
         *
         * @param type
         *        The schema ID's type.
         *
         * @param ohmageSchemaId
         *        The ohmage schema ID.
         *
         * @throws IllegalArgumentException
         *         The type or ID are null.
         */
        public OmhSchemaId(final Type type, final String ohmageSchemaId)
            throws IllegalArgumentException {

            if(type == null) {
                throw new IllegalArgumentException("The type is null.");
            }
            if(ohmageSchemaId == null) {
                throw
                    new IllegalArgumentException(
                        "The ohmage schema ID is null.");
            }

            this.type = type;
            this.ohmageSchemaId = ohmageSchemaId;
        }

        /**
         * Builds a new OmhSchemaId based on a schema ID in the Open mHealth
         * format.
         *
         * @param omhSchemaId
         *        A schema ID in the Open mHealth format.
         */
        public OmhSchemaId(final String omhSchemaId) {
            // If the parameter is null, error out.
            if(omhSchemaId == null) {
                throw new InvalidArgumentException("The schema ID is null.");
            }

            // Split the schema ID based on the colons.
            String[] schemaIdParts = omhSchemaId.split(":");

            // Validate the schema ID parts.
            if(schemaIdParts.length != 4) {
                throw
                    new InvalidArgumentException(
                        "The schema ID must be four parts each separated by " +
                            "a colon: " +
                                "omh:" +
                                "ohmage:" +
                                "{\"stream\" | \"survey\"}:" +
                                "{schema_id}");
            }
            else if(! "omh".equals(schemaIdParts[0])) {
                throw
                    new InvalidArgumentException(
                        "The schema ID must begin with \"omh:\": " +
                            omhSchemaId);
            }
            else if(! "ohmage".equals(schemaIdParts[1])) {
                throw
                    new InvalidArgumentException(
                        "The second part of the schema ID must be \"ohmage\": " +
                            omhSchemaId);
            }

            // Validate the type.
            try {
                type = Type.getType(schemaIdParts[2]);
            }
            catch(IllegalArgumentException e) {
                throw
                    new InvalidArgumentException(
                        "The third part of the schema ID must be either " +
                            "\"stream\" or \"survey\": " +
                            omhSchemaId);

            }

            // Store the ohmage schema ID.
            ohmageSchemaId = schemaIdParts[3];
        }

        /**
         * Returns this schema ID in the Open mHealth format.
         */
        @Override
        public String toString() {
            return
                "omh" + ":" +
                "ohmage" + ":" +
                type.toString() + ":" +
                ohmageSchemaId;
        }
    }

    /**
     * The root mapping for all Open mHealth APIs.
     */
    public static final String ROOT_MAPPING = "/omh/v1";

    /**
     * The path and parameter key for schema IDs.
     */
    public static final String KEY_SCHEMA_ID = "id";
    /**
     * The path and parameter key for schema versions.
     */
    public static final String KEY_SCHEMA_VERSION = "version";
    /**
     * The name of the parameter for the users auth token.
     */
    public static final String KEY_AUTH_TOKEN = "auth_token";
    /**
     * The name of the parameter for querying for specific values.
     */
    public static final String KEY_QUERY = "query";
    /**
     * The name of the parameter for querying data on or after a given time.
     */
    public static final String KEY_START_DATE = "t_start";
    /**
     * The name of the parameter for querying data on or before a given time.
     */
    public static final String KEY_END_DATE = "t_end";
    /**
     * The name of the parameter for limiting which fields are returned in the
     * data.
     */
    public static final String KEY_COLUMN_LIST = "column_list";

    /**
     * The list of allowed root values for the "column list" parameter.
     */
    public static final Set<String> ALLOWED_COLUMN_LIST_ROOTS =
        new HashSet<String>(
            Arrays
                .asList(
                    StreamData.JSON_KEY_META_DATA,
                    StreamData.JSON_KEY_DATA,
                    SurveyResponse.JSON_KEY_META_DATA,
                    SurveyResponse.JSON_KEY_RESPONSES));

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(OmhServlet.class.getName());

    /**
     * Returns a list of visible stream IDs.
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

        LOGGER.log(Level.INFO, "Creating an OmH registry read request.");

        LOGGER.log(Level.FINE, "Creating the result object.");
        List<String> result = new ArrayList<String>();

        LOGGER.log(Level.INFO, "Retrieving the stream IDs.");
        for(String streamId : StreamBin.getInstance().getStreamIds(query)) {
            result
                .add(
                    (new OmhSchemaId(OmhSchemaId.Type.STREAM, streamId))
                        .toString());
        }

        LOGGER.log(Level.INFO, "Retrieving the survey IDs.");
        for(String surveyId : SurveyBin.getInstance().getSurveyIds(query)) {
            result
                .add(
                    (new OmhSchemaId(OmhSchemaId.Type.SURVEY, surveyId))
                        .toString());
        }

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
                "Creating an OmH request to read the versions of a schema: " +
                    schemaId);

        LOGGER.log(Level.INFO, "Parsing the schema ID.");
        OmhSchemaId omhSchemaId = new OmhSchemaId(schemaId);

        LOGGER.log(Level.FINE, "Creating the result object.");
        List<Long> result = new ArrayList<Long>();

        LOGGER.log(Level.INFO, "Retrieving the versions.");
        switch(omhSchemaId.type) {
            case STREAM:
                LOGGER.log(Level.INFO, "The schema is a stream.");
                result
                    .addAll(
                        StreamBin
                            .getInstance()
                            .getStreamVersions(
                                omhSchemaId.ohmageSchemaId,
                                query));
                break;

            case SURVEY:
                LOGGER.log(Level.INFO, "The schema is a survey.");
                result
                    .addAll(
                        StreamBin
                            .getInstance()
                            .getStreamVersions(
                                omhSchemaId.ohmageSchemaId,
                                query));
                break;

            default:
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
    public static @ResponseBody Concordia getSchemaDefinition(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion) {

        LOGGER
            .log(
                Level.INFO,
                "Creating an OmH request for a schema definition: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.log(Level.INFO, "Parsing the schema ID.");
        OmhSchemaId omhSchemaId = new OmhSchemaId(schemaId);

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        Schema result;
        switch(omhSchemaId.type) {
            case STREAM:
                LOGGER.log(Level.INFO, "The schema is a stream.");
                result =
                    StreamBin
                        .getInstance()
                        .getStream(omhSchemaId.ohmageSchemaId, schemaVersion);
                break;

            case SURVEY:
                LOGGER.log(Level.INFO, "The schema is a survey.");
                result =
                    SurveyBin
                        .getInstance()
                        .getSurvey(omhSchemaId.ohmageSchemaId, schemaVersion);

            default:
                throw
                    new UnknownEntityException(
                        "The schema ID-verion pair is unknown.");

        }

        LOGGER.log(Level.INFO, "Returning the schema.");
        return result.getDefinition();
    }

    /**
     * Returns the data corresponding to the schema ID and version.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param startDate
     *        The earliest date for a given point.
     *
     * @param endDate
     *        The latest date for a given point.
     *
     * @param columnList
     *        A column-separated list of the fields that should be returned
     *        from the resulting data.
     *
     * @return The data corresponding to the schema ID and version.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}/data",
        method = RequestMethod.GET)
    public static @ResponseBody MultiValueResult<?> getData(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion,
        @RequestParam(KEY_AUTH_TOKEN) final String authToken,
        @RequestParam(value = KEY_START_DATE, required = false)
            final String startDate,
        @RequestParam(value = KEY_END_DATE, required = false)
            final String endDate,
        @RequestParam(value = KEY_COLUMN_LIST, required = false)
            final List<String> columnList) {

        LOGGER
            .log(
                Level.INFO,
                "Creating an OmH request for schema data: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER.log(Level.INFO, "Retrieving the auth information.");
        AuthorizationToken authTokenObject =
            AuthenticationTokenBin
                .getInstance()
                .getTokenFromAccessToken(authToken);

        LOGGER.log(Level.INFO, "Verifying the auth token is known.");
        if(authTokenObject == null) {
            throw new AuthenticationException("No auth token is unknown.");
        }

        LOGGER.log(Level.INFO, "Verifying the auth token is valid.");
        if(! authTokenObject.isValid()) {
            throw
                new AuthenticationException(
                    "No auth token is no longer valid.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authTokenObject.getUser();

        LOGGER.log(Level.INFO, "Parsing the schema ID.");
        OmhSchemaId omhSchemaId = new OmhSchemaId(schemaId);

        LOGGER.log(Level.FINE, "Parsing the start and end dates, if given.");
        DateTime startDateObject =
            (startDate == null) ?
                null :
                OHMAGE_DATE_TIME_FORMATTER.parseDateTime(startDate);
        DateTime endDateObject =
            (endDate == null) ?
                null :
                OHMAGE_DATE_TIME_FORMATTER.parseDateTime(endDate);

        LOGGER.log(Level.INFO, "Validating the column list, if given.");
        ColumnList columnListObject = null;
        if(columnList != null) {
            columnListObject = new ColumnList(columnList);

            LOGGER.log(Level.INFO, "Validating the column list.");
            Set<String> columnListRoots =
                new HashSet<String>(columnListObject.getChildren());
            columnListRoots.removeAll(ALLOWED_COLUMN_LIST_ROOTS);
            if(columnListRoots.size() > 0) {
                throw
                    new InvalidArgumentException(
                        "The root of every element in a column list must be " +
                            "one of: " +
                            ALLOWED_COLUMN_LIST_ROOTS.toString());
            }
        }

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        MultiValueResult<?> result;
        switch(omhSchemaId.type) {
            case STREAM:
                LOGGER.log(Level.INFO, "The schema is a stream.");
                result =
                    StreamDataBin
                        .getInstance()
                        .getStreamData(
                            user.getUsername(),
                            omhSchemaId.ohmageSchemaId,
                            schemaVersion,
                            startDateObject,
                            endDateObject,
                            columnListObject);
                break;

            case SURVEY:
                LOGGER.log(Level.INFO, "The schema is a survey.");
                result =
                    SurveyResponseBin
                        .getInstance()
                        .getSurveyResponses(
                            user.getUsername(),
                            omhSchemaId.ohmageSchemaId,
                            schemaVersion,
                            null,
                            startDateObject,
                            endDateObject,
                            columnListObject);
                break;

            default:
                throw
                    new UnknownEntityException(
                        "The schema ID-verion pair is unknown.");
        }

        LOGGER.log(Level.INFO, "Returning the schema.");
        return result;
    }
}