package org.ohmage.domain.ohmlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.ohmlet.Ohmlet.SchemaReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A subclass of an {@link OhmletReference} that also includes the set of
 * stream and schema references that are referenced in the ohmlet and filtered
 * out by what is ignored by this user.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmletReferenceView extends OhmletReference {
    /**
     * <p>
     * A builder for {@link OhmletReferenceView}s.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder extends OhmletReference.Builder {
        /**
         * The set of stream references that the ohmlet defines after filtering
         * out those that should be ignored.
         */
        private final Set<SchemaReference> streams;
        /**
         * The set of survey references that the ohmlet defines after filtering
         * out those that should be ignored.
         */
        private final Set<SchemaReference> surveys;

        /**
         * Creates a new builder that is initialized with an existing
         * ohmlet reference. If this is specifically an
         * {@link OhmletReferenceView}, the streams and surveys will be copied
         * as well.
         *
         * @param ohmletReference
         *        An existing {@link OhmletReference} object.
         */
        @JsonCreator
        public Builder(final OhmletReference ohmletReference) {
            super(ohmletReference);

            if(ohmletReference instanceof OhmletReferenceView) {
                streams =
                    new HashSet<SchemaReference>(
                        ((OhmletReferenceView) ohmletReference).streams);
                surveys =
                    new HashSet<SchemaReference>(
                        ((OhmletReferenceView) ohmletReference).surveys);
            }
            else {
                streams = new HashSet<SchemaReference>();
                surveys = new HashSet<SchemaReference>();
            }
        }

        /**
         * Adds a new stream to the set of referenced streams.
         *
         * @param stream The reference to the stream to add.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addStream(final SchemaReference stream) {
            streams.add(stream);

            return this;
        }

        /**
         * Removes a stream from the set of referenced streams.
         *
         * @param stream The stream reference to remove.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeStream(final SchemaReference stream) {
            streams.remove(stream);

            return this;
        }

        /**
         * Adds a new survey to the set of referenced surveys.
         *
         * @param survey The reference to the survey to add.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addSurvey(final SchemaReference stream) {
            surveys.add(stream);

            return this;
        }

        /**
         * Removes a survey from the set of referenced surveys.
         *
         * @param survey The survey reference to remove.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeSurvey(final SchemaReference survey) {
            surveys.remove(survey);

            return this;
        }

        /**
         * @return An {@link OhmletReferenceView} based on the current state of
         *         this builder.
         */
        @Override
        public OhmletReferenceView build() {
            return
                new OhmletReferenceView(
                    getOhmletId(),
                    getOhmletName(),
                    getIgnoredStreams(),
                    getIgnoredSurveys(),
                    getPseudonym(),
                    getPrivacyState(),
                    streams,
                    surveys);
        }
    }

    /**
     * The JSON key for the filter list of streams.
     *
     * This is private because neither it nor its corresponding variable should
     * be relied on.
     */
    private static final String JSON_KEY_STREAMS = "streams";
    /**
     * The JSON key for the filter list of streams.
     *
     * This is private because neither it nor its corresponding variable should
     * be relied on.
     */
    private static final String JSON_KEY_SURVEYS = "surveys";

    /**
     * The set of stream references that the ohmlet defines after filtering out
     * those that should be ignored.
     */
    @JsonProperty(JSON_KEY_STREAMS)
    private final Set<SchemaReference> streams;
    /**
     * The set of survey references that the ohmlet defines after filtering out
     * those that should be ignored.
     */
    @JsonProperty(JSON_KEY_SURVEYS)
    private final Set<SchemaReference> surveys;

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
    public OhmletReferenceView(
        final String ohmletId,
        final String name,
        final Set<SchemaReference> ignoredStreams,
        final Set<SchemaReference> ignoredSurveys,
        final String pseudonym,
        final PrivacyState privacyState,
        final Set<SchemaReference> streams,
        final Set<SchemaReference> surveys)
        throws InvalidArgumentException {

        super(
            ohmletId,
            name,
            ignoredStreams,
            ignoredSurveys,
            pseudonym,
            privacyState);

        this.streams =
            ((streams == null) ?
                Collections.<SchemaReference>emptySet() :
                Collections
                    .unmodifiableSet(new HashSet<SchemaReference>(streams)));
        this.surveys =
            ((surveys == null) ?
                Collections.<SchemaReference>emptySet() :
                Collections
                    .unmodifiableSet(new HashSet<SchemaReference>(surveys)));
    }
}