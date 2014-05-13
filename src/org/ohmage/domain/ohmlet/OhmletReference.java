package org.ohmage.domain.ohmlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.ohmlet.Ohmlet.SchemaReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A reference to a ohmlet and, optionally, specific streams and surveys
 * that that ohmlet defines that should be ignored by this user.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmletReference {
    /**
     * <p>
     * Allows a user to choose how data points are shared with the referenced
     * ohmlet.
     * </p>
     *
     * @author John Jenkins
     */
    public static enum PrivacyState {
        /**
         * Each data point shared with this ohmlet will use a randomly selected
         * unique identifier representing the user.
         */
        PRIVATE,
        /**
         * All data points shared with this ohmlet will use the user's
         * pseudodnym.
         */
        OHMLET,
        /**
         * All data points shared with this ohmlet will use the user's unique
         * identifier.
         */
        PUBLIC;

        /**
         * Returns the name of the enum as a lower-case string.
         */
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * <p>
     * A builder for {@link OhmletReference}s.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder {
        /**
         * The ohmlet's unique identifier.
         */
        private String ohmletId;
        /**
         * Specific stream references that should be ignored.
         */
        private Set<SchemaReference> ignoredStreams;
        /**
         * Specific survey references that should be ignored.
         */
        private Set<SchemaReference> ignoredSurveys;
        /**
         * The pseudonym for this ohmlet.
         */
        private String pseudonym;
        /**
         * The user's privacy state.
         */
        private PrivacyState privacyState;
        /**
         * Ohmlet name
         */
        private String name;

        /**
         * Creates a new builder.
         *
         * @param ohmletId
         *        The ohmlet's unique identifier.
         *
         * @param ignoredStreams
         *        The set of stream references that should be ignored.
         *
         * @param ignoredSurveys
         *        The set of survey references that should be ignored.
         */
        @JsonCreator
        public Builder(
            @JsonProperty(JSON_KEY_OHMLET_ID)
                final String ohmletId,
            @JsonProperty(JSON_KEY_IGNORED_STREAMS)
                final Set<SchemaReference> ignoredStreams,
            @JsonProperty(JSON_KEY_IGNORED_SURVEYS)
                final Set<SchemaReference> ignoredSurveys,
            @JsonProperty(JSON_KEY_NAME)
                final String name) {

            this.ohmletId = ohmletId;
            this.ignoredStreams = ignoredStreams;
            this.ignoredSurveys = ignoredSurveys;
            this.name = name;
        }

        /**
         * Creates a new builder that is initialized with an existing
         * ohmlet reference.
         *
         * @param ohmletReference
         *        An existing {@link OhmletReference} object.
         */
        @JsonCreator
        public Builder(final OhmletReference ohmletReference) {
            ohmletId = ohmletReference.ohmletId;
            ignoredStreams = ohmletReference.ignoredStreams;
            ignoredSurveys = ohmletReference.ignoredSurveys;
            pseudonym = ohmletReference.pseudonym;
            privacyState = ohmletReference.privacyState;
            name = ohmletReference.name;
        }

        /**
         * Sets the unique identifier for this ohmlet.
         *
         * @param ohmletId
         *        The ohmlet's unique identifier.
         *
         * @return This builder to facilitate chaining.
         */
        public OhmletReference.Builder setOhmletId(final String ohmletId) {
            this.ohmletId = ohmletId;

            return this;
        }

        /**
         * Returns the unique identifier for the ohmlet.
         *
         * @return The unique identifier for the ohmlet.
         */
        public String getOhmletId() {
            return ohmletId;
        }

        /**
         * Returns the name of the ohmlet.
         *
         * @return The name of the ohmlet.
         */
        public String getOhmletName() {
            return name;
        }

        /**
         * Adds a stream to ignore.
         *
         * @param streamReference
         *        The reference to the stream that should be ignored.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder ignoreStream(final SchemaReference streamReference) {
            if(ignoredStreams == null) {
                ignoredStreams = new HashSet<SchemaReference>();
            }

            ignoredStreams.add(streamReference);

            return this;
        }

        /**
         * Returns the set of ignored streams.
         *
         * @return The set of ignored streams.
         */
        public Set<SchemaReference> getIgnoredStreams() {
            return ignoredStreams;
        }

        /**
         * Removes a stream that is being ignored, meaning it should now be
         * seen by the user.
         *
         * @param streamReference
         *        The reference to the stream.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder stopIgnoringStream(
            final SchemaReference streamReference) {

            if(ignoredStreams == null) {
                return this;
            }

            ignoredStreams.remove(streamReference);

            return this;
        }

        /**
         * Stops ignoring all streams.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder stopIgnoringAllStreams() {
            ignoredStreams.clear();

            return this;
        }

        /**
         * Adds a survey to be ignored.
         *
         * @param surveyReference
         *        The reference to the survey that should be ignored.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder ignoreSurvey(final SchemaReference surveyReference) {
            if(ignoredSurveys == null) {
                ignoredSurveys = new HashSet<SchemaReference>();
            }

            ignoredSurveys.add(surveyReference);

            return this;
        }

        /**
         * Returns the set of ignored surveys.
         *
         * @return The set of ignored surveys.
         */
        public Set<SchemaReference> getIgnoredSurveys() {
            return ignoredSurveys;
        }

        /**
         * Removes a survey that is being ignored, meaning it should now be
         * seen by the user.
         *
         * @param surveyReference
         *        The reference to the survey.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder stopIgnoringSurvey(
            final SchemaReference surveyReference) {

            if(ignoredSurveys == null) {
                return this;
            }

            ignoredSurveys.remove(surveyReference);

            return this;
        }

        /**
         * Stops ignoring all surveys.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder stopIgnoringAllSurveys() {
            ignoredStreams.clear();

            return this;
        }

        /**
         * Returns the currently set pseudonym.
         *
         * @return The currently set pseudonym.
         */
        public String getPseudonym() {
            return pseudonym;
        }

        /**
         * Returns the currently set privacy state.
         *
         * @return The currently set privacy state.
         */
        public OhmletReference.PrivacyState getPrivacyState() {
            return privacyState;
        }

        /**
         * Builds a {@link OhmletReference} based on the state of this
         * builder.
         *
         * @return A {@link OhmletReference} based on the state of this
         *         builder.
         *
         * @throws InvalidArgumentException
         *         The state of this builder was not valid to build a new
         *         {@link OhmletReference} object.
         */
        public OhmletReference build() throws InvalidArgumentException {
            return new OhmletReference(
                ohmletId,
                name,
                ignoredStreams,
                ignoredSurveys,
                pseudonym,
                privacyState);
        }
    }

    /**
     * The default privacy state.
     */
    public static final PrivacyState DEFAULT_PRIVACY_STATE =
        PrivacyState.OHMLET;

    /**
     * The JSON key for the ohmlet's unique identifier.
     */
    public static final String JSON_KEY_OHMLET_ID = "ohmlet_id";

    /**
     * The JSON key for the ohmlet name.
     */
    public static final String JSON_KEY_NAME = "name";

    /**
     * The JSON key for the set of stream references that should be
     * ignored.
     */
    public static final String JSON_KEY_IGNORED_STREAMS =
        "ignored_streams";
    /**
     * The JSON key for the set of survey references that should be
     * ignored.
     */
    public static final String JSON_KEY_IGNORED_SURVEYS =
        "ignored_surveys";
    /**
     * The JSON key for the user's pseudonym in the ohmlet.
     */
    public static final String JSON_KEY_PSEUDONYM = "pseudonym";
    /**
     * The JSON key for the user's privacy state in the ohmlet.
     */
    public static final String JSON_KEY_PRIVACY_STATE = "privacy_state";

    /**
     * The unique identifier for the ohmlet that this object references.
     */
    @JsonProperty(JSON_KEY_OHMLET_ID)
    private final String ohmletId;
    /**
     * The unique identifier for the ohmlet that this object references.
     */
    @JsonProperty(JSON_KEY_NAME)
    private final String name;
    /**
     * The set of stream references that the ohmlet define(d) that should
     * be ignored.
     */
    @JsonProperty(JSON_KEY_IGNORED_STREAMS)
    private final Set<SchemaReference> ignoredStreams;
    /**
     * The set of survey references that the ohmlet define(d) that should
     * be ignored.
     */
    @JsonProperty(JSON_KEY_IGNORED_SURVEYS)
    private final Set<SchemaReference> ignoredSurveys;
    /**
     * The pseudonym for the user within the ohmlet.
     */
    @JsonProperty(JSON_KEY_PSEUDONYM)
    private final String pseudonym;
    /**
     * The user's privacy state within this ohmlet.
     */
    @JsonProperty(JSON_KEY_PRIVACY_STATE)
    private final PrivacyState privacyState;

    /**
     * Creates a new ohmlet reference for an ohmlet with the given unique
     * identifier and default values for the other fields.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     */
    public OhmletReference(final String ohmletId) {
        this(ohmletId, null, null, null, generatePseudonym(), null);
    }

    /**
     * Creates a new reference to a ohmlet.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
     * @param version
     *        The current internal version of the referenced ohmlet. This
     *        should never be stored in the database and should only be used
     *        for echoing back to the user.
     *
     * @param ignoredStreams
     *        The set of stream references that are defined by the ohmlet but
     *        should be ignored.
     *
     * @param ignoredSurveys
     *        The set of survey references that are defined by the ohmlet but
     *        should be ignored.
     *
     * @throws InvalidArgumentException
     *         The ohmlet identifier is null.
     */
    @JsonCreator
    public OhmletReference(
        @JsonProperty(JSON_KEY_OHMLET_ID) final String ohmletId,
        @JsonProperty(JSON_KEY_NAME) final String name,
        @JsonProperty(JSON_KEY_IGNORED_STREAMS)
            final Set<SchemaReference> ignoredStreams,
        @JsonProperty(JSON_KEY_IGNORED_SURVEYS)
            final Set<SchemaReference> ignoredSurveys,
        @JsonProperty(JSON_KEY_PSEUDONYM) final String pseudonym,
        @JsonProperty(JSON_KEY_PRIVACY_STATE) final PrivacyState privacyState)
        throws InvalidArgumentException {

        if(ohmletId == null) {
            throw new InvalidArgumentException("The ohmlet ID is null.");
        }
        if(pseudonym == null) {
            throw new InvalidArgumentException("The pseudonym is null.");
        }

        this.ohmletId = ohmletId;
        this.ignoredStreams =
            (ignoredStreams == null) ?
                new HashSet<SchemaReference>() :
                new HashSet<SchemaReference>(ignoredStreams);
        this.ignoredSurveys =
            (ignoredSurveys == null) ?
                new HashSet<SchemaReference>() :
                new HashSet<SchemaReference>(ignoredSurveys);
        this.pseudonym = pseudonym;
        this.privacyState =
            (privacyState == null) ? DEFAULT_PRIVACY_STATE : privacyState;
        this.name = name;
    }

    /**
     * Returns the unique identifier for the ohmlet.
     *
     * @return The unique identifier for the ohmlet.
     */
    public String getOhmletId() {
        return ohmletId;
    }

    /**
     * Returns the name of the ohmlet.
     *
     * @return The name of the ohmlet.
     */
    public String getOhmletName() {
        return name;
    }

    /**
     * Creates a new OhmletReference that is updated to ignore the given
     * stream.
     *
     * @param streamReference
     *        The reference to the stream that should be ignored.
     *
     * @return The updated OhmletReference.
     */
    public OhmletReference ignoreStream(
        final SchemaReference streamReference) {

        return (new Builder(this)).ignoreStream(streamReference).build();
    }

    /**
     * Returns the streams that the ohmlet defines but that should be
     * ignored.
     *
     * @return The streams that the ohmlet defines but that should be
     *         ignored.
     */
    public Set<SchemaReference> getIgnoredStreams() {
        return Collections.unmodifiableSet(ignoredStreams);
    }

    /**
     * Creates a new OhmletReference that is updated to stop ignoring the given
     * stream. If the user was not originally ignoring the stream, a new object
     * is returned that is identical to this one.
     *
     * @param streamReference
     *        The reference to the stream that should no longer be ignored.
     *
     * @return The updated OhmletReference.
     */
    public OhmletReference stopIgnoringStream(
        final SchemaReference streamReference) {

        return (new Builder(this)).stopIgnoringStream(streamReference).build();
    }

    /**
     * Creates a new OhmletReference that is updated to ignore the given
     * survey.
     *
     * @param surveyReference
     *        The reference to the survey that should be ignored.
     *
     * @return The updated OhmletReference.
     */
    public OhmletReference ignoreSurvey(
        final SchemaReference surveyReference) {

        return (new Builder(this)).ignoreSurvey(surveyReference).build();
    }

    /**
     * Returns the surveys that the ohmlet defines but that should be
     * ignored.
     *
     * @return The surveys that the ohmlet defines but that should be
     *         ignored.
     */
    public Set<SchemaReference> getIgnoredSurveys() {
        return Collections.unmodifiableSet(ignoredSurveys);
    }

    /**
     * Creates a new OhmletReference that is updated to stop ignoring the given
     * survey. If the user was not originally ignoring the survey, a new object
     * is returned that is identical to this one.
     *
     * @param surveyReference
     *        The reference to the survey that should no longer be ignored.
     *
     * @return The updated OhmletReference.
     */
    public OhmletReference stopIgnoringSurvey(
        final SchemaReference surveyReference) {

        return (new Builder(this)).stopIgnoringSurvey(surveyReference).build();
    }

    /**
     * Returns the user's pseudonym within this ohmlet.
     *
     * @return The user's pseudonym within this ohmlet.
     */
    public String getPseudonym() {
        return pseudonym;
    }

    /**
     * Generates and returns a new pseudonym.
     *
     * @return A new pseudonym.
     */
    private static String generatePseudonym() {
        return UUID.randomUUID().toString();
    }
}