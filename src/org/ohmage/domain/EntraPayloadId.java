package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadEntraRequest;

/**
 * The class that represents how Entra payload IDs are handled.
 *
 * @author John Jenkins
 */
public class EntraPayloadId implements PayloadId {
	private final String method;
	
	/**
	 * Creates a new Entra payload ID.
	 * 
	 * @param method The type of data requested.
	 * 
	 * @throws DomainException The method was null.
	 */
	public EntraPayloadId(final String method) throws DomainException {
		if(method == null) {
			throw new DomainException("The Entra method is null.");
		}
		
		this.method = method;
	}
	
	/**
	 * Returns the Entra method to call.
	 * 
	 * @return The Entra method to call.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Creates an Entra request.
	 * 
	 * @return An OmhReadEntraRequest object.
	 */
	@Override
	public OmhReadEntraRequest generateSubRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final long version,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {
		
		try {
			return
				new OmhReadEntraRequest(
					httpRequest, 
					parameters, 
					hashPassword,
					tokenLocation,
					callClientRequester,
					startDate, 
					endDate,
					numToSkip, 
					numToReturn,
					method);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the parameters.", 
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"There was a problem building the request.",
				e);
		}
	}

}
