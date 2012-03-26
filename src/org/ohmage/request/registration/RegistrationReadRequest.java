package org.ohmage.request.registration;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.RegistrationConfig;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
import org.ohmage.service.RegistrationServices;

/**
 * This class returns the registration information to the requesting user. 
 * There are no required parameters.
 *
 * @author John Jenkins
 */
public class RegistrationReadRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(RegistrationReadRequest.class);
	
	private RegistrationConfig regInfo;
	
	/**
	 * Creates a new registration read request.
	 * 
	 * @param httpRequest The HttpServletRequest that began this request.
	 */
	public RegistrationReadRequest(final HttpServletRequest httpRequest) {
		super(httpRequest);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the registration read request.");
		
		try {
			regInfo = RegistrationServices.getRegistrationConfig();
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

		LOGGER.info("Responding to the registration read request.");
		
		JSONObject response = new JSONObject();
		try {
			if(! isFailed()) {
				response.put(JSON_KEY_DATA, regInfo.toJson());
			}
		}
		catch(JSONException e) {
			LOGGER.error("Error building the JSONObject.", e);
			setFailed();
		}
		finally {
			respond(httpRequest, httpResponse, response);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#getAuditInformation()
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		return new HashMap<String, String[]>();
	}
}