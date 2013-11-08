package org.ohmage.servlet;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.CommunityBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.Community;
import org.ohmage.domain.Community.SchemaReference;
import org.ohmage.domain.User;
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
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * <p>
 * The controller for all requests to community entities.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(CommunityServlet.ROOT_MAPPING)
@SessionAttributes(
	{
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN,
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM
	})
public class CommunityServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/communities";
	
	/**
	 * The path and parameter key for community IDs.
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
		Logger.getLogger(CommunityServlet.class.getName());
	
	/**
	 * The usage in this class is entirely static, so there is no need to
	 * instantiate it.
	 */
	private CommunityServlet() {
		// Do nothing.
	}

	/**
	 * Creates a new community.
	 * 
	 * @param token
	 *        The authentication token for the user that is creating this
	 *        request.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was sent as a parameter.
	 * 
	 * @param communityBuilder
	 *        The parts of the community that are already set.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public static @ResponseBody Community createCommunity(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@RequestBody
			final Community.Builder communityBuilder) {
		
		LOGGER.log(Level.INFO, "Creating a community creation request.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.FINE,
				"Setting the token's owner as the creator of this community.");
		communityBuilder.addMember(user.getUsername(), Community.Role.OWNER);

		LOGGER.log(Level.FINE, "Building the community.");
		Community community = communityBuilder.build();
		
		// Validate that the streams exist.
		LOGGER.log(Level.INFO, "Validating that the given streams exist.");
		List<SchemaReference> streams = community.getStreams();
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
		
		// TODO: Validate that the surveys exist.

		LOGGER.log(Level.INFO, "Adding the community to the database.");
		CommunityBin.getInstance().addCommunity(community);
		
		LOGGER.log(Level.INFO, "Updating the user.");
		User.Builder updatedUserBuilder = new User.Builder(user);
		LOGGER.log(Level.FINE, "Building the community reference.");
		User.CommunityReference communityReference =
			new User.CommunityReference(community.getId(), null, null);
		LOGGER.log(Level.FINE, "Adding the community reference to the user.");
		updatedUserBuilder.addCommunity(communityReference);
		LOGGER.log(Level.FINE, "Building the user.");
		User updatedUser = updatedUserBuilder.build();
		
		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);
		
		return community;
	}
	
	/**
	 * Returns a list of visible community IDs.
	 * 
	 * @param token
	 *        Used for limiting which communities are returned based on
	 *        visibility.
	 * 
	 * @param search
	 *        A value that should appear in either the name or description.
	 * 
	 * @return A list of visible community IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getCommunityIds(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		
		LOGGER.log(Level.INFO, "Creating a community ID read request.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, null);

		return
			CommunityBin
				.getInstance()
				.getCommunityIds(user.getUsername(), query);
	}
	
	/**
	 * Returns a communities definition.
	 * 
	 * @param token
	 *        Used to verify that this user is allowed to read information
	 *        about a community.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 * 
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Community getCommunity(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@PathVariable(KEY_COMMUNITY_ID) final String communityId) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read a community: " + communityId);
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, null);
		
		LOGGER.log(Level.INFO, "Retrieving the community.");
		Community community =
			CommunityBin.getInstance().getCommunity(communityId);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user is allowed to query the " +
					"community.");
		LOGGER.log(Level.FINE, "Checking if the community is not private.");
		if(!
			Community
				.PrivacyState
				.PRIVATE
				.equals(community.getPrivacyState())) {
			
			LOGGER
				.log(
					Level.FINE,
					"The community is private, so the user must already be " +
						"assocaited with the community.");
			if(! community.hasRole(user.getUsername())) {
				throw 
					new InsufficientPermissionsException(
						"The user does not have sufficient permissions to " +
							"view this community.");
			}
		}
		
		return community;
	}
	
	/**
	 * Updates this community.
	 * 
	 * @param token
	 *        The authentication token for the user updating this community.
	 *        This must belong to an owner of the community.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the authentication token was passed as a
	 *        parameter.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 * 
	 * @param communityBuilder
	 *        The parts of the community that are already set.
	 */
	@RequestMapping(
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.POST)
	public static @ResponseBody void updateCommunity(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_COMMUNITY_ID) final String communityId,
		@RequestBody
			final Community.Builder communityBuilder) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to update a community: " + communityId);
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER.log(Level.INFO, "Retrieving the community.");
		Community community =
			CommunityBin.getInstance().getCommunity(communityId);
		
		LOGGER.log(Level.INFO, "Verifying that the community exists.");
		if(community == null) {
			throw new UnknownEntityException("The community is unknown.");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user is allowed to modify " +
					"the community.");
		if(! community.canModifyCommunity(user.getUsername())) {
			throw
				new InsufficientPermissionsException(
					"The user does not have sufficient permissions to " +
						"update the campaign.");
		}
		
		LOGGER
			.log(
				Level.FINE,
				"Creating a new builder based on the existing community.");
		Community.Builder newCommunityBuilder =
			new Community.Builder(community);
		
		LOGGER.log(Level.FINE, "Merging the changes into the old community.");
		newCommunityBuilder.merge(communityBuilder);
		
		LOGGER.log(Level.FINE, "Building a new community.");
		Community newCommunity = newCommunityBuilder.build();
		
		LOGGER.log(Level.FINE, "Storing the updated community.");
		CommunityBin.getInstance().updateCommunity(newCommunity);
	}
	
	/**
	 * Allows a user to modify their or another user's privileges. This can be
	 * used by users with the {@link Community.Role#INVITED} role to elevate
	 * their own role to {@link Community.Role#MEMBER} or by users with 
	 * sufficient privileges to escalate or de-escalate another user's role.
	 * 
	 * @param token
	 *        The authentication token for the user inviting the other user.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the authentication token was passed as a
	 *        parameter.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 * 
	 * @param member
	 * 		  The information about the user that is being changed.
	 */
	@RequestMapping(
		value =
			"{" + KEY_COMMUNITY_ID + "}" +
			"/" + Community.JSON_KEY_MEMBERS,
		method = RequestMethod.POST)
	public static @ResponseBody void updateRole(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_COMMUNITY_ID) final String communityId,
		@RequestBody final Community.Member member) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to modify a user's privileges in a " +
					"community: " +
					communityId);
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER.log(Level.INFO, "Retrieving the community.");
		Community community =
			CommunityBin.getInstance().getCommunity(communityId);
		
		LOGGER.log(Level.INFO, "Verifying that the community exists.");
		if(community == null) {
			throw new UnknownEntityException("The community is unknown.");
		}

		LOGGER.log(Level.FINE, "Retrieving the requesting user's role.");
		Community.Role requesterRole = community.getRole(user.getUsername());
		
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
								"community.");
				}
				else if(
					Community
						.PrivacyState
						.PRIVATE
						.equals(community.getPrivacyState())) {
					
					throw
						new InvalidArgumentException(
							"The community is private, therefore the user " +
								"may not request an invite.");
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
						Community
							.PrivacyState
							.PUBLIC
							.equals(community.getPrivacyState()))) {
					
					throw
						new InvalidArgumentException(
							"A user may not directly join a non-public " +
								"community.");
				}
				// Cascade.
				
			default:
				if(Community.Role.MEMBER.supersedes(requesterRole)) {
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
					"Retreving the requestee's role in the community.");
			Community.Role requesteeRole =
				community.getRole(member.getMemberId());
			
			switch(member.getRole()) {
			case REQUESTED:
				throw
					new InvalidArgumentException(
						"A user is not allowed to make invitation requests " +
							"for another user.");
				
			case INVITED:
				if(community.getInviteRole().supersedes(requesterRole)) {
					throw
						new InsufficientPermissionsException(
							"The user does not have sufficient permissions " +
								"to invite other users.");
				}
				if(
					(requesteeRole != null) &&
					(! Community.Role.REQUESTED.equals(requesteeRole)) &&
					(! Community.Role.INVITED.equals(requesteeRole))) {
					
					throw
						new InvalidArgumentException(
							"The user is already associated with the " +
								"community.");
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
				"Updating the community to reflect the role change.");
		Community updatedCommunity =
			(new Community.Builder(community))
				.addMember(member.getMemberId(), member.getRole())
				.build();
		
		LOGGER.log(Level.INFO, "Saving the updated community.");
		CommunityBin.getInstance().updateCommunity(updatedCommunity);
	}
	
	/**
	 * Deletes the community. This may only be done by supervisors.
	 * 
	 * @param token
	 *        The authentication token for the user updating this community.
	 *        This must belong to an owner of the community.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the authentication token was passed as a
	 *        parameter.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 */
	@RequestMapping(
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.DELETE)
	public static @ResponseBody void deleteCommunity(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_COMMUNITY_ID) final String communityId) {

		LOGGER
			.log(
				Level.INFO,
				"Creating a request to delete a community: " + communityId);
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER.log(Level.INFO, "Retrieving the community.");
		Community community =
			CommunityBin.getInstance().getCommunity(communityId);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user can delete the " +
					"community.");
		if(! community.hasRole(user.getUsername(), Community.Role.OWNER)) {
			throw
				new InsufficientPermissionsException(
					"The user does not have enough permissions to delete " +
						"the community.");
		}
		
		LOGGER.log(Level.INFO, "Deleting the community.");
		CommunityBin.getInstance().deleteCommunity(communityId);
	}
}