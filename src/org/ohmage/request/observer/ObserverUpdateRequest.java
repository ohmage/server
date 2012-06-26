package org.ohmage.request.observer;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;

/**
 * <p>Updates an existing observer in the system.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#OBSERVER_DEFINITION}</td>
 *     <td>The definition of the observer as an XML document.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ObserverUpdateRequest extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(ObserverUpdateRequest.class);
	
	private final Observer observer;
	
	/**
	 * Creates a observer update request.
	 * 
	 * @param httpRequest The HTTP servlet request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public ObserverUpdateRequest(
			final HttpServletRequest httpRequest)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, false, TokenLocation.PARAMETER, null);
		
		Observer tObserver = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating an observer update request.");
			
			try {
				byte[] observerDefinitionBytes =
					getMultipartValue(
						httpRequest,
						InputKeys.OBSERVER_DEFINITION);
				if(observerDefinitionBytes == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_DEFINITION,
						"The observer definition is missing: " +
							InputKeys.OBSERVER_DEFINITION);
				}
				else {
					tObserver =
						ObserverValidators.validateObserverDefinitionXml(
							new String(observerDefinitionBytes));
				}
				if(tObserver == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_DEFINITION,
						"The observer definition is missing: " +
							InputKeys.OBSERVER_DEFINITION);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		observer = tObserver;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a observer creation request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is allowed to update an existing observer.");
			ObserverServices.instance().verifyUserCanUpdateObserver(
				getUser().getUsername(), 
				observer.getId());
			
			LOGGER.info("Verifying that the update is valid.");
			Map<String, Long> unchangedStreamIds =
				ObserverServices.instance().verifyNewObserverIsValid(observer);
			
			LOGGER.info("Updating the observer.");
			ObserverServices.instance().updateObserver(
				getUser().getUsername(),
				observer, 
				unchangedStreamIds);
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

		super.respond(httpRequest, httpResponse, null);
	}
}