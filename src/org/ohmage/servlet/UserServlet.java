package org.ohmage.servlet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.auth.provider.Provider;
import org.ohmage.auth.provider.ProviderRegistry;
import org.ohmage.bin.CommunityBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.Community;
import org.ohmage.domain.Community.SchemaReference;
import org.ohmage.domain.ProviderUserInformation;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.Stream;
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
 * The controller for all requests to the list of users.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(UserServlet.ROOT_MAPPING)
@SessionAttributes(
	{
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN,
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM
	})
public class UserServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/users";
	
	/**
	 * The path key for the user-name.
	 */
	public static final String KEY_USERNAME = "username";
	
	/**
	 * The parameter key for provider identifiers.
	 */
	public static final String PARAMETER_PROVIDER = "provider";
	/**
	 * The parameter key for provider access tokens.
	 */
	public static final String PARAMETER_ACCESS_TOKEN = "access_token";
	
	/**
	 * The parameter key for the Captcha challenge.
	 */
	public static final String PARAMETER_CAPTCHA_CHALLENGE =
		"captcha_challenge";
	/**
	 * The parameter key for the Captcha response.
	 */
	public static final String PARAMETER_CAPTCHA_RESPONSE = "captcha_response";
	
	/**
	 * The preference key for the Captcha private key.
	 */
	public static final String PREFERENCE_KEY_CAPTCHA_PRIVATE_KEY =
		"ohmage.captcha_private_key";
	
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(UserServlet.class.getName());
	
	/**
	 * Creates a user user.
	 * 
	 * @param username
	 *        The new user's user-name.
	 * 
	 * @param plaintextPassword
	 *        The new user's plain-text password.
	 * 
	 * @param email
	 *        The new user's email address.
	 * 
	 * @param fullName
	 *        The new user's full name, which may be null.
	 * 
	 * @param captchaChallenge
	 *        The reCaptcha challenge key.
	 * 
	 * @param captchaResponse
	 *        The user's reCaptcha response.
	 * 
	 * @param request
	 *        The HTTP request.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = RequestMethod.POST,
		params = {
			User.JSON_KEY_PASSWORD/*,
			PARAMETER_CAPTCHA_CHALLENGE,
			PARAMETER_CAPTCHA_RESPONSE*/
		})
	public static @ResponseBody User createOhmageUser(
		/*
		@RequestParam(
			value = PARAMETER_CAPTCHA_CHALLENGE,
			required = true)
			final String captchaChallenge,
		@RequestParam(
			value = PARAMETER_CAPTCHA_RESPONSE,
			required = true)
			final String captchaResponse,
		final HttpServletRequest request,
		*/
		@RequestParam(value = User.JSON_KEY_PASSWORD, required = true)
			final String password,
		@RequestBody
			final User.Builder userBuilder) {
		
		LOGGER.log(Level.INFO, "Creating a new user.");

		/*
		LOGGER.log(Level.INFO, "Validating the captcha information.");
		LOGGER.log(Level.FINE, "Building the ReCaptcha validator.");
		ReCaptchaImpl reCaptcha = new ReCaptchaImpl();

		LOGGER.log(Level.FINE, "Setting out private key.");
		reCaptcha.setPrivateKey(
			ConfigurationFileImport
				.getCustomProperties()
				.getProperty(PREFERENCE_KEY_CAPTCHA_PRIVATE_KEY));
		
		LOGGER.log(Level.FINE, "Comparing the user's response.");
		ReCaptchaResponse reCaptchaResponse = 
			reCaptcha
				.checkAnswer(
					request.getRemoteAddr(),
					captchaChallenge,
					captchaResponse);
		
		LOGGER.log(Level.INFO, "Ensuring the response was valid.");
		if(! reCaptchaResponse.isValid()) {
			throw
				new InvalidArgumentException(
					"The reCaptcha response was invalid.");
		}
		*/
		
		LOGGER.log(Level.INFO, "Verifying that a user supplied a password.");
		if(password == null) {
			throw new InvalidArgumentException("A password was not given.");
		}
		
		LOGGER.log(Level.INFO, "Hashing the user's password.");
		userBuilder.setPassword(password, true);

		LOGGER.log(Level.FINE, "Building the user.");
		User validatedUser = userBuilder.build();
		
		// TODO: This is where the email validation process will go.

		LOGGER.log(Level.INFO, "Storing the user.");
		UserBin.getInstance().addUser(validatedUser);

		LOGGER.log(Level.INFO, "Echoing the user back.");
		return validatedUser;
	}
	
	/**
	 * Creates a user user.
	 * 
	 * @param username
	 *        The new user's user-name.
	 * 
	 * @param fullName
	 *        The new user's full name, which may be null.
	 * 
	 * @param provider
	 *        The provider's internal identifier.
	 * 
	 * @param accessToken
	 *        The access token provided by the provider to be used to
	 *        authenticate the user.
	 * 
	 * @param request
	 *        The HTTP request.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = RequestMethod.POST,
		params = { PARAMETER_PROVIDER, PARAMETER_ACCESS_TOKEN })
	public static @ResponseBody User createUser(
		@RequestParam(
			value = PARAMETER_PROVIDER,
			required = true)
			final String provider,
		@RequestParam(
			value = PARAMETER_ACCESS_TOKEN,
			required = true)
			final String accessToken,
		@RequestBody
			final User.Builder userBuilder) {
		
		LOGGER.log(Level.INFO, "Creating a new user.");
		
		LOGGER.log(Level.INFO, "Verifying that a username was given.");
		if(userBuilder.getUsername() == null) {
			throw new InvalidArgumentException("A username was not provided.");
		}
		
		LOGGER.log(Level.FINE, "Retrieving the provider implementation.");
		Provider providerObject = ProviderRegistry.get(provider);
		
		LOGGER
			.log(
				Level.INFO,
				"Building the user's information based on the provider.");
		ProviderUserInformation userInformation =
			providerObject.getUserInformation(accessToken);
		
		// Determine if the user has already created an account with
		// these credentials.
		LOGGER
			.log(
				Level.INFO,
				"Verifying that this provider account is not already linked " +
					"an existing ohmage account.");
		User user =
			UserBin
				.getInstance()
				.getUserFromProvider(
					userInformation.getProviderId(),
					userInformation.getUserId());
		if(user != null) {
			throw
				new InvalidArgumentException(
					"An ohmage account is already associated with this " +
						"provider-based user.");
		}
		
		LOGGER
			.log(
				Level.FINER,
				"Attaching the provider information to the user.");
		userBuilder
			.addProvider(userInformation.getProviderId(), userInformation);
		
		LOGGER
			.log(
				Level.FINER,
				"Adding the provider-based information's email address to " +
					"the user object.");
		userBuilder.setEmail(userInformation.getEmail());
		
		LOGGER.log(Level.FINE, "Building the user.");
		user = userBuilder.build();
		
		LOGGER.log(Level.INFO, "Storing the user.");
		UserBin.getInstance().addUser(user);
		
		LOGGER.log(Level.INFO, "Echoing back the user object.");
		return user;
	}
	
	/**
	 * Retrieves the list of users that are visible to the requesting user.
	 * 
	 * @return The list of users that are visible to the requesting user.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody Set<String> getVisibleUsers(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token) {
		
		LOGGER.log(Level.INFO, "Requesting a list of visible users.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, null);

		LOGGER.log(Level.FINE, "Create the result list.");
		Set<String> result = new HashSet<String>(1);
		LOGGER
			.log(
				Level.INFO,
				"If the calling user authenticated themselves, adding them " +
					"to the result list.");
		result.add(user.getUsername());

		return result;
	}
	
	/**
	 * Retrieves the information about a user.
	 * 
	 * @param token
	 *        The authentication token used to validate the caller.
	 * 
	 * @param username
	 *        The user whose information is desired.
	 * 
	 * @return The desired user's information.
	 */
	@RequestMapping(
		value = "{" + KEY_USERNAME + ":.+" + "}",
		method = RequestMethod.GET)
	public static @ResponseBody User getUserInformation(
		@ModelAttribute(value=AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@PathVariable(KEY_USERNAME) final String username) {
		
		LOGGER.log(Level.INFO, "Requesting information about a user.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, null);
		
		// Users are only visible to read their own data at this time.
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is requesting information about " +
					"themselves.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"A user may only view their own information.");
		}
		
		// Pull the user object from the token.
		LOGGER.log(Level.INFO, "Retreiving the user object.");
		return user;
	}
	
	/**
	 * Updates a user's password.
	 * 
	 * @param token
	 *        The authentication token used to validate the caller.
	 * 
	 * @param username
	 *        The user whose information is desired.
	 * 
	 * @param oldPassword
	 *        The user's current password.
	 * 
	 * @param newPassword
	 *        The user's new password.
	 * 
	 * @return The desired user's information.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_PASSWORD,
		method = RequestMethod.POST)
	public static @ResponseBody void updateUserPassword(
		@PathVariable(KEY_USERNAME) final String username,
		@RequestParam(
			value = User.JSON_KEY_PASSWORD,
			required = true)
			final String oldPassword,
		@RequestBody final String newPassword) {
		
		LOGGER.log(Level.INFO, "Updating a user's password.");
		
		LOGGER
			.log(Level.FINE, "Verifying that the new password is not empty.");
		if((newPassword == null) || (newPassword.length() == 0)) {
			throw new InvalidArgumentException("The new password is missing.");
		}
		
		LOGGER.log(Level.FINE, "Retrieving the user.");
		User user = UserBin.getInstance().getUser(username);
		
		LOGGER.log(Level.INFO, "Verifying that the user exists.");
		if(user == null) {
			throw new UnknownEntityException("The user is unknown.");
		}
		
		LOGGER.log(Level.INFO, "Verifying that the old password is correct.");
		if(! user.verifyPassword(oldPassword)) {
			throw new AuthenticationException("The password is incorrect.");
		}
		
		LOGGER.log(Level.INFO, "Updating the user's password.");
		user = user.updatePassword(User.hashPassword(newPassword));
		
		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(user);
		
		// Should we also invalidate all authentication tokens?
	}
	
	/**
	 * Retrieves the set of communities that this user is part of.
	 * 
	 * @param token
	 *        The user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the user's authentication token was sent as a
	 *        parameter.
	 * 
	 * @param username
	 *        The user's user-name, which, for now, must match the
	 *        authentication token.
	 * 
	 * @return The set of communities that this user is part of.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_COMMUNITIES,
		method = RequestMethod.GET)
	public static @ResponseBody Collection<User.CommunityReference> getFollowedCommunities(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username) {
				
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to track a stream.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is querying their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only query their own accounts.");
		}
		
		LOGGER.log(Level.INFO, "Returning the set of stream references.");
		return user.getCommunities();
	}
	
	/**
	 * Retrieves the specific information for a community that the user is
	 * following.
	 * 
	 * @param token
	 *        The user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the user's authentication token was sent as a
	 *        parameter.
	 * 
	 * @param username
	 *        The user's user-name, which, for now, must match the
	 *        authentication token.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 * 
	 * @return The user-specific information about a community that they are
	 *         following.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" +
			User.JSON_KEY_COMMUNITIES + "/" +
			"{" + Community.JSON_KEY_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody User.CommunityReference getFollowedCommunity(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username,
		@PathVariable(Community.JSON_KEY_ID) final String communityId) {
				
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to track a stream.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is reading their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only read their own accounts.");
		}
		
		LOGGER.log(Level.INFO, "Returning the set of stream references.");
		return user.getCommunity(communityId);
	}
	
	/**
	 * Retrieves the specific information for a community that the user is
	 * following.
	 * 
	 * @param token
	 *        The user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the user's authentication token was sent as a
	 *        parameter.
	 * 
	 * @param username
	 *        The user's user-name, which, for now, must match the
	 *        authentication token.
	 * 
	 * @param communityId
	 *        The community's unique identifier.
	 * 
	 * @return The user-specific information about a community that they are
	 *         following.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" +
			User.JSON_KEY_COMMUNITIES + "/" +
			"{" + Community.JSON_KEY_ID + "}",
		method = RequestMethod.DELETE)
	public static @ResponseBody void leaveCommunity(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username,
		@PathVariable(Community.JSON_KEY_ID) final String communityId) {
				
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to disassociate a user with a community.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		LOGGER.log(Level.INFO, "Retrieving the community.");
		Community community =
			CommunityBin.getInstance().getCommunity(communityId);
		
		LOGGER.log(Level.INFO, "Checking if the community exists.");
		if(community != null) {
			LOGGER
				.log(
					Level.INFO,
					"The community exists, so the user is being removed.");
			
			LOGGER.log(Level.INFO, "Removing the user from the community.");
			Community.Builder communityBuilder = new Community.Builder(community);
			communityBuilder.removeMember(user.getUsername());
			Community updatedCommunity = communityBuilder.build();
			
			LOGGER.log(Level.INFO, "Removing the user from the community.");
			CommunityBin.getInstance().updateCommunity(updatedCommunity);
		}
		else {
			LOGGER.log(Level.INFO, "The community does not exist.");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Removing the community from the user's profile.");
		User.Builder updatedUserBuilder = new User.Builder(user);
		updatedUserBuilder.removeCommunity(communityId);
		User updatedUser = updatedUserBuilder.build();
		
		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);
	}
	
	/**
	 * Allows a user to follow a stream. The user can optionally supply a
	 * version. If the version is given, that indicates that the user wishes to
	 * follow a specific version of the stream. If a version is not given, that
	 * indicates that a user wishes to follow the latest version of the stream.
	 * 
	 * @param token
	 *        The requesting user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was given as a parameter.
	 * 
	 * @param username
	 *        The user-name of the user that is following this stream. For now,
	 *        a user may only update their own profile, so this must match the
	 *        authentication token's user.
	 * 
	 * @param streamReference
	 *        A reference to the stream that must include the stream's unique
	 *        identifier and may include a specific version of the stream.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_STREAMS,
		method = RequestMethod.POST)
	public static @ResponseBody void followStream(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username,
		@RequestBody final SchemaReference streamReference) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to track a stream.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		LOGGER.log(Level.FINE, "Retrieving the stream.");
		Stream stream;
		if(streamReference.getVersion() == null) {
			stream =
				StreamBin
					.getInstance()
					.getLatestStream(streamReference.getSchemaId());
		}
		else {
			stream =
				StreamBin
					.getInstance()
					.getStream(
						streamReference.getSchemaId(),
						streamReference.getVersion());
		}
		
		LOGGER.log(Level.INFO, "Verifying that the stream exists.");
		if(stream == null) {
			throw
				new InvalidArgumentException("The stream does not exist.");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Adding the stream to the list of streams being followed.");
		User.Builder updatedUserBuilder = new User.Builder(user);
		updatedUserBuilder.addStream(streamReference);
		User updatedUser = updatedUserBuilder.build();
		
		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);
	}
	
	/**
	 * Retrieves the set of streams that this user is watching.
	 * 
	 * @param token
	 *        The user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the user's authentication token was sent as a
	 *        parameter.
	 * 
	 * @param username
	 *        The user's user-name, which, for now, must match the
	 *        authentication token.
	 * 
	 * @return The set of stream references that this user is watching.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_STREAMS,
		method = RequestMethod.GET)
	public static @ResponseBody Set<SchemaReference> getFollowedStreams(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username) {
				
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to view streams they are " +
					"following.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		LOGGER.log(Level.INFO, "Returning the set of stream references.");
		return user.getStreams();
	}
	
	/**
	 * Stops a user from following a stream.
	 * 
	 * @param token
	 *        The requesting user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was given as a parameter.
	 * 
	 * @param username
	 *        The user-name of the user that is following this stream. For now,
	 *        a user may only update their own profile, so this must match the
	 *        authentication token's user.
	 * 
	 * @param streamReference
	 *        A reference to the stream that must include the stream's unique
	 *        identifier and may include a specific version of the stream.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_STREAMS,
		method = RequestMethod.DELETE)
	public static @ResponseBody void forgetStream(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username,
		@RequestBody final SchemaReference streamReference) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to stop tracking a stream.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Adding the stream to the list of streams being followed.");
		User.Builder updatedUserBuilder = new User.Builder(user);
		updatedUserBuilder.removeStream(streamReference);
		User updatedUser = updatedUserBuilder.build();
		
		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);
	}
	
	/**
	 * Allows a user to follow a survey. The user can optionally supply a
	 * version. If the version is given, that indicates that the user wishes to
	 * follow a specific version of the stream. If a version is not given, that
	 * indicates that a user wishes to follow the latest version of the stream.
	 * 
	 * @param token
	 *        The requesting user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was given as a parameter.
	 * 
	 * @param username
	 *        The user-name of the user that is following this survey. For now,
	 *        a user may only update their own profile, so this must match the
	 *        authentication token's user.
	 * 
	 * @param surveyReference
	 *        A reference to the survey that must include the survey's unique
	 *        identifier and may include a specific version of the survey.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_SURVEYS,
		method = RequestMethod.POST)
	public static @ResponseBody void followSurvey(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username,
		@RequestBody final SchemaReference surveyReference) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to track a survey.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		// TODO: Implement surveys, remove this, and un-comment below it.
		throw new InvalidArgumentException("Surveys are not yet implemented.");
		
//		LOGGER.log(Level.FINE, "Retrieving the survey.");
//		Survey survey;
//		if(surveyReference.getVersion() == null) {
//			survey =
//				SurveyBin
//					.getInstance()
//					.getLatestSurvey(surveyReference.getSchemaId());
//		}
//		else {
//			survey =
//				SurveyBin
//					.getInstance()
//					.getSurvey(
//						surveyReference.getSchemaId(),
//						surveyReference.getVersion());
//		}
//		
//		LOGGER.log(Level.INFO, "Verifying that the survey exists.");
//		if(survey == null) {
//			throw
//				new InvalidArgumentException("The survey does not exist.");
//		}
//		
//		LOGGER
//			.log(
//				Level.INFO,
//				"Adding the stream to the list of surveys being followed.");
//		User.Builder updatedUserBuilder = new User.Builder(user);
//		updatedUserBuilder.addSurvey(surveyReference);
//		User updatedUser = updatedUserBuilder.build();
//		
//		LOGGER.log(Level.INFO, "Storing the updated user.");
//		UserBin.getInstance().updateUser(updatedUser);
	}
	
	/**
	 * Retrieves the set of surveys that this user is watching.
	 * 
	 * @param token
	 *        The user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the user's authentication token was sent as a
	 *        parameter.
	 * 
	 * @param username
	 *        The user's user-name, which, for now, must match the
	 *        authentication token.
	 * 
	 * @return The set of survey references that this user is watching.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_SURVEYS,
		method = RequestMethod.GET)
	public static @ResponseBody Set<SchemaReference> getFollowedSurveys(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username) {
				
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to view surveys they are " +
					"following.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		LOGGER.log(Level.INFO, "Returning the set of survey references.");
		return user.getSurveys();
	}
	
	/**
	 * Stops a user from following a survey.
	 * 
	 * @param token
	 *        The requesting user's authentication token.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was given as a parameter.
	 * 
	 * @param username
	 *        The user-name of the user that is following this survey. For now,
	 *        a user may only update their own profile, so this must match the
	 *        authentication token's user.
	 * 
	 * @param surveyReference
	 *        A reference to the survey that must include the survey's unique
	 *        identifier and may include a specific version of the survey.
	 */
	@RequestMapping(
		value =
			"{" + KEY_USERNAME + ":.+" + "}" + "/" + User.JSON_KEY_SURVEYS,
		method = RequestMethod.DELETE)
	public static @ResponseBody void forgetSurvey(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_USERNAME) final String username,
		@RequestBody final SchemaReference surveyReference) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a user to stop tracking a survey.");
		
		LOGGER
			.log(Level.INFO, "Retrieving the user associated with the token.");
		User user = AuthFilter.retrieveUserFromAuth(null, token, tokenIsParam);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user is updating their own profile.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"Users may only modify their own accounts.");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Adding the stream to the list of surveys being followed.");
		User.Builder updatedUserBuilder = new User.Builder(user);
		updatedUserBuilder.removeSurvey(surveyReference);
		User updatedUser = updatedUserBuilder.build();
		
		LOGGER.log(Level.INFO, "Storing the updated user.");
		UserBin.getInstance().updateUser(updatedUser);
	}
	
	/**
	 * Disables the user's account.
	 * 
	 * @param username
	 *        The user whose account is being disabled.
	 * 
	 * @param password
	 *        The user's password to confirm the deletion.
	 */
	@RequestMapping(
		value = "{" + KEY_USERNAME + ":.+" + "}",
		method = RequestMethod.DELETE,
		params = { User.JSON_KEY_PASSWORD })
	public static @ResponseBody void deleteUserWithPassword(
		@PathVariable(KEY_USERNAME)
			final String username,
		@RequestParam(
			value = User.JSON_KEY_PASSWORD,
			required = true)
			final String password) {
		
		LOGGER.log(Level.INFO, "Deleting a user.");
		
		LOGGER.log(Level.FINE, "Retreiving the user.");
		User user = UserBin.getInstance().getUser(username);
		
		LOGGER.log(Level.INFO, "Verifying the user's password.");
		user.verifyPassword(password);
		
		LOGGER.log(Level.INFO, "Disabling the user's account.");
		UserBin.getInstance().disableUser(username);
	}
	
	/**
	 * Disables the user's account.
	 * 
	 * @param username
	 *        The user whose account is being disabled.
	 * 
	 * @param providerId
	 *        The internal unique identifier of the provider.
	 * 
	 * @param accessToken
	 *        A provider-generated access token to authenticate the user and
	 *        validate the request.
	 */
	@RequestMapping(
		value = "{" + KEY_USERNAME + ":.+" + "}",
		method = RequestMethod.DELETE,
		params = { PARAMETER_PROVIDER, PARAMETER_ACCESS_TOKEN })
	public static @ResponseBody void deleteUserWithProvider(
		@PathVariable(KEY_USERNAME) final String username,
		@RequestParam(
			value = PARAMETER_PROVIDER,
			required = true)
			final String providerId,
		@RequestParam(
			value = PARAMETER_ACCESS_TOKEN,
			required = true)
			final String accessToken) {
		
		LOGGER.log(Level.INFO, "Deleting a user.");

		LOGGER
			.log(
				Level.FINE,
				"Retrieving the implementation for this provider.");
		Provider provider = ProviderRegistry.get(providerId);
		
		LOGGER
			.log(
				Level.INFO,
				"Retrieving the user based on the access token.");
		ProviderUserInformation userInformation =
			provider.getUserInformation(accessToken);
		
		LOGGER
			.log(
				Level.INFO,
				"Retrieving the ohmage account linked with the " +
					"provider-given ID.");
		User user =
			UserBin
				.getInstance()
				.getUserFromProvider(
					userInformation.getProviderId(),
					userInformation.getUserId());
		if(user == null) {
			LOGGER
				.log(
					Level.INFO,
					"No ohmage account has linked itself with these " +
						"credentials.");
			throw
				new InsufficientPermissionsException(
					"The user has not yet created an ohmage account.");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the requesting user is the same as the user " +
					"that is attempting to be deleted.");
		if(! user.getUsername().equals(username)) {
			throw
				new InsufficientPermissionsException(
					"No user can delete another user's account.");
		}
		
		LOGGER.log(Level.INFO, "Disabling the user's account.");
		UserBin.getInstance().disableUser(user.getUsername());
	}
}