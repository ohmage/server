package org.ohmage.request.observer;

import java.io.IOException;

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

public class ObserverUpdate extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(ObserverUpdate.class);
	
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
	public ObserverUpdate(
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
			ObserverServices.instance().verifyNewObserverIsValid(observer);
			
			LOGGER.info("Updating the observer.");
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	@Override
	public void respond(
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse) {
		// TODO Auto-generated method stub

	}
}