package org.ohmage.request.mobility;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.request.UserRequest;
import org.ohmage.validator.ValidationException;

public class MobilityUploadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityUploadRequest.class);
	
	private final JSONObject data;
	
	/**
	 * Creates a Mobility upload request.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the parameters for
	 * 					  this request.
	 */
	public MobilityUploadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, false);
		
		LOGGER.debug("Creating a Mobility upload request.");
		
		JSONObject tData = null;
		
		if(! isFailed()) {
			try {
				tData = 
			}
			catch(ValidationException e) {
				
			}
		}
		
		data = tData;
	}

	@Override
	public void service() {
		// TODO Auto-generated method stub

	}

	@Override
	public void respond(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		// TODO Auto-generated method stub

	}

}
