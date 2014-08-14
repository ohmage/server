package org.ohmage.controller;

import com.sun.mail.smtp.SMTPTransport;
import org.ohmage.bin.MediaBin;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.OhmletBin;
import org.ohmage.bin.OhmletInvitationBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.UserBin;
import org.ohmage.bin.UserInvitationBin;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.ohmlet.Ohmlet;
import org.ohmage.domain.ohmlet.Ohmlet.SchemaReference;
import org.ohmage.domain.ohmlet.OhmletInvitation;
import org.ohmage.domain.ohmlet.OhmletReference;
import org.ohmage.domain.stream.Stream;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.Survey;
import org.ohmage.domain.user.User;
import org.ohmage.domain.user.UserInvitation;
import org.ohmage.javax.servlet.filter.AuthFilter;
import org.ohmage.javax.servlet.listener.ConfigurationFileImport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerMapping;

/**
 * <p>
 * The controller for all requests to ohmlet entities.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(OhmletController.ROOT_MAPPING)
public class OhmletController extends OhmageController {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/ohmlets";

	/**
	 * The path and parameter key for ohmlet IDs.
	 */
	public static final String KEY_OHMLET_ID = "id";
    /**
     * The parameter key for an ohmlet definition.
     */
    public static final String KEY_OHMLET_DEFINITION = "definition";
    /**
     * The parameter for the icon.
     */
    public static final String KEY_ICON = "icon";
	/**
	 * The name of the parameter for querying for specific values.
	 */
	public static final String KEY_QUERY = "query";

	/**
	 * The path to the invitations.
	 */
	public static final String PATH_INVITATIONS = "invitations";

    /**
     * The path to a single invitation.
     */
    public static final String PATH_INVITATION = "invitation";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(OhmletController.class.getName());

    /**
     * The mail protocol to use when sending mail.
     */
    private static final String MAIL_PROTOCOL = "smtp";

    /**
     * The key to use to retrieve the email registration sender.
     */
    private static final String INVITATION_SENDER_KEY =
        "ohmage.user.invitation.sender";
    /**
     * The email address to use to build the email to the self-registering
     * user.
     */
    private static final InternetAddress INVITATION_SENDER;
    static {
        try {
            INVITATION_SENDER =
                new InternetAddress(
                    ConfigurationFileImport
                        .getCustomProperties()
                        .getProperty(INVITATION_SENDER_KEY));
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
    private static final String INVITATION_SUBJECT_KEY =
        "ohmage.user.invitation.subject";
    /**
     * The registration subject to use to build the email to the
     * self-registering user.
     */
    private static final String INVITATION_SUBJECT =
        ConfigurationFileImport
            .getCustomProperties()
            .getProperty(INVITATION_SUBJECT_KEY);
    /**
     * The key to use to retrieve the email registration text.
     */
    private static final String INVITATION_TEXT_KEY =
        "ohmage.user.invitation.text";
    /**
     * The registration text to use to build the email to the self-registering
     * user.
     */
    private static final String INVITATION_TEXT =
        ConfigurationFileImport
            .getCustomProperties()
            .getProperty(INVITATION_TEXT_KEY);
    /**
     * The specialized text to use as a placeholder for the activation link
     * within the {@link #INVITATION_TEXT}.
     */
    private static final String INVITATION_LINK_PLACEHOLDER =
        "{INVITATION_LINK}";

    /**
     * The encoding to use URL-encoding parameters.
     */
    private static final String URL_ENCODING = "UTF-8";

	/**
	 * The usage in this class is entirely static, so there is no need to
	 * instantiate it.
	 */
	private OhmletController() {
		// Do nothing.
	}

    /**
     * Creates a new ohmlet.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param ohmletBuilder
     *        The parts of the ohmlet that are already set.
     */
    @RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
    public static @ResponseBody Ohmlet createOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestBody final Ohmlet.Builder ohmletBuilder) {

        return createOhmlet(authToken, ohmletBuilder, null);
    }

    /**
     * Creates a new ohmlet.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param ohmletBuilder
     *        The parts of the ohmlet that are already set.
     *
     * @param iconFile
     *        The file that represents the icon, which must be present if an
     *        icon is defined in the 'stream builder'.
     */
    @RequestMapping(
        value = { "", "/" },
        method = RequestMethod.POST,
        consumes = "multipart/*")
    public static @ResponseBody Ohmlet createOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestPart(value = KEY_OHMLET_DEFINITION, required = true)
            final Ohmlet.Builder ohmletBuilder,
        @RequestPart(value = KEY_ICON, required = false)
            final MultipartFile iconFile) {

		LOGGER.info("Creating a ohmlet creation request.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

		LOGGER.debug("Setting the token's owner as the creator of this ohmlet.");
		ohmletBuilder.addMember(user.getId(), Ohmlet.Role.OWNER);

        LOGGER.debug("Checking if an icon was given.");
        Media icon = null;
        // If given, verify that it was attached as well.
        if(ohmletBuilder.getIconId() != null) {
            if(iconFile
                .getOriginalFilename()
                .equals(ohmletBuilder.getIconId())) {

                String newIconId = Media.generateUuid();
                ohmletBuilder.setIconId(newIconId);
                icon = new Media(newIconId, iconFile);
            }
            else {
                throw
                    new InvalidArgumentException(
                        "An icon file was referenced but not uploaded.");
            }
        }

		LOGGER.debug("Building the ohmlet.");
		Ohmlet ohmlet = ohmletBuilder.build();

		// Validate that the streams exist.
		LOGGER.info("Validating that the given streams exist.");
		List<SchemaReference> streams = ohmlet.getStreams();
		for(SchemaReference stream : streams) {
			// Get the schema ID.
			String id = stream.getSchemaId();
			// Get the schema version.
			Long version = stream.getVersion();

			LOGGER.info("Checking if the stream is a known stream.");
			if(! StreamBin.getInstance().exists(id, version, false)) {
				throw
					new InvalidArgumentException(
						"No such stream '" +
							id +
							"'" +
							((version == null) ?
								"" :
								" with version '" + version + "'") +
							".");
			}
		}

		// Validate that the surveys exist.
        LOGGER.info("Validating that the given surveys exist.");
        List<SchemaReference> surveys = ohmlet.getSurveys();
        for(SchemaReference survey : surveys) {
            // Get the schema ID.
            String id = survey.getSchemaId();
            // Get the schema version.
            Long version = survey.getVersion();

            LOGGER.info("Checking if the survey is a known survey.");
            if(! SurveyBin.getInstance().exists(id, version, false)) {
                throw
                    new InvalidArgumentException(
                        "No such survey '" +
                            id +
                            "'" +
                            ((version == null) ?
                                "" :
                                " with version '" + version + "'") +
                            ".");
            }
        }

		LOGGER.info("Updating the user.");
		User.Builder updatedUserBuilder = new User.Builder(user);

		LOGGER.debug("Building the ohmlet reference.");
		OhmletReference ohmletReference = new OhmletReference(ohmlet.getId(), ohmlet.getName());

		LOGGER.debug("Adding the ohmlet reference to the user.");
		updatedUserBuilder.addOhmlet(ohmletReference);

		LOGGER.debug("Building the user.");
		User updatedUser = updatedUserBuilder.build();

        // Attempt to store the updated user first because if it fails, the subsequent inserts should not occur.
        // The most likely cause of a user update failing is if some other process updated the user as this
        // particular request was being handled.
        LOGGER.info("Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);

        if(icon != null) {
            LOGGER.info("Storing the icon.");
            MediaBin.getInstance().addMedia(icon);
        }

        LOGGER.info("Adding the ohmlet to the database.");
        OhmletBin.getInstance().addOhmlet(ohmlet);

        return ohmlet;
	}

	/**
	 * Returns a list of visible ohmlets.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call or null if the call is being made anonymously.
	 *
	 * @param query
	 *        A value that should appear in either the name or description.
     *
     * @param numToSkip
     *        The number of ohmlets to skip.
     *
     * @param numToReturn
     *        The number of ohmlets to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
	 *
	 * @return A list of visible ohmlets.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<Ohmlet>> getOhmlets(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
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
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER.info("Creating a ohmlet ID read request.");

		LOGGER.debug("Determining the user making the request.");
		String userId = null;

		if(authToken == null) {
		    LOGGER.info("The request is being made anonymously.");
		}
		else {
            LOGGER.info("Validating the user from the token.");
            userId =
                OhmageController.validateAuthorization(authToken, null).getId();
		}

        LOGGER.info("Retrieving the ohmlets.");
        MultiValueResult<Ohmlet> ohmlets =
            OhmletBin
                .getInstance()
                .getOhmlets(userId, query, numToSkip, numToReturn);

        LOGGER.info("Found " + ohmlets.size() + " ohmlets.");

        // Find all of the unique schema ID-version pairs in order to avoid retrieving the same survey or stream
        // multiple times (i.e., the Mongo n + 1 selects problem -- each ohmlet can have p surveys and q streams where
        // p + q = n. The likelihood that there will be a large (> 5) number of surveys or streams in an ohmlet is
        // small as is the likelihood that the number of ohmlets will number over 500. The maximum number of ohmlets
        // returned by this call is 100, so the worst case would be 700 find() calls if every survey and stream
        // definition was unique. Using 2.16 campaign and survey counts from test.ohmage.org, that VM has 97 active
        // campaigns with an average of 2 surveys per campaign. It is impossible to determine the number of stream
        // definitions per campaign because the relationship did not exist, but most 2.16 pilots only used one stream
        // definition (mobility). That leaves the calculation as (97 * 2) + 1 = 195 selects. Per 100 ohmlets then, the
        // limits are 195 < x < 700 selects per invocation of this method. In practice (e.g., 2.16 pilots.ohmage.org),
        // the number of ohmlets is far smaller, say 20 max, which puts the upper bound at 41 find calls per invocation
        // of this method. It is also important to note that it took 2 years for the 2.16 test.ohmage.org to grow into
        // the size it did. The same applies to pilots.ohmage.org. For 3.0 there are also other factors at play:
        // this call will only return the ohmlets that are visible to the logged-in user. For production environments,
        // most ohmlets will be private and invite-only whereas for testing everything is public.

        Set<SchemaReference> surveySet = new HashSet<SchemaReference>();
        Set<SchemaReference> streamSet = new HashSet<SchemaReference>();

        for(Ohmlet ohmlet : ohmlets) {
            List<SchemaReference> surveyReferences = ohmlet.getSurveys();
            List<SchemaReference> streamReferences = ohmlet.getStreams();

            LOGGER.info("Found " + (surveyReferences.size() + streamReferences.size()) + " surveys and streams for ohmlet ID  " + ohmlet.getId());

            for(SchemaReference surveyReference : surveyReferences) {
                surveySet.add(surveyReference);
            }

            for(SchemaReference streamReference : streamReferences) {
                streamSet.add(streamReference);
            }
        }

        List<Survey> surveys = new ArrayList<Survey>();
        List<Stream> streams = new ArrayList<Stream>();

        LOGGER.info("Retrieving " + surveySet.size() + " survey definitions for the ohmlets.");

        for(SchemaReference surveyReference : surveySet) {
            // Make sure to grab the latest version if the version is null
            if(surveyReference.getVersion() == null) {
                surveys.add(SurveyBin.getInstance().getLatestSurvey(surveyReference.getSchemaId(), false));
            } else {
                surveys.add(SurveyBin.getInstance()
                    .getSurvey(surveyReference.getSchemaId(), surveyReference.getVersion(), false));
            }
        }

        LOGGER.info("Retrieving " + streamSet.size() + "  stream definitions for the ohmlets.");

        for(SchemaReference streamReference : streamSet) {
            // Make sure to grab the latest version if the version is null
            if(streamReference.getVersion() == null) {
                streams.add(StreamBin.getInstance().getLatestStream(streamReference.getSchemaId(), false));
            } else {
                streams.add(StreamBin.getInstance()
                        .getStream(streamReference.getSchemaId(), streamReference.getVersion(), false));
            }
        }

        LOGGER.info("After unique-ifying Found " + (surveys.size() + streams.size()) + " surveys and streams.");

        LOGGER.info("Building the paging headers.");
        HttpHeaders headers =
            OhmageController
                .buildPagingHeaders(
                        numToSkip,
                        numToReturn,
                        Collections.<String, String>emptyMap(),
                        ohmlets,
                        rootUrl + ROOT_MAPPING);

        LOGGER.info("Creating the response object.");
        ResponseEntity<MultiValueResult<Ohmlet>> result =
            new ResponseEntity<MultiValueResult<Ohmlet>>(
                ohmlets,
                headers,
                HttpStatus.OK);

        LOGGER.info("Returning the stream IDs.");
        return result;
	}

	/**
	 * Returns a ohmlet's definition.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @param ohmletId
	 *        The ohmlet's unique identifier.
	 *
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_OHMLET_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Ohmlet getOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_OHMLET_ID) final String ohmletId) {

		LOGGER.info("Creating a request to read a ohmlet: " + ohmletId);

        LOGGER.info("Retrieving the ohmlet.");
        Ohmlet ohmlet = OhmletBin.getInstance().getOhmlet(ohmletId);

        LOGGER.info("Ensuring the ohmlet exists.");
        if(ohmlet == null) {
            throw
                new UnknownEntityException(
                    "The " + Ohmlet.OHMLET_SKIN + " is unknown.");
        }

        LOGGER.info("Verifying that the requesting user is allowed to query the " +
                    "ohmlet.");
        LOGGER.debug("Checking if the ohmlet is private.");
        if(Ohmlet
            .PrivacyState
            .PRIVATE
            .equals(ohmlet.getPrivacyState())) {

            LOGGER.info("The ohmlet is private. Checking credentials.");

            LOGGER.info("Verifying that auth information was given.");
            if(authToken == null) {
                throw
                    new AuthenticationException(
                        "The " +
                            Ohmlet.OHMLET_SKIN +
                            " is private and no auth information was " +
                            "given.");
            }

            LOGGER.info("Verifying the user may view the ohmlet.");
            if(! ohmlet.canViewOhmlet(authToken.getUserId())) {
                throw
                    new InsufficientPermissionsException(
                        "The user does not have sufficient permissions to " +
                            "view this " + Ohmlet.OHMLET_SKIN + ".");
            }
        }

		return ohmlet;
	}

	/**
	 * Updates this ohmlet.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @param ohmletId
	 *        The ohmlet's unique identifier.
	 *
	 * @param ohmletBuilder
	 *        The parts of the ohmlet that are already set.
	 */
	@RequestMapping(
		value = "{" + KEY_OHMLET_ID + "}",
		method = RequestMethod.POST)
	public static @ResponseBody void updateOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_OHMLET_ID) final String ohmletId,
		@RequestBody
			final Ohmlet.Builder ohmletBuilder) {

		LOGGER.info("Creating a request to update a ohmlet: " + ohmletId);

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

		LOGGER.info("Retrieving the ohmlet.");
		Ohmlet ohmlet =
			OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER.info("Verifying that the ohmlet exists.");
		if(ohmlet == null) {
			throw
				new UnknownEntityException(
					"The " + Ohmlet.OHMLET_SKIN + " is unknown.");
		}

		LOGGER.info("Verifying that the requesting user is allowed to modify " +
					"the ohmlet.");
		if(! ohmlet.canModifyOhmlet(user.getId())) {
			throw
				new InsufficientPermissionsException(
					"The user does not have sufficient permissions to " +
						"update the " + Ohmlet.OHMLET_SKIN + ".");
		}

		LOGGER.debug("Creating a new builder based on the existing ohmlet.");
		Ohmlet.Builder newOhmletBuilder =
			new Ohmlet.Builder(ohmlet);

		LOGGER.debug("Merging the changes into the old ohmlet.");
		newOhmletBuilder.merge(ohmletBuilder);

		LOGGER.debug("Building a new ohmlet.");
		Ohmlet newOhmlet = newOhmletBuilder.build();

        // Validate that the streams exist.
        LOGGER.info("Validating that the given streams exist.");
        List<SchemaReference> streams = newOhmlet.getStreams();
        for(SchemaReference stream : streams) {
            // Get the schema ID.
            String id = stream.getSchemaId();
            // Get the schema version.
            Long version = stream.getVersion();

            LOGGER.info("Checking if the stream is a known stream.");
            if(! StreamBin.getInstance().exists(id, version, false)) {
                throw
                    new InvalidArgumentException(
                        "No such stream '" +
                            id +
                            "'" +
                            ((version == null) ?
                                "" :
                                " with version '" + version + "'") +
                            ".");
            }
        }

        // Validate that the surveys exist.
        LOGGER.info("Validating that the given surveys exist.");
        List<SchemaReference> surveys = newOhmlet.getSurveys();
        for(SchemaReference survey : surveys) {
            // Get the schema ID.
            String id = survey.getSchemaId();
            // Get the schema version.
            Long version = survey.getVersion();

            LOGGER.info("Checking if the survey is a known survey.");
            if(! SurveyBin.getInstance().exists(id, version, false)) {
                throw
                    new InvalidArgumentException(
                        "No such survey '" +
                            id +
                            "'" +
                            ((version == null) ?
                                "" :
                                " with version '" + version + "'") +
                            ".");
            }
        }

		LOGGER.info("Storing the updated ohmlet.");
		OhmletBin.getInstance().updateOhmlet(newOhmlet);
	}

    /**
     * Invites a set of users to ohmage and to a specific ohmlet. If a user
     * exists with the given email address, they are immediately set to the
     * INVITED role if they are not already at INVITED+. If no such user exists
     * then an email is sent to the email address inviting them to use the
     * system.
     *
     * @param authToken
     *        The inviting user's authentication token.
     *
     * @param rootUrl
     *        The root URL for the incoming request used to build the
     *        invitation URL in the email.
     *
     * @param ohmletId
     *        The specific ohmlet's unique identifier.
     *
     * @param emails
     *        The set of emails that should be used to invite users.
     */
    @RequestMapping(
        value =
            "{" + KEY_OHMLET_ID + "}" +
            "/" + Ohmlet.JSON_KEY_MEMBERS +
            "/" + PATH_INVITATIONS,
        method = RequestMethod.POST)
    public static @ResponseBody void inviteUsers(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @ModelAttribute(ATTRIBUTE_REQUEST_URL_ROOT) final String rootUrl,
        @PathVariable(KEY_OHMLET_ID) final String ohmletId,
        @RequestBody final List<String> emails) {

        LOGGER.info("Creating a request to invite users to an ohmlet: " +
                    ohmletId);

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.info("Retrieving the ohmlet.");
        Ohmlet ohmlet =
            OhmletBin.getInstance().getOhmlet(ohmletId);

        LOGGER.info("Verifying that the ohmlet exists.");
        if(ohmlet == null) {
            throw new UnknownEntityException("The ohmlet is unknown.");
        }

        LOGGER.info("Verifying that the user is allowed to invite other users.");
        if(ohmlet.getInviteRole().supersedes(ohmlet.getRole(user.getId()))) {
            throw
                new InsufficientPermissionsException(
                    "The user does not have sufficient permissions to " +
                        "invite other users.");
        }

        // Build the URL.
        
        // Note that this URL is currently used only by the Android app.
        // The user must have ohmage installed on their device and must click
        // the invitation link in an email client running on the device. This 
        // will cause ohmage to launch and perform the API call to /ohmlets/<ohmlet_id>/people
        // with the invitation code. In the future, this would be a great feature 
        // to have on the server, so users don't have to always join from their phones.
        
        // Start with the root URL to our web app.
        StringBuilder baseInvitationUrlBuilder =
            new StringBuilder(rootUrl);
        // Add the ohmlet endpoint.
        baseInvitationUrlBuilder.append(ROOT_MAPPING);
        // Add the ohmlet's ID.
        baseInvitationUrlBuilder.append('/').append(ohmletId);
        // Add the custom endpoint for accepting an invitation.
        baseInvitationUrlBuilder.append("/invitation");
        
        // Each email address is treated independently.
        for(String email : emails) {
            // Create the map of parameters.
            Map<String, String> parameters = new HashMap<String, String>();

            // Check if an account already exists with the given email address.
            User currUser = UserBin.getInstance().getUserFromEmail(email);

            // If the user doesn't exist, add all of the invitations.
            if(currUser == null) {
                // Create the ohmlet invitation.
                OhmletInvitation ohmletInvitation =
                    new OhmletInvitation(ohmletId);

                // Create the user invitation.
                UserInvitation userInvitation =
                    new UserInvitation(email, ohmletInvitation.getId());

                // Store the invitations in the database.
                OhmletInvitationBin
                    .getInstance()
                    .addInvitation(ohmletInvitation);
                UserInvitationBin.getInstance().addInvitation(userInvitation);

                // Add the email address.
                parameters.put(User.JSON_KEY_EMAIL, email);

                // Add the user invitation code.
                parameters
                    .put(
                        UserInvitation.JSON_KEY_INVITATION_ID,
                        userInvitation.getId());

                // Add the ohmlet invitation code.
                parameters
                    .put(
                        OhmletInvitation.JSON_KEY_INVITATION_ID,
                        ohmletInvitation.getId());
            }
            // If the user does exist,
            else {
                // Check if the user has already been invited.
                OhmletInvitation ohmletInvitation =
                    currUser.getOhmletInvitationFromOhmletId(ohmletId);

                // If the user does not already have an invitation to the
                // ohmlet, create one.
                if(ohmletInvitation == null) {
                    // Create the ohmlet invitation.
                    ohmletInvitation = new OhmletInvitation(ohmletId);

                    // Add the invitation to the user's object.
                    User updatedUser =
                        (new User.Builder(currUser))
                            .addOhlmetInvitation(ohmletInvitation)
                            .build();

                    // Store the updated user.
                    UserBin.getInstance().updateUser(updatedUser);

                    // Add the user's invitation ID.
                    parameters
                        .put(
                            OhmletInvitation.JSON_KEY_INVITATION_ID,
                            ohmletInvitation.getId());
                }
            }

            // If any parameters were added, send the invitation email.
            if(parameters.size() > 0) {
                // Create a specific redirect URL for this user.
                StringBuilder invitationUrlBuilder =
                    new StringBuilder(baseInvitationUrlBuilder);

                // Add the parameters to the invitation URL.
                boolean firstPass = true;
                try {
                    for(String key : parameters.keySet()) {
                        if(firstPass) {
                            invitationUrlBuilder.append('?');
                            firstPass = false;
                        }
                        else {
                            invitationUrlBuilder.append('&');
                        }

                        invitationUrlBuilder
                            .append(URLEncoder.encode(key, URL_ENCODING))
                            .append('=')
                            .append(
                                URLEncoder
                                    .encode(
                                        parameters.get(key),
                                        URL_ENCODING));
                    }
                }
                catch(UnsupportedEncodingException e) {
                    throw
                        new IllegalStateException(
                            "The encoding is unknown: " + URL_ENCODING,
                            e);
                }

                // Send an invitation email.
                sendUserInvitationEmail(
                    email,
                    invitationUrlBuilder.toString());
            }
        }
    }

    /**
     * Allow a user to respond to invitation to join an ohmlet. Currently this
     * method is specific to iOS because the Android app intercepts this link
     * and formulates a call to OhmletController.updateRole(). Eventually this
     * method can also be used to formulate a redirect to the front end so
     * users can join via their web browsers.
     *
     * @param rootUrl
     *        The root URL for the incoming request.
     *
     * @param userAgent
     *        The user-agent performing the request.
     *
     * @param ohmletId
     *        The specific ohmlet's unique identifier.
     *
     * @param httpRequest
     *        Included so the request URL and query string can be retrieved.
     */
    @RequestMapping(
        value =
            "{" + KEY_OHMLET_ID + "}" +
            "/" + PATH_INVITATION,
        method = RequestMethod.GET)
    public String acceptInvitation(
            @ModelAttribute(ATTRIBUTE_REQUEST_URL_ROOT) final String rootUrl,
            @RequestHeader("User-Agent") final String userAgent,
            @PathVariable(KEY_OHMLET_ID) final String ohmletId,
            HttpServletRequest httpRequest) {

        LOGGER.info("A user is accepting an invite to join an ohmlet: " + ohmletId);

        if(userAgent != null) {
            if(userAgent.contains("iPhone")
                || userAgent.contains("iPod")
                || userAgent.contains("iPad")) {

                int schemeLength = 4; // http
                if(httpRequest.isSecure()) {
                    schemeLength = 5;
                }

                // Using the ohmage  scheme, rebuild the URL for the redirect.
                // The 'redirect:' syntax is magic for Spring to interpret into
                // the actual HTTP redirect
                StringBuilder redirectUrl = new StringBuilder("redirect:ohmage");
                redirectUrl.append(
                    httpRequest.getRequestURL().substring(schemeLength)
                    .toString());
                redirectUrl.append("?");
                redirectUrl.append(httpRequest.getQueryString());

                return redirectUrl.toString();
            }
        }

        LOGGER.info("Throwing an exception because an ohmlet join invitation " +
            "redirect is only supported for iOS. The user-agent is " + userAgent);

        throw new InvalidArgumentException("User-Agent not supported.");
    }

    /**
     * Allows a user to modify their or another user's privileges. This can be
     * used by users with the {@link Ohmlet.Role#INVITED} role to elevate their
     * own role to {@link Ohmlet.Role#MEMBER} or by users with sufficient
     * privileges to escalate or de-escalate another user's role.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
     * @param ohmletInvitationId
     *        The unique identifier for an invitation to this ohmlet.
     *
     * @param member
     *        The information about the user that is being changed.
     */
	@RequestMapping(
		value =
			"{" + KEY_OHMLET_ID + "}" +
			"/" + Ohmlet.JSON_KEY_MEMBERS,
		method = RequestMethod.POST)
	public static @ResponseBody void updateRole(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_OHMLET_ID) final String ohmletId,
        @RequestParam(
            value = OhmletInvitation.JSON_KEY_INVITATION_ID,
            required = false)
            final String ohmletInvitationId,
		@RequestBody final Ohmlet.Member member) {

		LOGGER.info("Creating a request to modify a user's privileges in a " +
					"ohmlet: " +
					ohmletId);

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

		LOGGER.info("Retrieving the ohmlet.");
		Ohmlet ohmlet = OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER.info("Verifying that the ohmlet exists.");
		if(ohmlet == null) {
			throw
				new UnknownEntityException(
					"The " + Ohmlet.OHMLET_SKIN + " is unknown.");
		}

		OhmletInvitation invitation = null;
		if(ohmletInvitationId != null) {
		    LOGGER.info("Validating the ohmlet invitation ID.");
		    invitation = user.getOhmletInvitation(ohmletInvitationId);

            LOGGER.info("Verifying that the invitation exists.");
            if(invitation == null) {
                throw
                    new InvalidArgumentException("The invitation is unknown.");
            }

            LOGGER.info("Verifying that the invitation belongs to this ohmlet.");
            if(! invitation.getOhmletId().equals(ohmletId)) {
                throw
                    new InvalidArgumentException(
                        "The invitation belongs to a different email " +
                            "address.");
            }

            LOGGER.info("Verifying that the invitation is still valid.");
            if(! invitation.isValid()) {
                throw
                    new InvalidArgumentException(
                        "The invitation is no longer valid.");
            }
		}

		LOGGER.debug("Retrieving the requesting user's role.");
		Ohmlet.Role requesterRole = ohmlet.getRole(user.getId());

		LOGGER.info("Validating the request.");
		if(user.getId().equals(member.getMemberId())) {
			LOGGER.debug("The user is attempting to update their own role.");
			switch(member.getRole()) {
			case REQUESTED:
				if(requesterRole != null) {
					throw
						new InvalidArgumentException(
							"The user is already associated with the " +
								"ohmlet.");
				}
				else if(
					Ohmlet
						.PrivacyState
						.PRIVATE
						.equals(ohmlet.getPrivacyState())) {

					throw
						new InvalidArgumentException(
							"The " +
								Ohmlet.OHMLET_SKIN +
								" is private, therefore the user may not " +
								"request an invite.");
				}
				break;

			case INVITED:
				throw
					new InvalidArgumentException(
						"A user cannot invite themselves.");

			case MEMBER:
			    // If the user doesn't have a role,
			        // The ohmlet must be public.
			    // If the user is REQUESTED,
			        // The ohmlet must be public.
			    // If the user is INVITED, then we are good.
			    // If the user is already a MEMBER, then we are good.
			    // If the user is anything else, then they are down-grading
			    // their role, and we are good.
			    // If the user gave a valid ohmlet invitation, then we are
			    // good.
				if(
					(
					    (requesterRole == null) ||
					    (Ohmlet.Role.REQUESTED.equals(requesterRole))
				    ) &&
					(!
						Ohmlet
							.PrivacyState
							.PUBLIC
							.equals(ohmlet.getPrivacyState())
					) &&
					(
					    invitation == null)) {

					throw
						new InvalidArgumentException(
							"A user may not directly join a non-public " +
								Ohmlet.OHMLET_SKIN + ".");
				}
				break;

			default:
			    // The user may only decrease their role, at this point.
				if(member.getRole().supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"A user may not elevate their own role beyond " +
								"its current value.");
				}
				break;
			}
		}
		else {
			LOGGER.debug("A user is attempting to modify another user's role.");

			LOGGER.info("Verifying that the other user exists.");
			if(UserBin.getInstance().getUser(member.getMemberId()) == null) {
				throw
					new InvalidArgumentException(
						"The user does not exist: " +
							member.getMemberId());
			}

			LOGGER.debug("Retreving the requestee's role in the ohmlet.");
			Ohmlet.Role requesteeRole =
				ohmlet.getRole(member.getMemberId());

			switch(member.getRole()) {
			case REQUESTED:
				throw
					new InvalidArgumentException(
						"A user is not allowed to make invitation requests " +
							"for another user.");

			case INVITED:
				if(ohmlet.getInviteRole().supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"The user does not have sufficient permissions " +
								"to invite other users.");
				}
				if(
					(requesteeRole != null) &&
					(! Ohmlet.Role.REQUESTED.equals(requesteeRole)) &&
					(! Ohmlet.Role.INVITED.equals(requesteeRole))) {

					throw
						new InvalidArgumentException(
							"The user is already associated with the " +
								Ohmlet.OHMLET_SKIN + ".");
				}
				break;

			default:
				if(member.getRole().supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"A user may not elevate another user's role " +
								"beyond their own role.");
				}
				if(
				    (requesteeRole != null) &&
				    requesteeRole.supersedes(requesterRole)) {

					throw
						new InsufficientPermissionsException(
							"The user may not change the role of a user " +
								"with a higher role.");
				}
			}
		}

		LOGGER.info("Updating the ohmlet to reflect the role change.");
		Ohmlet updatedOhmlet =
			(new Ohmlet.Builder(ohmlet))
				.addMember(member.getMemberId(), member.getRole())
				.build();

		LOGGER.info("Saving the updated ohmlet.");
		OhmletBin.getInstance().updateOhmlet(updatedOhmlet);

		User.Builder userBuilder = null;
		if(invitation != null) {
            LOGGER.info("Invalidating the invitation.");
            OhmletInvitation expiredInvitation =
                (new OhmletInvitation.Builder(invitation))
                    .setUsedTimestamp(System.currentTimeMillis())
                    .build();

            LOGGER.info("Updating the user with the expired invitation.");
            userBuilder = new User.Builder(user);
            userBuilder.addOhlmetInvitation(expiredInvitation);
        }

		LOGGER.debug("Checking if the user's account already tracks this ohmlet.");
		if(user.getOhmlet(ohmletId) == null) {
		    LOGGER.info("Updating the user to be part of the new ohmlet.");
		    if(userBuilder == null) {
		        userBuilder = new User.Builder(user);
		    }
		    userBuilder.addOhmlet(new OhmletReference(ohmletId, null));
		}

		if(userBuilder != null) {
            LOGGER.info("Storing the updated user.");
            UserBin.getInstance().updateUser(userBuilder.build());
		}
	}

	/**
	 * Removes a user from an ohmlet.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
     *
	 * @param userId The unique identifier of the user to be removed from the
	 *
	 */
    @RequestMapping(
        value =
            "{" + KEY_OHMLET_ID + "}" +
            "/" + Ohmlet.JSON_KEY_MEMBERS +
            "/{" + User.JSON_KEY_ID + "}",
        method = RequestMethod.DELETE)
    public static @ResponseBody void removeRole(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_OHMLET_ID) final String ohmletId,
        @PathVariable(User.JSON_KEY_ID) final String userId) {

        LOGGER.info("Creating a request to remove a user from an ohmlet: " +
                    ohmletId);

        LOGGER.info("Validating the user from the token");
        User requester = OhmageController.validateAuthorization(authToken, null);

        LOGGER.debug("Setting the requestee.");
        User requestee = requester;
        if(! requester.getId().equals(userId)) {
            LOGGER.info("Retrieving the requestee.");
            requestee = UserBin.getInstance().getUser(userId);
        }

        LOGGER.info("Retrieving the ohmlet.");
        Ohmlet ohmlet = OhmletBin.getInstance().getOhmlet(ohmletId);

        LOGGER.info("Verifying that the ohmlet exists.");
        if(ohmlet == null) {
            throw
                new UnknownEntityException(
                    "The " + Ohmlet.OHMLET_SKIN + " is unknown.");
        }

        LOGGER.debug("Retrieving the requesting user's role.");
        Ohmlet.Role requesterRole = ohmlet.getRole(requester.getId());

        LOGGER.debug("Checking if the user is removing someone else or " +
                    "themselves from the ohmlet.");
        if(requester != requestee) {
            LOGGER.info("The user is attempting to remove a different user from " +
                        "the ohmlet.");

            LOGGER.debug("Retrieving the requestee user's role.");
            Ohmlet.Role requesteeRole = ohmlet.getRole(userId);

            LOGGER.info("Verifying that the user is allowed to remove the other " +
                        "user.");
            if(ohmlet.getInviteRole().supersedes(requesterRole)) {
                throw
                    new InsufficientPermissionsException(
                        "The user does not have sufficient permissions to " +
                            "remove other users.");
            }
            if(requesteeRole.supersedes(requesterRole)) {
                throw
                    new InsufficientPermissionsException(
                        "The user may not change the role of a user " +
                            "with a higher role.");
            }
        }

        LOGGER.info("Updating the ohmlet to reflect the role change.");
        Ohmlet updatedOhmlet =
            (new Ohmlet.Builder(ohmlet)).removeMember(userId).build();

        LOGGER.info("Saving the updated ohmlet.");
        OhmletBin.getInstance().updateOhmlet(updatedOhmlet);

        LOGGER.debug("Removing the reference to the ohmlet from the user's " +
                    "account.");
        User updatedRequestee = requestee.leaveOhmlet(ohmletId);
        UserBin.getInstance().updateUser(updatedRequestee);
    }

	/**
	 * Deletes the ohmlet. This may only be done by supervisors.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @param ohmletId
	 *        The ohmlet's unique identifier.
	 */
	@RequestMapping(
		value = "{" + KEY_OHMLET_ID + "}",
		method = RequestMethod.DELETE)
	public static @ResponseBody void deleteOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_OHMLET_ID) final String ohmletId) {

		LOGGER.info("Creating a request to delete a ohmlet: " + ohmletId);

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

		LOGGER.info("Retrieving the ohmlet.");
		Ohmlet ohmlet =
			OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER.info("Verifying that the requesting user can delete the " +
					"ohmlet.");
		if(! ohmlet.hasRole(user.getId(), Ohmlet.Role.OWNER)) {
			throw
				new InsufficientPermissionsException(
					"The user does not have enough permissions to delete " +
						"the " +
						Ohmlet.OHMLET_SKIN + ".");
		}

		LOGGER.info("Deleting the ohmlet.");
		OhmletBin.getInstance().deleteOhmlet(ohmletId);
	}

    /**
     * Sends the user their invitation email including their invitation link.
     *
     * @param invitationUrl
     *        The invitation URL that will be intercepted by the phone
     *        application.
     *
     * @throws IllegalStateException
     *         There was a problem building the message.
     */
    public static void sendUserInvitationEmail(
        final String emailAddress,
        final String invitationUrl)
        throws IllegalStateException {

        // Create a mail session.
        Session smtpSession = Session.getDefaultInstance(new Properties());

        // Create the message.
        MimeMessage message = new MimeMessage(smtpSession);

        // Add the sender.
        try {
            message.setFrom(INVITATION_SENDER);
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
            message.setSubject(INVITATION_SUBJECT);
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
                    createRegistrationText(invitationUrl),
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
                LOGGER.warn("After sending the message there was an error " +
                            "closing the connection.",
                            e);
            }
        }
    }

    /**
     * Creates the registration text.
     *
     * @param invitationUrl
     *        The URL to use to activate a user's account.
     *
     * @return The registration text.
     */
    private static String createRegistrationText(final String invitationUrl) {
        return
            INVITATION_TEXT
                .replace(
                    INVITATION_LINK_PLACEHOLDER,
                    "<a href=\"" +
                        invitationUrl +
                        "\">Click here to join the " +
                        Ohmlet.OHMLET_SKIN +
                        ".</a>");
    }
}
