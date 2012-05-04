package org.ohmage.request.probe;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Probe;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.validator.ProbeValidators;

public class ProbeCreationRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(ProbeCreationRequest.class);
	
	private final Probe probe;
	
	public ProbeCreationRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER, false);
		
		Probe tProbe = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a probe creation request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.PROBE_DEFINITION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.PROBE_INVALID_DEFINITION,
						"Multiple probe definitions were given: " +
							InputKeys.PROBE_DEFINITION);
				}
				else if(t.length == 1) {
					tProbe = ProbeValidators.validateProbeDefinitionXml(t[0]);
				}
				if(tProbe == null) {
					throw new ValidationException(
						ErrorCode.PROBE_INVALID_DEFINITION,
						"The probe definition is missing: " +
							InputKeys.PROBE_DEFINITION);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		probe = tProbe;
	}

	@Override
	public void service() {
		LOGGER.info("Servicing a probe creation request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Storing the new probe.");
			ProbeServices.createProbe(probe);
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
	
		super.respond(httpRequest, httpResponse, null);
	}

}
