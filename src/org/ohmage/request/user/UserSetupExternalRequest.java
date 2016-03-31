package org.ohmage.request.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.KeycloakUser;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.UserValidators;

public class UserSetupExternalRequest extends UserRequest {
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(UserCreationRequest.class);

	private final Set<String> classIds;
	private final String newUsername;


	/**
	 * Creates a user creation request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required and
	 *            optional parameters for creating this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 *                   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserSetupExternalRequest(
			final HttpServletRequest httpRequest)
					throws IOException, InvalidRequestException {

		super(httpRequest, null, TokenLocation.EITHER, null);

		Set<String> tClassIds = null;
		String tNewUsername = null;

		if(! isFailed()) {
			LOGGER.info("Creating a user setup external request.");

			try {
				String[] t;     

				// Get and validate the class ID.
				t = getParameterValues(InputKeys.CLASS_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID,
							"Multiple class ID lists were given: " +
									InputKeys.CLASS_URN_LIST);
				}
				else if(t.length == 1) {
					tClassIds = ClassValidators.validateClassIdList(t[0]);
				}

				// get external user account name 
				t = getParameterValues(InputKeys.NEW_USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME_PROPERTY,
							"Multiple new usernames were given: " +
									InputKeys.NEW_USERNAME);
				}
				else if ((t.length == 1) && (! StringUtils.isEmptyOrWhitespaceOnly(t[0]))) {
					try {
						tNewUsername = UserValidators.validateUsername(t[0].trim());
					} catch (ValidationException e) {
						throw new ValidationException(
								ErrorCode.USER_INVALID_USERNAME_PROPERTY,
								"Invalid new username pattern: " +
										InputKeys.NEW_USERNAME, 
										e);
					}
				}    

			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}

		classIds = tClassIds;
		newUsername = tNewUsername;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user setup request.");

		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}

		try {
			LOGGER.info("Verifying that the requesting user can setup users.");
			UserServices
			.instance()
			.verifyUserCanSetupUsers(getUser().getUsername());

			if(classIds != null) {
				LOGGER.info("Verifying that the classes exist.");
				ClassServices.instance().checkClassesExistence(classIds, true);
			}

			LOGGER.info("Determining if the user already exists.");
			boolean userExistsAndExternal =
					UserServices
					.instance()
					.userExistsAndIsExternal(newUsername);

			// If the user does not exists or external. create them
			// This will throw an exception if the user exists but is not external,
			// which can be considered intentional.
			if(userExistsAndExternal == false) {
				LOGGER.info("The user does not exist or is not external.");
				LOGGER.info("Trying to create the user.");
				UserServices
				.instance()
				.createUser(
						newUsername,
						KeycloakUser.KEYCLOAK_USER_PASSWORD,
						null,
						false,  // admin
						true,   // enable
						false,   // new account
						true,   // campaign creation privilege. should read from config
						false,   // storeInitialPassword
						true,  // externalAccount
						null);
			}

			// Add them to the class, if given.
			if(classIds != null) {
				// need to check that the requester is a privileged
				// user in the class 


				// For each class, reset the user's role in that class to
				// restricted.
				for(String classId : classIds) {
					Map<String, Clazz.Role> usersToAdd =
							new HashMap<String, Clazz.Role>();
					usersToAdd.put(newUsername, Clazz.Role.RESTRICTED);

					LOGGER.info("Reseting the user in the class: " + classId);
					ClassServices
					.instance()
					.updateClass(
							classId, 
							null, 
							null, 
							usersToAdd, 
							null);
				}
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {

		JSONObject response = new JSONObject();
		try {
			response.put("username", newUsername);
			response.put("external", true);

			super.respond(httpRequest, httpResponse, response);
		}
		catch(JSONException e) {
			super.respond(httpRequest, httpResponse, (JSONObject) null);
		}
	}

}