package org.ohmage.domain.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.validator.routines.EmailValidator;
import org.mindrot.jbcrypt.BCrypt;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;
import org.ohmage.domain.jackson.MapValuesJsonSerializer;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;
import org.ohmage.domain.ohmlet.Ohmlet.SchemaReference;
import org.ohmage.domain.ohmlet.OhmletInvitation;
import org.ohmage.domain.ohmlet.OhmletReference;

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
         * The user's unique identifier.
         */
        private final String userId;
        /**
         * The user's password, plain-text or hashed.
         */
        private String password;
        /**
         * The user's email address.
         */
        private String email;
        /**
         * The user's full name.
         */
        private String fullName;
        /**
         * The authenticated information from a provider about this user.
         */
        private Map<String, ProviderUserInformation> providers =
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
         * The user's self-registration information or null if the user is not
         * self-registered.
         */
        private Registration registration;
        /**
         * The unique identifier for the invitation that the user used to
         * create their account.
         */
        private String invitationId;
        /**
         * The set of ohmlet invitations that a user may redeem to join an ohmlet.
         */
        private Set<OhmletInvitation> ohmletInvitations;

        /**
         * Creates a new builder with the outward-facing allowed parameters.
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
            @JsonProperty(JSON_KEY_EMAIL) final String email,
            @JsonProperty(JSON_KEY_FULL_NAME) final String fullName) {

            super(null);

            userId = generateId();
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

            userId = user.id;
            password = user.password;
            email = user.email;
            fullName = user.fullName;
            providers =
                new HashMap<String, ProviderUserInformation>(user.providers);
            ohmlets = new HashMap<String, OhmletReference>(user.ohmlets);
            streams = new HashSet<SchemaReference>(user.streams);
            surveys = new HashSet<SchemaReference>(user.surveys);
            registration = user.registration;
            invitationId = user.invitationId;
            ohmletInvitations =
                new HashSet<OhmletInvitation>(user.ohmletInvitations.values());
        }

        /**
         * Returns the new user's unique identifier.
         *
         * @return The new user's unique identifier.
         */
        public String getId() {
            return userId;
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
         * Returns the email address associated with this user.
         *
         * @return The email address associated with this user.
         */
        public String getEmail() {
            return email;
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
         * Returns the self-registration information for this user.
         *
         * @return The self-registration information for this user.
         */
        public Registration getRegistration() {
            return registration;
        }

        /**
         * Adds, replaces, or removes the self-registration information for
         * this user.
         *
         * @param registration
         *        The self-registration information for this user.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setRegistration(final Registration registration) {
            this.registration = registration;

            return this;
        }

        /**
         * Returns the unique identifier for the user invitation that this user
         * used to create their account.
         *
         * @return The unique identifier for the user invitation that this user
         *         used to create their account.
         */
        public Registration getInvitationId() {
            return registration;
        }

        /**
         * Adds, replaces, or removes the unique identifier for the user
         * invitation that this user used to create their account.
         *
         * @param invitationId
         *        The unique identifier for the user invitation that this user
         *        used to create their account.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setInvitationId(final String invitationId) {
            this.invitationId = invitationId;

            return this;
        }

        /**
         * Adds an ohmlet invitation to this user. This can also be used to
         * upgrade an existing ohmlet invitation by adding an invitation with
         * the same unique identifier and ohlmet ID as another invitation but
         * with different fields like the time-stamp it was accepted.
         *
         * @param ohmletInvitation
         *        The ohmlet invitation to add.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder addOhlmetInvitation(
            final OhmletInvitation ohmletInvitation) {

            if(ohmletInvitations == null) {
                ohmletInvitations = new HashSet<OhmletInvitation>();
            }

            ohmletInvitations.add(ohmletInvitation);

            return this;
        }

        /**
         * Removes an ohmlet invitation from this user.
         *
         * @param ohmletInvitation
         *        The ohmlet invitation to remove.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder removeOhmletInvitation(
            final OhmletInvitation ohmletInvitation) {

            if(ohmletInvitations == null) {
                ohmletInvitations = new HashSet<OhmletInvitation>();
            }

            ohmletInvitations.remove(ohmletInvitation);

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
        @Override
        public User build() {
            return new User(
                userId,
                password,
                email,
                fullName,
                (providers == null) ? null : providers.values(),
                (ohmlets == null) ? null : ohmlets.values(),
                streams,
                surveys,
                registration,
                invitationId,
                ohmletInvitations,
                internalReadVersion,
                internalWriteVersion);
        }
    }

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
     * The JSON key for the unique identifier for this user.
     */
    public static final String JSON_KEY_ID = "user_id";
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
     * The JSON key for the self-registration object.
     */
    public static final String JSON_KEY_REGISTRATION = "registration";
    /**
     * The JSON key for the invitation ID.
     */
    public static final String JSON_KEY_INVITATION_ID = "invitation_id";
    /**
     * The JSON key for the set of ohmlet invitations.
     */
    public static final String JSON_KEY_OHMLET_INVITATIONS =
        "ohmlet_invitations";

    /**
     * The internal unique ID for a user.
     */
    @JsonProperty(JSON_KEY_ID)
    @JsonFilterField
    private final String id;
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
    private final Map<String, OhmletReference> ohmlets;
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
     * The user's self-registration information or null if the user is not
     * self-registered.
     */
    @JsonProperty(JSON_KEY_REGISTRATION)
    @JsonFilterField
    private final Registration registration;
    /**
     * The unique identifier for the user invitation that the user used when
     * creating their account.
     */
    @JsonProperty(JSON_KEY_INVITATION_ID)
    @JsonFilterField
    private final String invitationId;
    /**
     * The set of ohmlet invitations that a user may redeem to join an ohmlet.
     */
    // FIXME: This should probably be done in a more abstract way. This should
    // probably reference just the ohmlet invitation IDs, which exist in an
    // external table. Then, both the inviter and invitee can access them and
    // use/revoke them.
    @JsonProperty(JSON_KEY_OHMLET_INVITATIONS)
    @JsonSerialize(using = MapValuesJsonSerializer.class)
    private final Map<String, OhmletInvitation> ohmletInvitations;

    /**
     * Creates a new User object.
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
     * @param registration
     *        The user's self-registration information if the user was
     *        self-registered; if not, null.
     *
     * @param invitationId
     *        The unique identifier of the user invitation that was used to
     *        create this account.
     *
     * @param ohmletInvitations
     *        The invitations to ohmlets that have been given to this user.
     *        They may or may not have been used.
     *
     * @throws InvalidArgumentException
     *         A required parameter is null or invalid.
     */
    public User(
        final String password,
        final String email,
        final String fullName,
        final List<ProviderUserInformation> providers,
        final Set<OhmletReference> ohmlets,
        final Set<SchemaReference> streams,
        final Set<SchemaReference> surveys,
        final Registration registration,
        final String invitationId,
        final Set<OhmletInvitation> ohmletInvitations)
        throws InvalidArgumentException {

        // Pass through to the builder constructor.
        this(
            generateId(),
            password,
            email,
            fullName,
            providers,
            ohmlets,
            streams,
            surveys,
            registration,
            invitationId,
            ohmletInvitations,
            null);
    }

    /**
     * Rebuilds an existing user.
     *
     * @param id
     *        The internal unique identifier for this user.
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
     * @param registration
     *        The user's self-registration information if the user was
     *        self-registered; if not, null.
     *
     * @param invitationId
     *        The unique identifier of the user invitation that was used to
     *        create this account.
     *
     * @param ohmletInvitations
     *        The invitations to ohmlets that have been given to this user.
     *        They may or may not have been used.
     *
     * @param internalVersion
     *        The internal version of this entity.
     *
     * @throws InvalidArgumentException
     *         A required parameter is null or invalid.
     */
    @JsonCreator
    protected User(
        @JsonProperty(JSON_KEY_ID) final String id,
        @JsonProperty(JSON_KEY_PASSWORD) final String password,
        @JsonProperty(JSON_KEY_EMAIL) final String email,
        @JsonProperty(JSON_KEY_FULL_NAME) final String fullName,
        @JsonProperty(JSON_KEY_PROVIDERS)
            final List<ProviderUserInformation> providers,
        @JsonProperty(JSON_KEY_OHMLETS) final Set<OhmletReference> ohmlets,
        @JsonProperty(JSON_KEY_STREAMS) final Set<SchemaReference> streams,
        @JsonProperty(JSON_KEY_SURVEYS) final Set<SchemaReference> surveys,
        @JsonProperty(JSON_KEY_REGISTRATION) final Registration registration,
        @JsonProperty(JSON_KEY_INVITATION_ID) final String invitationId,
        @JsonProperty(JSON_KEY_OHMLET_INVITATIONS)
            final Set<OhmletInvitation> ohmletInvitations,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        // Pass through to the builder constructor.
        this(
            id,
            password,
            email,
            fullName,
            providers,
            ohmlets,
            streams,
            surveys,
            registration,
            invitationId,
            ohmletInvitations,
            internalVersion,
            null);
    }

    /**
     * Builds the User object.
     *
     * @param id
     *        The internal unique identifier for this user.
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
     * @param registration
     *        The user's self-registration information if the user was
     *        self-registered; if not, null.
     *
     * @param invitationId
     *        The unique identifier of the user invitation that was used to
     *        create this account.
     *
     * @param ohmletInvitations
     *        The invitations to ohmlets that have been given to this user.
     *        They may or may not have been used.
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
        final String id,
        final String password,
        final String email,
        final String fullName,
        final Collection<ProviderUserInformation> providers,
        final Collection<OhmletReference> ohmlets,
        final Set<SchemaReference> streams,
        final Set<SchemaReference> surveys,
        final Registration registration,
        final String invitationId,
        final Set<OhmletInvitation> ohmletInvitations,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        // Initialize the parent.
        super(internalReadVersion, internalWriteVersion);

        // Validate the parameters.
        if(id == null) {
            throw
                new IllegalArgumentException(
                    "The user's internal ID is null.");
        }
        if(email == null) {
            throw new InvalidArgumentException("The email address is null.");
        }

        // Save the state.
        this.id = id;
        this.password = password;
        this.email = validateEmail(email);
        this.fullName = validateName(fullName);

        this.providers = new HashMap<String, ProviderUserInformation>();
        if(providers != null) {
            for(ProviderUserInformation information : providers) {
                this.providers.put(information.getProviderId(), information);
            }
        }

        // Ensure that either a password or at least one provider exists.
        if((this.password == null) && (this.providers.size() == 0)) {
            throw
                new InvalidArgumentException(
                    "The user must either have a password set or have at " +
                        "least one provider account to use to authenticate " +
                        "themselves.");
        }

        this.ohmlets = new HashMap<String, OhmletReference>();
        if(ohmlets != null) {
            for(OhmletReference ohmletReference : ohmlets) {
                this.ohmlets
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

        this.registration = registration;
        this.invitationId = invitationId;

        this.ohmletInvitations = new HashMap<String, OhmletInvitation>();
        if(ohmletInvitations != null) {
            for(OhmletInvitation ohmletInvitation : ohmletInvitations) {
                this
                    .ohmletInvitations
                    .put(ohmletInvitation.getId(), ohmletInvitation);
            }
        }
    }

    /**
     * Returns the unique identifier for this user which should be used to
     * coordinate objects owned by this user.
     *
     * @return The unique identifier for this user.
     */
    public String getId() {
        return id;
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
     * Creates a new User object that is identical to this one that is now
     * indicated as being part of the given ohmlet.
     *
     * @param ohmletReference
     *        The reference to the ohmlet.
     *
     * @return The new, updated User object.
     */
    public User joinOhmlet(final OhmletReference ohmletReference) {
        return (new Builder(this)).addOhmlet(ohmletReference).build();
    }

    /**
     * Returns the unmodifiable collection of ohmlet IDs that this user is
     * watching.
     *
     * @return The unmodifiable collection of ohmlet IDs that this user is
     *         watching.
     */
    public Collection<OhmletReference> getOhmlets() {
        return Collections.unmodifiableCollection(ohmlets.values());
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
        return ohmlets.get(ohmletId);
    }

    /**
     * Creates a new User from this user with an updated reference to an
     * ohmlet. If this user was not already associated with the given ohmlet,
     * they will now be.
     *
     * @param ohmletReference
     *        The reference to the ohmlet which should be inserted or updated.
     *
     * @return A new User with the new reference to the ohmlet.
     */
    public User upsertOhmlet(final OhmletReference ohmletReference) {
        return (new Builder(this)).upsertOhmlet(ohmletReference).build();
    }

    /**
     * Creates a new User object that is identical to this one that is now no
     * longer associated (within the User object only) with an ohmlet.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
     * @return The updated User object.
     */
    public User leaveOhmlet(final String ohmletId) {
        return (new User.Builder(this)).removeOhmlet(ohmletId).build();
    }

    /**
     * Creates a new User identical to this one that now follows the given
     * stream.
     *
     * @param streamReference
     *        The reference to the stream that the user is now following.
     *
     * @return The updated User.
     */
    public User followStream(final SchemaReference streamReference) {
        return (new Builder(this)).addStream(streamReference).build();
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
     * Creates a new User identical to this one that no longer follows the
     * given stream.
     *
     * @param streamReference
     *        The reference to the stream that should no longer be followed.
     *
     * @return The updated User.
     */
    public User ignoreStream(final SchemaReference streamReference) {
        return (new Builder(this)).removeStream(streamReference).build();
    }

    /**
     * Creates a new User identical to this one that now follows the given
     * survey.
     *
     * @param surveyReference
     *        The reference to the survey that the user is now following.
     *
     * @return The updated User.
     */
    public User followSurvey(final SchemaReference surveyReference) {
        return (new Builder(this)).addSurvey(surveyReference).build();
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
     * Creates a new User identical to this one that no longer follows the
     * given survey.
     *
     * @param surveyReference
     *        The reference to the survey that should no longer be followed.
     *
     * @return The updated User.
     */
    public User ignoreSurvey(final SchemaReference surveyReference) {
        return (new Builder(this)).removeSurvey(surveyReference).build();
    }

    /**
     * Returns the self-registration information for this user if they were
     * self-registered. Otherwise, null is returned.
     *
     * @return The self-registration information for this user if they were
     *         self-registered; otherwise, null.
     */
    public Registration getRegistration() {
        return registration;
    }

    /**
     * Returns the unique identifier for the user invitation used to create
     * this account.
     *
     * @return The unique identifier for the user invitation used to create
     *         this account.
     */
    public String getInvitationId() {
        return invitationId;
    }

    /**
     * Returns the set of ohmlet invitations for this user.
     *
     * @return The set of ohmlet invitations for this user.
     */
    public Collection<OhmletInvitation> getOhmletInvitations() {
        return Collections.unmodifiableCollection(ohmletInvitations.values());
    }

    /**
     * Returns a specific ohmlet invitation based on the given ohmlet
     * invitation ID or null if no such ID is known for this user.
     *
     * @param invitationId
     *        The ohmlet invitation's unique identifier.
     *
     * @return The ohmlet invitation or null if the invitation is unknown for
     *         this user.
     */
    public OhmletInvitation getOhmletInvitation(
        final String ohmletInvitationId) {

        return ohmletInvitations.get(ohmletInvitationId);
    }

    /**
     * Returns a specific ohmlet invitation based on the given ohmlet ID or
     * null if no such ID is known for this user.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
     * @return The ohmlet invitation or null if the invitation is unknown for
     *         this user.
     */
    public OhmletInvitation getOhmletInvitationFromOhmletId(
        final String ohmletId) {

        for(OhmletInvitation ohmletInvitation : ohmletInvitations.values()) {
            if(ohmletInvitation.getOhmletId().equals(ohmletId)) {
                return ohmletInvitation;
            }
        }

        return null;
    }

    /**
     * Updates this user's activation status by creating a new User object with
     * all of the same fields as this object except that the registration now
     * indicates that the account has been activated.
     *
     * @return The new User object that represents the activation.
     */
    public User activate() {
        return
            (new Builder(this))
                .setRegistration(
                    (new Registration.Builder(registration))
                        .setActivationTimestamp(System.currentTimeMillis())
                        .build())
                .build();
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

    /**
     * Generates a random unique identifier to use as a user's ID.
     *
     * @return A random unique identifier to use as a user's ID.
     */
    private static String generateId() {
        return UUID.randomUUID().toString();
    }
}