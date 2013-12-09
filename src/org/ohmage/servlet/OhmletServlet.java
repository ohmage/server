package org.ohmage.servlet;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.OhmletBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.Ohmlet;
import org.ohmage.domain.Ohmlet.SchemaReference;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all requests to ohmlet entities.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(OhmletServlet.ROOT_MAPPING)
public class OhmletServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/ohmlets";

	/**
	 * The path and parameter key for ohmlet IDs.
	 */
	public static final String KEY_COMMUNITY_ID = "id";
	/**
	 * The name of the parameter for querying for specific values.
	 */
	public static final String KEY_QUERY = "query";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(OhmletServlet.class.getName());

	/**
	 * The usage in this class is entirely static, so there is no need to
	 * instantiate it.
	 */
	private OhmletServlet() {
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
		@RequestBody
			final Ohmlet.Builder ohmletBuilder) {

		LOGGER.log(Level.INFO, "Creating a ohmlet creation request.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		LOGGER
			.log(
				Level.FINE,
				"Setting the token's owner as the creator of this ohmlet.");
		ohmletBuilder.addMember(user.getUsername(), Ohmlet.Role.OWNER);

		LOGGER.log(Level.FINE, "Building the ohmlet.");
		Ohmlet ohmlet = ohmletBuilder.build();

		// Validate that the streams exist.
		LOGGER.log(Level.INFO, "Validating that the given streams exist.");
		List<SchemaReference> streams = ohmlet.getStreams();
		for(SchemaReference stream : streams) {
			// Get the schema ID.
			String id = stream.getSchemaId();
			// Get the schema version.
			Long version = stream.getVersion();

			LOGGER
				.log(Level.INFO, "Checking if the stream is a known stream.");
			if(! StreamBin.getInstance().exists(id, version)) {
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
        LOGGER.log(Level.INFO, "Validating that the given surveys exist.");
        List<SchemaReference> surveys = ohmlet.getSurveys();
        for(SchemaReference survey : surveys) {
            // Get the schema ID.
            String id = survey.getSchemaId();
            // Get the schema version.
            Long version = survey.getVersion();

            LOGGER
                .log(Level.INFO, "Checking if the survey is a known survey.");
            if(! SurveyBin.getInstance().exists(id, version)) {
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

		LOGGER.log(Level.INFO, "Adding the ohmlet to the database.");
		OhmletBin.getInstance().addOhmlet(ohmlet);

		LOGGER.log(Level.INFO, "Updating the user.");
		User.Builder updatedUserBuilder = new User.Builder(user);
		LOGGER.log(Level.FINE, "Building the ohmlet reference.");
		User.OhmletReference ohmletReference =
			new User.OhmletReference(ohmlet.getId(), null, null);
		LOGGER.log(Level.FINE, "Adding the ohmlet reference to the user.");
		updatedUserBuilder.addOhmlet(ohmletReference);
		LOGGER.log(Level.FINE, "Building the user.");
		User updatedUser = updatedUserBuilder.build();

		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);

		return ohmlet;
	}

	/**
	 * Returns a list of visible ohmlet IDs.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @param query
	 *        A value that should appear in either the name or description.
	 *
	 * @return A list of visible ohmlet IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getOhmletIds(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {

		LOGGER.log(Level.INFO, "Creating a ohmlet ID read request.");

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		return
			OhmletBin
				.getInstance()
				.getOhmletIds(user.getUsername(), query);
	}

	/**
	 * Returns a communities definition.
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
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Ohmlet getOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_COMMUNITY_ID) final String ohmletId) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read a ohmlet: " + ohmletId);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		LOGGER.log(Level.INFO, "Retrieving the ohmlet.");
		Ohmlet ohmlet =
			OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user is allowed to query the " +
					"ohmlet.");
		LOGGER.log(Level.FINE, "Checking if the ohmlet is not private.");
		if(!
			Ohmlet
				.PrivacyState
				.PRIVATE
				.equals(ohmlet.getPrivacyState())) {

			LOGGER
				.log(
					Level.FINE,
					"The ohmlet is private, so the user must already be " +
						"assocaited with the ohmlet.");
			if(! ohmlet.hasRole(user.getUsername())) {
				throw
					new InsufficientPermissionsException(
						"The user does not have sufficient permissions to " +
							"view this " + Ohmlet.COMMUNITY_SKIN + ".");
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
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.POST)
	public static @ResponseBody void updateOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_COMMUNITY_ID) final String ohmletId,
		@RequestBody
			final Ohmlet.Builder ohmletBuilder) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to update a ohmlet: " + ohmletId);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		LOGGER.log(Level.INFO, "Retrieving the ohmlet.");
		Ohmlet ohmlet =
			OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER.log(Level.INFO, "Verifying that the ohmlet exists.");
		if(ohmlet == null) {
			throw
				new UnknownEntityException(
					"The " + Ohmlet.COMMUNITY_SKIN + " is unknown.");
		}

		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user is allowed to modify " +
					"the ohmlet.");
		if(! ohmlet.canModifyOhmlet(user.getUsername())) {
			throw
				new InsufficientPermissionsException(
					"The user does not have sufficient permissions to " +
						"update the " + Ohmlet.COMMUNITY_SKIN + ".");
		}

		LOGGER
			.log(
				Level.FINE,
				"Creating a new builder based on the existing ohmlet.");
		Ohmlet.Builder newOhmletBuilder =
			new Ohmlet.Builder(ohmlet);

		LOGGER.log(Level.FINE, "Merging the changes into the old ohmlet.");
		newOhmletBuilder.merge(ohmletBuilder);

		LOGGER.log(Level.FINE, "Building a new ohmlet.");
		Ohmlet newOhmlet = newOhmletBuilder.build();

        // Validate that the streams exist.
        LOGGER.log(Level.INFO, "Validating that the given streams exist.");
        List<SchemaReference> streams = newOhmlet.getStreams();
        for(SchemaReference stream : streams) {
            // Get the schema ID.
            String id = stream.getSchemaId();
            // Get the schema version.
            Long version = stream.getVersion();

            LOGGER
                .log(Level.INFO, "Checking if the stream is a known stream.");
            if(! StreamBin.getInstance().exists(id, version)) {
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
        LOGGER.log(Level.INFO, "Validating that the given surveys exist.");
        List<SchemaReference> surveys = newOhmlet.getSurveys();
        for(SchemaReference survey : surveys) {
            // Get the schema ID.
            String id = survey.getSchemaId();
            // Get the schema version.
            Long version = survey.getVersion();

            LOGGER
                .log(Level.INFO, "Checking if the survey is a known survey.");
            if(! SurveyBin.getInstance().exists(id, version)) {
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

		LOGGER.log(Level.FINE, "Storing the updated ohmlet.");
		OhmletBin.getInstance().updateOhmlet(newOhmlet);
	}

	/**
	 * Allows a user to modify their or another user's privileges. This can be
	 * used by users with the {@link Ohmlet.Role#INVITED} role to elevate
	 * their own role to {@link Ohmlet.Role#MEMBER} or by users with
	 * sufficient privileges to escalate or de-escalate another user's role.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @param ohmletId
	 *        The ohmlet's unique identifier.
	 *
	 * @param member
	 * 		  The information about the user that is being changed.
	 */
	@RequestMapping(
		value =
			"{" + KEY_COMMUNITY_ID + "}" +
			"/" + Ohmlet.JSON_KEY_MEMBERS,
		method = RequestMethod.POST)
	public static @ResponseBody void updateRole(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_COMMUNITY_ID) final String ohmletId,
		@RequestBody final Ohmlet.Member member) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to modify a user's privileges in a " +
					"ohmlet: " +
					ohmletId);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		LOGGER.log(Level.INFO, "Retrieving the ohmlet.");
		Ohmlet ohmlet =
			OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER.log(Level.INFO, "Verifying that the ohmlet exists.");
		if(ohmlet == null) {
			throw
				new UnknownEntityException(
					"The " + Ohmlet.COMMUNITY_SKIN + " is unknown.");
		}

		LOGGER.log(Level.FINE, "Retrieving the requesting user's role.");
		Ohmlet.Role requesterRole = ohmlet.getRole(user.getUsername());

		LOGGER.log(Level.INFO, "Validating the request.");
		if(user.getUsername().equals(member.getMemberId())) {
			LOGGER
				.log(
					Level.FINE,
					"The user is attempting to update their own role.");
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
								Ohmlet.COMMUNITY_SKIN +
								" is private, therefore the user may not " +
								"request an invite.");
				}
				break;

			case INVITED:
				throw
					new InvalidArgumentException(
						"A user cannot invite themselves.");

			case MEMBER:
				if(
					(requesterRole == null) &&
					(!
						Ohmlet
							.PrivacyState
							.PUBLIC
							.equals(ohmlet.getPrivacyState()))) {

					throw
						new InvalidArgumentException(
							"A user may not directly join a non-public " +
								Ohmlet.COMMUNITY_SKIN + ".");
				}
				// Cascade.

			default:
				if(Ohmlet.Role.MEMBER.supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"The user cannot modify their role until they " +
								"have been invited.");
				}
				else if(member.getRole().supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"A user may not elevate their own role beyond " +
								"its current value.");
				}
				break;
			}
		}
		else {
			LOGGER
				.log(
					Level.FINE,
					"A user is attempting to modify another user's role.");

			LOGGER.log(Level.INFO, "Verifying that the other user exists.");
			if(UserBin.getInstance().getUser(member.getMemberId()) == null) {
				throw
					new InvalidArgumentException(
						"The user does not exist: " +
							member.getMemberId());
			}

			LOGGER
				.log(
					Level.FINE,
					"Retreving the requestee's role in the ohmlet.");
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
								Ohmlet.COMMUNITY_SKIN + ".");
				}
				break;

			default:
				if(member.getRole().supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"A user may not elevate another user's role " +
								"beyond their own role.");
				}
				if(requesteeRole.supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"The user may not change the role of a user " +
								"with a higher role.");
				}
			}
		}

		LOGGER
			.log(
				Level.INFO,
				"Updating the ohmlet to reflect the role change.");
		Ohmlet updatedOhmlet =
			(new Ohmlet.Builder(ohmlet))
				.addMember(member.getMemberId(), member.getRole())
				.build();

		LOGGER.log(Level.INFO, "Saving the updated ohmlet.");
		OhmletBin.getInstance().updateOhmlet(updatedOhmlet);
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
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.DELETE)
	public static @ResponseBody void deleteOhmlet(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
		@PathVariable(KEY_COMMUNITY_ID) final String ohmletId) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to delete a ohmlet: " + ohmletId);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = authToken.getUser();

		LOGGER.log(Level.INFO, "Retrieving the ohmlet.");
		Ohmlet ohmlet =
			OhmletBin.getInstance().getOhmlet(ohmletId);

		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user can delete the " +
					"ohmlet.");
		if(! ohmlet.hasRole(user.getUsername(), Ohmlet.Role.OWNER)) {
			throw
				new InsufficientPermissionsException(
					"The user does not have enough permissions to delete " +
						"the " +
						Ohmlet.COMMUNITY_SKIN + ".");
		}

		LOGGER.log(Level.INFO, "Deleting the ohmlet.");
		OhmletBin.getInstance().deleteOhmlet(ohmletId);
	}
}