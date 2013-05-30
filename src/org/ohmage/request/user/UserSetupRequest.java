package org.ohmage.request.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;

public class UserSetupRequest extends UserRequest {
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(UserCreationRequest.class);
	
	private static final String USERNAME_PREFIX = "lausd-";
	private static final int USERNAME_DIGITS = 5;
	
	private final UserPersonal personalInfo;
	
	private String username = null;
	private String password = null;
	private String emailAddress = null;
	
	/**
	 * Creates a user creation request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required and
	 * 					  optional parameters for creating this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserSetupRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		UserPersonal tPersonalInfo = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user setup request.");
			
			try {
				String[] t;
				
				// Get and validate the first name.
				String firstName = null;
				t = getParameterValues(InputKeys.FIRST_NAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_FIRST_NAME_VALUE,
							"Multiple first name parameters were given: " +
								InputKeys.FIRST_NAME);
				}
				else if(t.length == 1) {
					firstName = UserValidators.validateFirstName(t[0]);
				}
				if(firstName == null) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_FIRST_NAME_VALUE,
							"Missing the required first name parameter: " + 
								InputKeys.FIRST_NAME);
				}
				
				// Get and validate the last name.
				String lastName = null;
				t = getParameterValues(InputKeys.LAST_NAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_LAST_NAME_VALUE,
							"Multiple last name parameters were given: " +
								InputKeys.LAST_NAME);
				}
				else if(t.length == 1) {
					lastName = UserValidators.validateLastName(t[0]);
				}
				if(lastName == null) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_LAST_NAME_VALUE,
							"Missing the required last name parameter: " + 
								InputKeys.LAST_NAME);
				}
				
				// Get and validate the organization.
				String organization = null;
				t = getParameterValues(InputKeys.ORGANIZATION);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ORGANIZATION_VALUE,
							"Multiple organization parameters were given: " +
								InputKeys.ORGANIZATION);
				}
				else if(t.length == 1) {
					organization = UserValidators.validateOrganization(t[0]);
				}
				if(organization == null) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ORGANIZATION_VALUE,
							"Missing the required organization parameter: " + 
								InputKeys.ORGANIZATION);
				}
				
				// Get and validate the personal ID.
				String personalId = null;
				t = getParameterValues(InputKeys.PERSONAL_ID);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PERSONAL_ID_VALUE,
							"Multiple personal ID parameters were given: " +
								InputKeys.PERSONAL_ID);
				}
				else if(t.length == 1) {
					personalId = UserValidators.validatePersonalId(t[0]);
				}
				if(personalId == null) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PERSONAL_ID_VALUE,
							"Missing the required personal ID parameter: " + 
								InputKeys.PERSONAL_ID);
				}
				
				// Create the personal information.
				try {
					tPersonalInfo =
						new UserPersonal(
							firstName, 
							lastName, 
							organization, 
							personalId);
				}
				catch(DomainException e) {
					throw
						new ValidationException(
							"There was an error creating the personal " +
								"information.",
							e);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		personalInfo = tPersonalInfo;
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
			LOGGER.info("Verifying that the requesting user is an admin.");
			UserServices
				.instance()
				.verifyUserCanSetupUsers(getUser().getUsername());
			
			LOGGER.info("Determining if the user already exists.");
			UserInformation userInformation =
				UserServices
					.instance()
					.getUserInformationFromPersonalInformation(
						getUser().getUsername(),
						personalInfo);
			
			// If the user does not exist, create them and remember their
			// username.
			if(userInformation == null) {
				LOGGER.info("The user does not exist.");
				
				LOGGER.info("Generating a random username.");
				username = getRandomUsername();
				
				LOGGER.info("Generating a random password.");
				password = getRandomPassword();
				
				LOGGER.info("Retrieving the requesting user's email address.");
				String emailAddress =
					UserServices
						.instance()
						.getUserEmail(getUser().getUsername());
				
				UserServices
					.instance()
					.createUser(
						username,
						password,
						emailAddress,
						false,
						true,
						false,
						true);
			}
			// If the user does exist, store their username and the email
			// address associated with their account.
			else {
				LOGGER.info("The user already exists.");
				
				// Retrieve the user's username.
				username = userInformation.getUsername();
				// Store the requesting user's email address.
				this.emailAddress =
					UserServices.instance().getUserEmail(username);
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
			response.put("username", username);
			if(password != null) {
				response.put("password", password);
			}
			if(emailAddress != null) {
				response.put("email_address", emailAddress);
			}
			
			super.respond(httpRequest, httpResponse, response);
		}
		catch(JSONException e) {
			super.respond(httpRequest, httpResponse, (JSONObject) null);
		}
	}
	
	/**
	 * Creates a random username.
	 * 
	 * @return The random username.
	 * 
	 * @throws ServiceException There was a problem creating the username.
	 * 
	 * @see #USERNAME_PREFIX
	 * @see #USERNAME_DIGITS
	 */
	public String getRandomUsername() throws ServiceException {
		// Setup the zero array.
    	char[] zeros = new char[USERNAME_DIGITS];
    	Arrays.fill(zeros, '0');
		DecimalFormat format =
			new DecimalFormat(String.valueOf(zeros));
		int modifier = (new Double(Math.pow(10, USERNAME_DIGITS))).intValue();
		
		// Create usernames until we get one that has not yet been created.
		String username = null;
		do {
	    	int num =
	    		(new Double(Math.random() * modifier)).intValue() % modifier;
			username = USERNAME_PREFIX + format.format(num);
			
			try {
				LOGGER
					.info(
						"Checking the existance of the new user: " + username);
				UserServices.instance().checkUserExistance(username, false);
			}
			catch(ServiceException e) {
				username = null;
			}
		} while(username == null);
		
		// Return the username.
		return username;
	}
	
	/**
	 * Creates a new, random password for the user.
	 * 
	 * @return The new, random password.
	 * 
	 * @throws ServiceException
	 *         There was a problem generating the password.
	 */
	public String getRandomPassword() throws ServiceException {
		try {
			// Create the URL.
			URL url = new URL("http://makeagoodpassword.com/password/simple/");
			// Connect to the URL.
			HttpURLConnection connection =
				(HttpURLConnection) url.openConnection();
			// Make sure redirects are followed.
			connection.setInstanceFollowRedirects(true);
			
			// Make the request and check the response's status code.
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw
					new ServiceException(
						"The connection failed with the given status code: " +
							connection.getResponseCode());
			}
			
			// Read the response.
			BufferedReader reader =
				new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			return reader.readLine();
		}
		catch(MalformedURLException e) {
			throw new ServiceException(e);
		}
		catch(IOException e) {
			throw new ServiceException(e);
		}
	}
}