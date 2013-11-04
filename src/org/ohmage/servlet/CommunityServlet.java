package org.ohmage.servlet;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.CommunityBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.Community;
import org.ohmage.domain.Community.SchemaReference;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.InvalidArgumentException;
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
	public static @ResponseBody void createProject(
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
		communityBuilder.setOwner(user.getUsername());

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
	}
	
	/**
	 * Returns a list of visible community IDs.
	 * 
	 * @param search
	 *        A value that should appear in either the name or description.
	 * 
	 * @return A list of visible community IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getCommunityIds(
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		LOGGER.log(Level.INFO, "Creating a community ID read request.");

		return CommunityBin.getInstance().getCommunityIds(query);
	}
	
	/**
	 * Returns a list of versions for the given community.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 * 
	 * @param search
	 *        A value that should appear in either the name or description.
	 * 
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_COMMUNITY_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Community getCommunity(
		@PathVariable(KEY_COMMUNITY_ID) final String communityId,
		@RequestParam(value = KEY_QUERY, required = false)
			final String query) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read the versions of a community: " +
					communityId);
		
		return CommunityBin.getInstance().getCommunity(communityId);
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
		
		// TODO:
	}
}