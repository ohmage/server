package org.ohmage.request.observer;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;
import org.ohmage.validator.ObserverValidators;

public class StreamReadRequest extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(StreamReadRequest.class);
	
	// Part of the URI.
	private final String observerId;
	
	// Optional.
	private final Long observerVersion;
	
	// Required parameters.
	private final String streamId;
	private final Long streamVersion;
	
	// Optional parameters.
	private final DateTime startDate;
	private final DateTime endDate;
	private final Collection<String> columns;
	
	private final int page;
	private final int pageSize;
	
	public StreamReadRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		String tObserverId = null;
		Long tObserverVersion = null;
		String tStreamId = null;
		Long tStreamVersion = null;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		Collection<String> tColumns = null;
		int tPage = -1;
		int tPageSize = -1;
		
		if(! isFailed()) {
			LOGGER.info("Creating a stream read request.");
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
			}
			catch(ValidationException e) {
				
			}
		}
		
		observerId = tObserverId;
		observerVersion = tObserverVersion;
		streamId = tStreamId;
		streamVersion = tStreamVersion;
		startDate = tStartDate;
		endDate = tEndDate;
		columns = tColumns;
		page = tPage;
		pageSize = tPageSize;
	}

	@Override
	public void service() {
		// TODO Auto-generated method stub

	}

	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {
		// TODO Auto-generated method stub

	}

}
