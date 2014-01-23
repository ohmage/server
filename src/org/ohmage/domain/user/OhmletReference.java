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

        return (new Builder(this)).addStream(streamReference).build();
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

        return (new Builder(this)).removeStream(streamReference).build();
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

        return (new Builder(this)).addSurvey(surveyReference).build();
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

        return (new Builder(this)).removeSurvey(surveyReference).build();
    }
}