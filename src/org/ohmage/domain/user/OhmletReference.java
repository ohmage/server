package org.ohmage.domain.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ohmage.domain.Ohmlet.SchemaReference;
import org.ohmage.domain.exception.InvalidArgumentException;

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
                final Set<SchemaReference> ignoredSurveys) {

            this.ohmletId = ohmletId;
            this.ignoredStreams = ignoredStreams;
            this.ignoredSurveys = ignoredSurveys;
        }

        /**
         * Creates a new builder that is initialized with an existing
         * ohmlet reference.
         *
         * @param ohmletReference
         *        An existing {@link OhmletReference} object.
         */
        @JsonCreator
        public Builder(
            final OhmletReference ohmletReference) {
            ohmletId = ohmletReference.ohmletId;
            ignoredStreams = ohmletReference.ignoredStreams;
            ignoredSurveys = ohmletReference.ignoredSurveys;
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
         * Adds a stream to ignore.
         *
         * @param streamReference
         *        The reference to the stream that should be ignored.
         *
         * @return This builder to facilitate chaining.
         */
        public OhmletReference.Builder addStream(final SchemaReference streamReference) {
            if(ignoredStreams == null) {
                ignoredStreams = new HashSet<SchemaReference>();
            }

            ignoredStreams.add(streamReference);

            return this;
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
        public OhmletReference.Builder removeStream(
            final SchemaReference streamReference) {

            if(ignoredStreams == null) {
                return this;
            }

            ignoredStreams.remove(streamReference);

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
        public OhmletReference.Builder addSurvey(final SchemaReference surveyReference) {
            if(ignoredSurveys == null) {
                ignoredSurveys = new HashSet<SchemaReference>();
            }

            ignoredSurveys.add(surveyReference);

            return this;
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
        public OhmletReference.Builder removeSurvey(
            final SchemaReference surveyReference) {

            if(ignoredSurveys == null) {
                return this;
            }

            ignoredSurveys.remove(surveyReference);

            return this;
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
                ignoredStreams,
                ignoredSurveys);
        }
    }

    /**
     * The JSON key for the ohmlet's unique identifier.
     */
    public static final String JSON_KEY_OHMLET_ID = "ohmlet_id";
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
     * The unique identifier for the ohmlet that this object references.
     */
    @JsonProperty(JSON_KEY_OHMLET_ID)
    private final String ohmletId;
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
     * Creates a new reference to a ohmlet.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
     * @param ignoredStreams
     *        The set of stream references that are defined by the ohmlet
     *        but should be ignored.
     *
     * @param ignoredSurveys
     *        The set of survey references that are defined by the ohmlet
     *        but should be ignored.
     *
     * @throws InvalidArgumentException
     *         The ohmlet identifier is null.
     */
    @JsonCreator
    public OhmletReference(
        @JsonProperty(JSON_KEY_OHMLET_ID)
            final String ohmletId,
        @JsonProperty(JSON_KEY_IGNORED_STREAMS)
            final Set<SchemaReference> ignoredStreams,
        @JsonProperty(JSON_KEY_IGNORED_SURVEYS)
            final Set<SchemaReference> ignoredSurveys)
        throws InvalidArgumentException {

        if(ohmletId == null) {
            throw new InvalidArgumentException("The ohmlet ID is null.");
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
     * Returns the surveys that the ohmlet defines but that should be
     * ignored.
     *
     * @return The surveys that the ohmlet defines but that should be
     *         ignored.
     */
    public Set<SchemaReference> getIgnoredSurveys() {
        return Collections.unmodifiableSet(ignoredSurveys);
    }
}