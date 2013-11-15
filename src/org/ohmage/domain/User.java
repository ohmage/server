package org.ohmage.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.mindrot.jbcrypt.BCrypt;
import org.ohmage.domain.Ohmlet.SchemaReference;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;
import org.ohmage.domain.jackson.MapValuesJsonSerializer;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * <p>
 * A user in the system.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(User.JACKSON_FILTER_GROUP_ID)
public class User extends OhmageDomainObject {
    /**
     * <p>
     * A builder for constructing {@link User} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder extends OhmageDomainObject.Builder<User> {
        /**
         * The user's user-name.
         */
        protected String username;
        /**
         * The user's password, plain-text or hashed.
         */
        protected String password;
        /**
         * The user's email address.
         */
        protected String email;
        /**
         * The user's full name.
         */
        protected String fullName;
        /**
         * The authenticated information from a provider about this user.
         */
        protected Map<String, ProviderUserInformation> providers =
            new HashMap<String, ProviderUserInformation>();
        /**
         * The collection of ohmlets to which this user has subscribed.
         */
        private Map<String, OhmletReference> ohmlets;
        /**
         * The set of streams and, optionally, a version of each stream that
         * the user is tracking.
         */
        private Set<SchemaReference> streams;
        /**
         * The set of surveys and, optionally, a version of each survey that
         * the user is tracking.
         */
        private Set<SchemaReference> surveys;

        /**
         * Creates a new builder with the outward-facing allowed parameters.
         *
         * @param username
         *        The user-name of the user.
         *
         * @param password
         *        The hashed password of the user.
         *
         * @param email
         *        The email address of the user.
         *
         * @param fullName
         *        The full name of the user, which may be null.
         */
        @JsonCreator
        public Builder(
            @JsonProperty(JSON_KEY_USERNAME) final String username,
            @JsonProperty(JSON_KEY_EMAIL) final String email,
            @JsonProperty(JSON_KEY_FULL_NAME) final String fullName) {

            super(null);

            this.username = username;
            this.email = email;
            this.fullName = fullName;
        }

        /**
         * Creates a new builder based on an existing User object.
         *
         * @param user
         *        The existing User object on which this Builder should be
         *        based.
         */
        public Builder(
            final User user) {
            super(user);

            username = user.username;
            password = user.password;
            email = user.email;
            fullName = user.fullName;
            providers = user.providers;
            ohmlets = user.communities;
            streams = user.streams;
            surveys = user.surveys;
        }

        /**
         * Returns the currently set user-name of the user.
         *
         * @return The currently set user-name of the user.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Returns the currently set password or null if no password is set.
         *
         * @return The currently set password or null if no password is set.
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the password for the user. If the password is in plain-text,
         * the 'hash' parameter should always be set to true to avoid
         * attempting to create an account with a plain-text password. To
         * remove the password, pass null for the password and "false" for the
         * 'hash' parameter.
         *
         * @param password
         *        The password. This may already be hashed or it may be
         *        plain-text.
         *
         * @param hash
         *        Whether or not the password should be hashed. For plain-text
         *        passwords, this should always be true.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setPassword(final String password, final boolean hash) {
            if(hash) {
                this.password = hashPassword(password);
            }
            else {
                this.password = password;
            }

            return this;
        }

        /**
         * Sets the email address of the user. User null to remove the value.
         *
         * @param email
         *        The email address of the user or null to remove the currently
         *        set value.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setEmail(final String email) {
            this.email = email;

            return this;
        }

        /**
         * Returns the currently set full name of the user, which may be null
         * indicating that there is no full name set.
         *
         * @return The currently set full name of the user or null if there is
         *         no currently set full name.
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * Sets the full name of the user. Use null to remove the value.
         *
         * @param fullName
         *        The full name of the user or null to remove the current full
         *        name.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setFullName(final String fullName) {
            this.fullName = fullName;

            return this;
        }

        /**
         * Directly sets the map of providers. This can be used to remove the
         * entire map of providers by passing null. It is recommended to
         * instead use {@link #addProvider(String, ProviderUserInformation)}
         * and {@link #removeProvider(String)}.
         *
         * @param providers
         *        The new map of providers.
         *
         * @return This builder to facilitate chaining.
         *
         * @see #addProvider(String, ProviderUserInformation)
         * @see #removeProvider(String)
         */
        public Builder setProviders(
            final Map<String, ProviderUserInformation> providers) {
            this.providers = providers;

            return this;
        }

