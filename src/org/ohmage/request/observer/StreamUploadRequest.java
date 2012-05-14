package org.ohmage.request.observer;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.Observer;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;

public class StreamUploadRequest extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(StreamUploadRequest.class);

	private final String observerId;
	private final Long observerVersion;
	private final Collection<JSONObject> data;
	
	public StreamUploadRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER, false);
		
		String tObserverId = null;
		Long tObserverVersion = null;
		Collection<JSONObject> tData = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a stream upload request.");
			String[] t;
			
			try {
				String[] uriParts = httpRequest.getRequestURI().split("/");
				
				tObserverId = 
					ObserverValidators.validateObserverId(
						uriParts[uriParts.length - 2]);
				if(tObserverId == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_ID,
						"The observer's ID is missing.");
				}
				
				tObserverVersion = 
					ObserverValidators.validateObserverVersion(
						uriParts[uriParts.length - 1]);
				if(tObserverVersion == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_VERSION,
						"The observer's version is missing.");
				}
				
				t = getParameterValues(InputKeys.DATA);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_STREAM_DATA,
						"Multiple data streams were uploaded: " + 
							InputKeys.DATA);
				}
				else if(t.length == 1) {
					tData = ObserverValidators.validateData(t[0]);
				}
				if(tData == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_STREAM_DATA,
						"The data was missing: " + InputKeys.DATA);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		observerId = tObserverId;
		observerVersion = tObserverVersion;
		data = tData;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a stream upload request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Getting the observer definition.");
			Observer observer = 
				ObserverServices.instance().getObserver(
					observerId, 
					observerVersion);
			
			LOGGER.info("Validating the uploaded data.");
			Collection<DataStream> dataStreams =
				ObserverServices.instance().validateData(observer, data);
			
			LOGGER.info("Storing the uploaded data.");
			ObserverServices.instance().storeData(
				getUser().getUsername(), 
				dataStreams);
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
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		
		super.respond(httpRequest, httpResponse, null);
	}
}