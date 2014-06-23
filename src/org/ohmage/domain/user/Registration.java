package org.ohmage.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.javax.servlet.listener.ConfigurationFileImport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.mail.smtp.SMTPTransport;

/**
 * <p>
 * A registration request for a user.
 * </p>
 *
 * @author John Jenkins
 */
public class Registration {
    public static class Builder {
        /**
         * The unique identifier for the user that owns this registration.
         */
        private final String userId;
        /**
         * The email address of the user where the registration link was sent.
         */
        private final String emailAddress;
        /**
         * The unique identifier for the user to use to activate their account.
         */
        private String activationId;
        /**
         * The number of milliseconds since the Unix epoch at which time the
         * user activated their account or null if the account has not yet been
         * activated.
         */
        private Long activationTimestamp;

        /**
         * Creates a new Builder object for a new Registration object.
         *
         * @param userId
         *        The user's unique identifier.
         *
         * @param emailAddress
         *        The user's email address.
         *
         * @param activationId
         *        The unique identifier for the user to use to activate their
         *        account.
         */
        public Builder(
            final String userId,
            final String emailAddress) {

            this.userId = userId;
            this.emailAddress = emailAddress;
        }

        /**
         * Creates a new Builder from an existing Registration object.
         *
         * @param registration The existing Registration object.
         */
        public Builder(final Registration registration) {
            userId = registration.userId;
            emailAddress = registration.emailAddress;
            activationId = registration.activationId;
        }

        /**
         * Recreates a new Builder.
         *
         * @param userId
         *        The user's unique identifier.
         *
         * @param emailAddress
         *        The user's email address.
         *
         * @param activationId
         *        The unique identifier for the user to use to activate their
         *        account.
         *
         * @param activationTimestamp
         *        The number of milliseconds since the Unix epoch at which time
         *        the user activated their account or null if the account has
         *        not yet been activated.
         */
        @JsonCreator
        protected Builder(
            @JsonProperty(JSON_KEY_USER_ID) final String userId,
            @JsonProperty(JSON_KEY_EMAIL) final String emailAddress,
            @JsonProperty(JSON_KEY_ACTIVATION_ID) final String activationId,
            @JsonProperty(JSON_KEY_ACTIVATION_TIMESTAMP)
                final Long activationTimestamp) {

            this.userId = userId;
            this.emailAddress = emailAddress;
            this.activationId = activationId;
            this.activationTimestamp = activationTimestamp;
        }

        /**
         * Sets the activation time-stamp for this registration.
         *
         * @param activationTimestamp
         *        The activation time-stamp as the number of milliseconds since
         *        the Unix epoch.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setActivationTimestamp(final Long activationTimestamp) {
            this.activationTimestamp = activationTimestamp;

            return this;
        }

        /**
         * Creates a {@link Registration} object based on the state of this
         * builder.
         *
         * @return The {@link Registration} object based on the state of this
         *         builder.
         */
        public Registration build() {
            return
                new Registration(
                    userId,
                    emailAddress,
                    activationId,
                    activationTimestamp);
        }
    }

    /**
     * The JSON key for the user's unique identifier.
     */
    public static final String JSON_KEY_USER_ID = "user_id";
    /**
     * The JSON key for the email address.
     */
    public static final String JSON_KEY_EMAIL = "email";
    /**
     * The JSON key for the activation ID.
     */
    public static final String JSON_KEY_ACTIVATION_ID = "activation_id";
    /**
     * The JSON key for the activation time-stamp.
     */
    public static final String JSON_KEY_ACTIVATION_TIMESTAMP =
        "activation_timestamp";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(Registration.class.getName());

    /**
     * The hashing function to use to generate the activation ID.
     */
    private static final String HASH_FUNCTION = "SHA-512";

    /**
     * The mail protocol to use when sending mail.
     */
    private static final String MAIL_PROTOCOL = "smtp";