        /**
         * Adds new or overwrites existing information as generated by a
         * provider.
         *
         * @param providerId
         *        The provider's unique identifier.
         *
         * @param information
         *        The information generated by the provider.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addProvider(
            final String providerId,
            final ProviderUserInformation information) {

            providers.put(providerId, information);

            return this;
        }

        /**
         * Removes a provider's information about this user.
         *
         * @param providerId
         *        The provider's unique identifier.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeProvider(final String providerId) {
            providers.remove(providerId);

            return this;
        }

        /**
         * Returns the currently-set information about a user from a provider.
         *
         * @param provider
         *        The provider's unique identifier.
         *
         * @return The currently set information about a user from a provider.
         */
        public ProviderUserInformation getProvider(final String provider) {
            return providers.get(provider);
        }

        /**
         * Adds a ohmlet to the list of ohmlets being watched by this user.
         *
         * @param ohmletReference
         *        The information referencing the ohmlet and the user's
         *        specific view of the ohmlet.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addOhmlet(
            final OhmletReference ohmletReference) {

            if(ohmlets == null) {
                ohmlets = new HashMap<String, OhmletReference>();
            }

            ohmlets
                .put(ohmletReference.getOhmletId(), ohmletReference);

            return this;
        }

        /**
         * Updates a reference to a ohmlet.
         *
         * @param ohmletReference
         *        The reference to the ohmlet. If no such reference already
         *        exists, then a new one is created.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder upsertOhmlet(
            final OhmletReference ohmletReference) {

            removeOhmlet(ohmletReference.getOhmletId());
            addOhmlet(ohmletReference);

            return this;
        }

        /**
         * Ensures that a user is no longer watching a ohmlet.
         *
         * @param ohmletId
         *        The ohmlet's unique identifier.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeOhmlet(final String ohmletId) {
            if(ohmlets == null) {
                return this;
            }

            ohmlets.remove(ohmletId);

            return this;
        }

        /**
         * Adds a stream to the set of streams being followed by this user.
         *
         * @param streamReference
         *        The stream's reference.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addStream(final SchemaReference streamReference) {
            if(streams == null) {
                streams = new HashSet<SchemaReference>();
            }

            streams.add(streamReference);

            return this;
        }

        /**
         * Ensures that a user is no longer following a stream. If a specific
         * version is given, the user will no longer be following that version.
         * If no version is given, the user will no longer be following the
         * latest version but may still be following other, specific versions.
         *
         * @param streamReference
         *        The reference to the stream.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeStream(final SchemaReference streamReference) {
            if(streams == null) {
                streams = new HashSet<SchemaReference>();
            }

            streams.remove(streamReference);

            return this;
        }

        /**
         * Adds a survey to the set of surveys being followed by this user.
         *
         * @param surveyReference
         *        The survey's reference.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addSurvey(final SchemaReference surveyReference) {
            if(surveys == null) {
                surveys = new HashSet<SchemaReference>();
            }

            surveys.add(surveyReference);

            return this;
        }

        /**
         * Ensures that a user is no longer following a survey. If a specific
         * version is given, the user will no longer be following that version.
         * If no version is given, the user will no longer be following the
         * latest version but may still be following other, specific versions.
         *
         * @param surveyReference
         *        The reference to the survey.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeSurvey(final SchemaReference surveyReference) {
            if(surveys == null) {
                surveys = new HashSet<SchemaReference>();
            }

            surveys.remove(surveyReference);

            return this;
        }

        /**
         * Creates a {@link User} object based on the state of this builder.
         *
         * @return A {@link User} object based on the state of this builder.
         *
         * @throws OhmageException
         *         The state of the builder contained invalid fields.
         */
        public User build() {
            return new User(
                username,
                password,
                email,
                fullName,
                (providers == null) ? null : providers.values(),
                (ohmlets == null) ? null : ohmlets.values(),
                streams,
                surveys,
                internalReadVersion,
                internalWriteVersion);
        }
    }

    /**
     * <p>
     * A reference to a ohmlet and, optionally, specific streams and surveys
     * that that ohmlet defines that should be ignored by this user.
     * </p>
     *
     * @author John Jenkins
     */
    public static class OhmletReference {
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
            public Builder setOhmletId(final String ohmletId) {
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
            public Builder addStream(final SchemaReference streamReference) {
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
            public Builder removeStream(
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
            public Builder addSurvey(final SchemaReference surveyReference) {
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
            public Builder removeSurvey(
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

    /**
     * The minimum allowed length for a user-name.
     *
     * @see #validateUsername(String)
     */
    public static final int USERNAME_LENGTH_MIN = 3;
    /**
     * The maximum allowed length for a user-name.
     *
     * @see #validateUsername(String)
     */
    public static final int USERNAME_LENGTH_MAX = 25;

    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.User";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(User.class);
    }

    /**
     * The number of rounds for BCrypt to use when generating a salt.
     */
    private static final int BCRYPT_SALT_ROUNDS = 12;

    /**
     * The JSON key for the user-name.
     */
    public static final String JSON_KEY_USERNAME = "username";
    /**
     * The JSON key for the password.
     */
    public static final String JSON_KEY_PASSWORD = "password";
    /**
     * The JSON key for the email.
     */
    public static final String JSON_KEY_EMAIL = "email";
    /**
     * The JSON key for the full name.
     */
    public static final String JSON_KEY_FULL_NAME = "full_name";
    /**
     * The JSON key for the list of providers.
     */
    public static final String JSON_KEY_PROVIDERS = "providers";
    /**
     * The JSON key for the ohmlets.
     */
    public static final String JSON_KEY_OHMLETS = "ohmlets";
    /**
     * The JSON key for the streams.
     */
    public static final String JSON_KEY_STREAMS = "streams";
    /**
     * The JSON key for the surveys.
     */
    public static final String JSON_KEY_SURVEYS = "surveys";

    /**
     * The user's user-name.
     */
    @JsonProperty(JSON_KEY_USERNAME)
    private final String username;
    /**
     * The user's hashed password.
     */
    @JsonProperty(JSON_KEY_PASSWORD)
    @JsonFilterField
    private final String password;
    /**
     * The user's email address.
     */
    @JsonProperty(JSON_KEY_EMAIL)
    private final String email;
    /**
     * The user's full name.
     */
    @JsonProperty(JSON_KEY_FULL_NAME)
    private final String fullName;
    /**
     * The list of providers that have been linked to this account.
     */
    @JsonProperty(JSON_KEY_PROVIDERS)
    @JsonSerialize(using = MapValuesJsonSerializer.class)
    private final Map<String, ProviderUserInformation> providers;
    /**
     * The list of ohmlets to which this user has subscribed.
     */
    @JsonProperty(JSON_KEY_OHMLETS)
    @JsonSerialize(using = MapValuesJsonSerializer.class)
    private final Map<String, OhmletReference> communities;
    /**
     * The set of streams and, optionally, a version associated with each
     * stream, that this user is tracking.
     */
    @JsonProperty(JSON_KEY_STREAMS)
    private final Set<SchemaReference> streams;
    /**
     * The set of surveys and, optionally, a version associated with each
     * survey, that this user is tracking.
     */
    @JsonProperty(JSON_KEY_SURVEYS)
    private final Set<SchemaReference> surveys;

    /**
     * Creates a new User object.
     *
     * @param username
     *        The user-name of the user.
     *
     * @param password
     *        The hashed password of the user.
     *
     * @param email
     *        The email address of the user.
     *
     * @param fullName
     *        The full name of the user, which may be null.
     *
     * @param providers
     *        The collection of information about providers that have
     *        authenticated this user.
     *
     * @param ohmlets
     *        The set of ohmlets to which the user is associated and their
     *        specific view of that ohmlet.
     *
     * @param streams
     *        A set of stream identifiers and, optionally, a version that this
     *        user is tracking.
     *
     * @param surveys
     *        A set of survey identifiers and, optionally, a version that this
     *        user is tracking.
     *
     * @throws InvalidArgumentException
     *         A required parameter is null or invalid.
     */
    public User(
        final String username,
        final String password,
        final String email,
        final String fullName,
        final List<ProviderUserInformation> providers,
        final Set<OhmletReference> communities,
        final Set<SchemaReference> streams,
        final Set<SchemaReference> surveys)
        throws InvalidArgumentException {

        // Pass through to the builder constructor.
        this(
            username,
            password,
            email,
            fullName,
            providers,
            communities,
            streams,
            surveys,
            null);
    }

    /**
     * Rebuilds an existing user.
     *
     * @param username
     *        The user-name of the user.
     *
     * @param password
     *        The hashed password of the user.
     *
     * @param email
     *        The email address of the user.
     *
     * @param fullName
     *        The full name of the user, which may be null.
     *
     * @param providers
     *        The collection of information about providers that have
     *        authenticated this user.
     *
     * @param ohmlets
     *        The set of ohmlets to which the user is associated and their
     *        specific view of that ohmlet.
     *
     * @param streams
     *        A set of stream identifiers and, optionally, a version that this
     *        user is tracking.
     *
     * @param surveys
     *        A set of survey identifiers and, optionally, a version that this
     *        user is tracking.
     *
     * @param internalVersion
     *        The internal version of this entity.
     *
     * @throws InvalidArgumentException
     *         A required parameter is null or invalid.
     */
    @JsonCreator
    protected User(
        @JsonProperty(JSON_KEY_USERNAME) final String username,
        @JsonProperty(JSON_KEY_PASSWORD) final String password,
        @JsonProperty(JSON_KEY_EMAIL) final String email,
        @JsonProperty(JSON_KEY_FULL_NAME) final String fullName,
        @JsonProperty(JSON_KEY_PROVIDERS)
            final List<ProviderUserInformation> providers,
        @JsonProperty(JSON_KEY_OHMLETS) final Set<OhmletReference> communities,
        @JsonProperty(JSON_KEY_STREAMS) final Set<SchemaReference> streams,
        @JsonProperty(JSON_KEY_SURVEYS) final Set<SchemaReference> surveys,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        // Pass through to the builder constructor.
        this(
            username,
            password,
            email,
            fullName,
            providers,
            communities,
            streams,
            surveys,
            internalVersion,
            null);
    }

    /**
     * Builds the User object.
     *
     * @param username
     *        The user-name of the user.
     *
     * @param password
     *        The hashed password of the user.
     *
     * @param email
     *        The email address of the user.
     *
     * @param fullName
     *        The full name of the user, which may be null.
     *
     * @param providers
     *        The collection of information about providers that have
     *        authenticated this user.
     *
     * @param ohmlets
     *        The set of ohmlets to which the user is associated and their
     *        specific view of that ohmlet.
     *
     * @param streams
     *        A set of stream identifiers and, optionally, a version that this
     *        user is tracking.
     *
     * @param surveys
     *        A set of survey identifiers and, optionally, a version that this
     *        user is tracking.
     *
     * @param internalReadVersion
     *        The version of this entity when it was read from the database.
     *
     * @param internalWriteVeresion
     *        The version of this entity when it will be written to the
     *        database.
     *
     * @throws InvalidArgumentException
     *         A required parameter is null or invalid.
     */
    private User(
        final String username,
        final String password,
        final String email,
        final String fullName,
        final Collection<ProviderUserInformation> providers,
        final Collection<OhmletReference> communities,
        final Set<SchemaReference> streams,
        final Set<SchemaReference> surveys,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        // Initialize the parent.
        super(internalReadVersion, internalWriteVersion);

        // Validate the parameters.
        if(username == null) {
            throw new InvalidArgumentException("The username is null.");
        }
        if(email == null) {
            throw new InvalidArgumentException("The email address is null.");
        }

        // Save the state.
        this.username = validateUsername(username);
        this.password = password;
        this.email = validateEmail(email);
        this.fullName = validateName(fullName);

        this.providers = new HashMap<String, ProviderUserInformation>();
        if(providers != null) {
            for(ProviderUserInformation information : providers) {
                this.providers.put(information.getProviderId(), information);
            }
        }

        this.communities = new HashMap<String, OhmletReference>();
        if(communities != null) {
            for(OhmletReference ohmletReference : communities) {
                this.communities
                    .put(
                        ohmletReference.getOhmletId(),
                        ohmletReference);
            }
        }

        this.streams =
            (streams == null) ?
                new HashSet<SchemaReference>() :
                new HashSet<SchemaReference>(streams);
        this.surveys =
            (surveys == null) ?
                new HashSet<SchemaReference>() :
                new HashSet<SchemaReference>(surveys);
    }

    /**
     * Returns the user-name of this user.
     *
     * @return The user-name of this user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of this user.
     *
     * @return The password of this user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the email address of this user.
     *
     * @return The email address of this user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the information from the provider for this user.
     *
     * @param providerId
     *        The provider's unique identifier.
     *
     * @return The information from the provider for this user or null if no
     *         such provider has been associated with this user.
     */
    public ProviderUserInformation getProvider(final String providerId) {
        return providers.get(providerId);
    }

    /**
     * Updates a provider's information for this user.
     *
     * @param information
     *        The information to add/update.
     *
     * @return The new User object that reflects this change.
     *
     * @throws IllegalArgumentException
     *         The information is null.
     */
    public User updateProvider(
        final ProviderUserInformation information)
        throws IllegalArgumentException {

        // Verify the input.
        if(information == null) {
            throw new IllegalArgumentException("The information is null.");
        }

        // Build a new User object with the new provider information.
        return (new Builder(this))
            .addProvider(information.getProviderId(), information)
            .build();
    }

    /**
     * Returns the unmodifiable collection of ohmlet IDs that this user is
     * watching.
     *
     * @return The unmodifiable collection of ohmlet IDs that this user is
     *         watching.
     */
    public Collection<OhmletReference> getCommunities() {
        return Collections.unmodifiableCollection(communities.values());
    }

    /**
     * Returns the ohmlet reference associated with this user if such a ohmlet
     * exists. Otherwise, null is returned.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
     * @return The ohmlet's reference or null if the user is not associated
     *         with the ohmlet.
     */
    public OhmletReference getOhmlet(final String ohmletId) {
        return communities.get(ohmletId);
    }

    /**
     * Returns the unmodifiable set of stream references that this user is
     * watching.
     *
     * @return The unmodifiable set of stream references that this user is
     *         watching.
     */
    public Set<SchemaReference> getStreams() {
        return Collections.unmodifiableSet(streams);
    }

    /**
     * Returns the unmodifiable set of survey references that this user is
     * watching.
     *
     * @return The unmodifiable set of survey references that this user is
     *         watching.
     */
    public Set<SchemaReference> getSurveys() {
        return Collections.unmodifiableSet(surveys);
    }

    /**
     * Verifies that a given password matches this user's password. This should
     * only be used if the user's account actually has a password.
     *
     * @param plaintextPassword
     *        The plain-text password to check against this user's password.
     *
     * @return True if the passwords match, false otherwise.
     */
    public boolean verifyPassword(final String plaintextPassword) {
        if(password == null) {
            throw new IllegalStateException(
                "The user account does not have a password.");
        }
        return BCrypt.checkpw(plaintextPassword, password);
    }

    /**
     * Updates this user's password by creating a new User object with all of
     * the same fields as this object except the password, which is set as the
     * given value.
     *
     * @param password
     *        The user's new password.
     *
     * @return The new User object that represents the password change.
     *
     * @throws IllegalArgumentException
     *         The password is null.
     */
    public User updatePassword(
        final String password)
        throws IllegalArgumentException {

        // Validate the input.
        if(password == null) {
            throw new IllegalArgumentException("The password is null.");
        }

        // Build a new User object with the new password.
        return (new Builder(this)).setPassword(password, false).build();
    }

    /**
     * Validates that a user-name is a valid user-name.
     *
     * @param username
     *        The user-name to validate.
     *
     * @return The trimmed and validated user-name.
     *
     * @throws IllegalArgumentException
     *         The user-name is not valid.
     */
    public static String validateUsername(
        final String username)
        throws IllegalArgumentException {

        // Verify that it is not null.
        if(username == null) {
            throw new IllegalArgumentException("The username is null.");
        }

        // Trim it and continue validation.
        String trimmedUsername = username.trim();

        // Verify that the user-name is not empty.
        if(trimmedUsername.length() == 0) {
            throw new IllegalArgumentException("The username is empty.");
        }

        // Verify that the user-name has at least as long as the minimum.
        if(trimmedUsername.length() < USERNAME_LENGTH_MIN) {
            throw new IllegalArgumentException(
                "The username is too short. It must be at least " +
                    USERNAME_LENGTH_MIN +
                    " characters.");
        }

        // Verify that the user-name has at least as long as the minimum.
        if(trimmedUsername.length() > USERNAME_LENGTH_MAX) {
            throw new IllegalArgumentException(
                "The username is too long. It must be less than " +
                    USERNAME_LENGTH_MAX +
                    " characters.");
        }

        // Return the trimmed, validated user-name.
        return trimmedUsername;
    }

    /**
     * Hashes a user's plain-text password.
     *
     * @param plaintextPassword
     *        The plain-text password to hash.
     *
     * @return The hashed password.
     */
    public static String hashPassword(
        final String plaintextPassword)
        throws IllegalArgumentException {

        // Verify that it is not null.
        if(plaintextPassword == null) {
            throw new IllegalArgumentException(
                "The plain-text password is null.");
        }

        // Verify that it is not empty.
        if(plaintextPassword.length() == 0) {
            throw new IllegalArgumentException(
                "The plain-text password is empty.");
        }

        return BCrypt
            .hashpw(plaintextPassword, BCrypt.gensalt(BCRYPT_SALT_ROUNDS));
    }

    /**
     * Validates that a user's email address is a valid email address. Note
     * that there is no verification that the email address actually exists,
     * only that it is a valid email address.
     *
     * @param email
     *        The email to validate.
     *
     * @return The trimmed and validated email address.
     *
     * @throws IllegalArgumentException
     *         The email is not valid.
     */
    public static String validateEmail(
        final String email)
        throws IllegalArgumentException {

        // Verify that it is not null.
        if(email == null) {
            throw new IllegalArgumentException("The email address is null.");
        }

        // Trim it and continue validation.
        String trimmedEmail = email.trim();

        // Verify that the email is not empty.
        if(trimmedEmail.length() == 0) {
            throw new IllegalArgumentException("The email address is empty.");
        }

        // Verify that the email address is a valid email address even if the
        // email address doesn't actually exist.
        EmailValidator.getInstance().isValid(email);

        // Return the trimmed, validated email address.
        return trimmedEmail;
    }

    /**
     * Validates that a name is valid.
     *
     * @param name
     *        The name to validate.
     *
     * @return The trimmed and validated name or null if the parameter was null
     *         only whitespace.
     *
     * @throws IllegalArgumentException
     *         The name is not valid.
     */
    public static String validateName(
        final String name)
        throws IllegalArgumentException {

        // It is acceptable to be null.
        if(name == null) {
            return null;
        }

        // Trim it and continue validation.
        String trimmedName = name.trim();

        // If it is empty, that is the same as being non-existent.
        if(trimmedName.length() == 0) {
            return null;
        }

        // Return the trimmed, validated name.
        return trimmedName;
    }
}