package org.ohmage.request.probe;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;

public class ProbeCreationRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(ProbeCreationRequest.class);
	
	public ProbeCreationRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER, false);
		
		if(! isFailed()) {
			LOGGER.info("Creating a probe creation request.");
			
			try {
				throw new ValidationException("boo");
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
	}

	@Override
	public void service() {
		// TODO Auto-generated method stub

	}

	@Override
	public void respond(
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse) {
		// TODO Auto-generated method stub

	}

}