    /**
     * The key to use to retrieve the email registration sender.
     */
    private static final String REGISTRATION_SENDER_KEY =
        "ohmage.user.registration.sender";
    /**
     * The email address to use to build the email to the self-registering
     * user.
     */
    private static final InternetAddress REGISTRATION_SENDER;
    static {
        try {
            REGISTRATION_SENDER =
                new InternetAddress(
                    ConfigurationFileImport
                        .getCustomProperties()
                        .getProperty(REGISTRATION_SENDER_KEY));
        }
        catch(AddressException e) {
            throw
                    new IllegalStateException(
                            "The sender email address is invalid.",
                            e);
        }
    }
    /**
     * The key to use to retrieve the email registration subject.
     */
    private static final String REGISTRATION_SUBJECT_KEY =
        "ohmage.user.registration.subject";
    /**
     * The registration subject to use to build the email to the
     * self-registering user.
     */
    private static final String REGISTRATION_SUBJECT =
        ConfigurationFileImport
            .getCustomProperties()
            .getProperty(REGISTRATION_SUBJECT_KEY);
    /**
     * The key to use to retrieve the email registration text.
     */
    private static final String REGISTRATION_TEXT_KEY =
        "ohmage.user.registration.text";
    /**
     * The registration text to use to build the email to the self-registering
     * user.
     */
    private static final String REGISTRATION_TEXT =
        ConfigurationFileImport
            .getCustomProperties()
            .getProperty(REGISTRATION_TEXT_KEY);
    /**
     * The specialized text to use as a placeholder for the activation link
     * within the {@link #REGISTRATION_TEXT}.
     */
    private static final String ACTIVATION_LINK_PLACEHOLDER =
        "{ACTIVATION_LINK}";

    /**
     * The unique identifier for the user that owns this registration.
     */
    @JsonProperty(JSON_KEY_USER_ID)
    private final String userId;
    /**
     * The email address of the user where the registration link was sent.
     */
    @JsonProperty(JSON_KEY_EMAIL)
    private final String emailAddress;
    /**
     * The unique identifier for the user to use to activate their account.
     */
    @JsonProperty(JSON_KEY_ACTIVATION_ID)
    private final String activationId;
    /**
     * The number of milliseconds since the Unix epoch at which time the user
     * activated their account or null if the account has not yet been
     * activated.
     */
    @JsonProperty(JSON_KEY_ACTIVATION_TIMESTAMP)
    private final Long activationTimestamp;

    /**
     * Creates a new Registration object.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param emailAddress
     *        The user's email address.
     *
     * @throws IllegalArgumentException
     *         The user's unique identifier or email address is null.
     */
    public Registration(final String userId, final String emailAddress)
        throws IllegalArgumentException {

        // Validate the parameters.
        if(userId == null) {
            throw
                new IllegalArgumentException(
                    "The user's unique identifier is null.");
        }
        if(emailAddress == null) {
            throw new IllegalArgumentException("The email address is null.");
        }

        // Store the parameters
        this.userId = userId;
        this.emailAddress = emailAddress;
        activationId = createRegistrationId();
        activationTimestamp = null;
    }

    /**
     * Recreates an existing Registration object.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param emailAddress
     *        The user's email address.
     *
     * @param activationId
     *        The unique identifier for the user to use to activate their
     *        account.
     *
     * @param activationTimestamp
     *        The number of milliseconds since the Unix epoch when the user's
     *        account was activated.
     *
     * @throws IllegalArgumentException
     *         The user's unique identifier or activation ID is null.
     *
     * @throws InvalidArgumentException
     *         The user's email address is null.
     */
    @JsonCreator
    protected Registration(
        @JsonProperty(JSON_KEY_USER_ID) final String userId,
        @JsonProperty(JSON_KEY_EMAIL) final String emailAddress,
        @JsonProperty(JSON_KEY_ACTIVATION_ID) final String activationId,
        @JsonProperty(JSON_KEY_ACTIVATION_TIMESTAMP)
            final Long activationTimestamp)
        throws IllegalArgumentException {

        if(userId == null) {
            throw
                new IllegalArgumentException(
                    "The user's unique identifier is null.");
        }
        if(emailAddress == null) {
            throw new InvalidArgumentException("The email address is null.");
        }

        this.userId = userId;
        this.emailAddress = emailAddress;
        this.activationId =
            (activationId == null) ? createRegistrationId() : activationId;
        this.activationTimestamp = activationTimestamp;
    }

    /**
     * Returns the number of milliseconds since the Unix epoch when the user
     * activated their account or null if the account was never activated.
     *
     * @return The number of milliseconds since the Unix epoch when the user
     *         activated their account or null if the account was never
     *         activated.
     */
    public Long getActivationTimestamp() {
        return activationTimestamp;
    }

