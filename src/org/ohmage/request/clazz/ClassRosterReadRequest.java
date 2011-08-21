package org.ohmage.request.clazz;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.UserBin;
import org.ohmage.dao.ClassDaos.UserAndClassRole;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Generates a class roster for an existing class. The user must be an admin
 * or they must be privileged in each of the classes that are requested to be
 * part of the roster.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes identifiers (URNs) separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassRosterReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassRosterReadRequest.class);

	private static final long MILLIS_IN_A_SECOND = 1000;
	
	private final List<String> classIds;
	
	private Map<String, List<UserAndClassRole>> roster;
	
	/**
	 * Creates a new class roster read request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the parameters
	 * 					  from the requester.
	 */
	public ClassRosterReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating a class roster read request.");
		
		List<String> tClassIds = null;
		
		try {
			tClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if(tClassIds == null) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Missing required class ID list: " + InputKeys.CLASS_URN_LIST);
				throw new ValidationException("Missing required class ID list: " + InputKeys.CLASS_URN_LIST);
			}
			else if(httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Multiple class ID lists were found.");
				throw new ValidationException("Multiple class ID lists were found.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		classIds = tClassIds;
		roster = null;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the class roster read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the classes in the class list exist.");
			ClassServices.checkClassesExistence(this, classIds, true);
			
			LOGGER.info("Verify that the user is an admin or that they are privileged in each of the classes in a list.");
			UserClassServices.userIsAdminOrPrivilegedInAllClasses(this, getUser().getUsername(), classIds);
			
			LOGGER.info("Generating the class roster.");
			roster = ClassServices.generateClassRoster(this, classIds);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Generates the class roster and returns it to the user as an attachment
	 * unless it fails in which case a JSON message will be returned not as an
	 * attachment.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the class roster read response.");
		
		// Creates the writer that will write the response, success or fail.
		Writer writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
		
		// Write an error message or the roster depending on if it failed or 
		// not.
		String responseText = "";
		if(isFailed()) {
			httpResponse.setContentType("text/html");
			
			// Use the annotator's message to build the response.
			responseText = getFailureMessage();
		}
		else {
			// Set the type and force the browser to download it as the 
			// last step before beginning to stream the response.
			httpResponse.setContentType("ohmage/roster");
			httpResponse.setHeader("Content-Disposition", "attachment; filename=roster.csv");
			
			// If available, set the token.
			if(getUser() != null) {
				final String token = getUser().getToken(); 
				if(token != null) {
					CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
				}
			}

			// Build the class roster as a CSV file.
			StringBuilder resultBuilder = new StringBuilder();
			for(String classId : roster.keySet()) {
				List<UserAndClassRole> userAndClassRoles = roster.get(classId);
				
				for(UserAndClassRole userAndClassRole : userAndClassRoles) {
					resultBuilder.append(classId).append(",").append(userAndClassRole.getUsername()).append(",").append(userAndClassRole.getRole()).append("\n");
				}
			}
			
			responseText = resultBuilder.toString();
		}
			
		// Write the error response.
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			LOGGER.error("Unable to write failed response message. Aborting.", e);
			return;
		}
		
		// Flush it and close.
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			LOGGER.error("Unable to flush or close the writer.", e);
		}
	}
	
	/**
	 * Returns the list of classes from the parameters.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		
		if(classIds != null) {
			result.put(InputKeys.CLASS_URN, classIds.toArray(new String[0]));
		}
		
		return result;
	}
}