    /**
     * Emails the user their registration email including their activation
     * link.
     *
     * @param activationUrl
     *        The URL to the activation API.
     *
     * @throws IllegalStateException
     *         There was a problem building the message.
     */
    public void sendUserRegistrationEmail(final String activationUrl)
        throws IllegalStateException {

        // Create a mail session.
        Session smtpSession = Session.getDefaultInstance(new Properties());

        // Create the message.
        MimeMessage message = new MimeMessage(smtpSession);

        // Add the sender.
        try {
            message.setFrom(REGISTRATION_SENDER);
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "There was an error setting the sender's email address.",
                    e);
        }

        // Add the recipient.
        try {
            message
                .setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(emailAddress));
        }
        catch(AddressException e) {
            throw
                new IllegalStateException(
                    "The user's email address is not a valid email address.",
                    e);
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "There was an error setting the recipient of the message.",
                    e);
        }

        // Set the subject.
        try {
            message.setSubject(REGISTRATION_SUBJECT);
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "There was an error setting the subject on the message.",
                    e);
        }

        // Set the content of the message.
        try {
            message
                .setContent(
                    createRegistrationText(activationUrl),
                    "text/html");
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "There was an error constructing the message.",
                    e);
        }

        // Prepare the message to be sent.
        try {
            message.saveChanges();
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "There was an error saving the changes to the message.",
                    e);
        }

        // Get the transport from the session.
        SMTPTransport transport;
        try {
            transport =
                (SMTPTransport) smtpSession.getTransport(MAIL_PROTOCOL);
        }
        catch(NoSuchProviderException e) {
            throw
                new IllegalStateException(
                    "There is no provider for the transport protocol: " +
                        MAIL_PROTOCOL,
                    e);
        }

        // Connect to the transport.
        try {
            transport.connect();
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "Could not connect to the mail server.",
                    e);
        }

        // Send the message.
        try {
            transport.sendMessage(message, message.getAllRecipients());
        }
        catch(SendFailedException e) {
            throw new IllegalStateException("Failed to send the message.", e);
        }
        catch(MessagingException e) {
            throw
                new IllegalStateException(
                    "There was a problem while sending the message.",
                    e);
        }
        finally {
            // Close the connection to the transport.
            try {
                transport.close();
            }
            catch(MessagingException e) {
                LOGGER
                    .log(
                        Level.WARNING,
                        "After sending the message there was an error " +
                            "closing the connection.",
                            e);
            }
        }
    }

    /**
     * Creates a random activation ID.
     *
     * @return A random value that can be used as a activation ID.
     *
     * @throws IllegalStateException
     *         There was a problem creating the activation ID.
     */
    private String createRegistrationId() throws IllegalStateException {
        // Generate the digest.
        MessageDigest digest;
        try {
                digest = MessageDigest.getInstance(HASH_FUNCTION);
        }
        catch(NoSuchAlgorithmException e) {
                throw new IllegalStateException("The hasing algorithm is unknown: " + HASH_FUNCTION, e);
        }

        // Add the user-specific stuff with some randomness.
        digest.update(userId.getBytes());
        digest.update(emailAddress.getBytes());
        digest
            .update(
                new Long(System.currentTimeMillis()).toString().getBytes());
        digest.update(UUID.randomUUID().toString().getBytes());
        byte[] digestBytes = digest.digest();

        // Build the string.
        StringBuffer buffer = new StringBuffer();
        for(byte digestByte : digestBytes) {
            buffer
                .append(
                    Integer
                        .toString((digestByte & 0xff) + 0x100, 16)
                        .substring(1));
        }

        // Return the random activation ID.
        return buffer.toString();
    }

    /**
     * Creates the registration text.
     *
     * @param activationUrl
     *        The URL to use to activate a user's account.
     *
     * @return The registration text.
     */
    private String createRegistrationText(final String activationUrl) {
        return
            REGISTRATION_TEXT
                .replace(
                    ACTIVATION_LINK_PLACEHOLDER,
                    "<a href=\"" +
                        activationUrl +
                        "/" +
                        activationId +
                    "\">Click here to activate your account.</a>");
    }
}