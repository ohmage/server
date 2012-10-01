package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadHealthVaultRequest;

/**
 * The class that represents how HealthVault payload IDs are handled.
 *
 * @author John Jenkins
 */
public class HealthVaultPayloadId implements PayloadId {
	private final String thingName;
	
	/**
	 * Creates a new HealthVault payload ID.
	 * 
	 * @param thingName The type of data desired from HealthVault.
	 * 
	 * @throws DomainException The ID was null.
	 */
	public HealthVaultPayloadId(
			final String thingName)
			throws DomainException {
		
		if(thingName == null) {
			throw new DomainException("The HealthVault type is null.");
		}
		
		this.thingName = thingName;
	}
	
	/**
	 * Returns the HealthVault Thing name.
	 * 
	 * @return The HealthVault Thing name.
	 */
	public String getThingName() {
		return thingName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.PayloadId#generateSubRequest(javax.servlet.http.HttpServletRequest, java.util.Map, java.lang.Boolean, org.ohmage.request.UserRequest.TokenLocation, boolean, long, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
	 */
	@Override
	public UserRequest generateSubRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final long version,
			final String owner,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {
		
		try {
			return 
				new OmhReadHealthVaultRequest(
					httpRequest,
					parameters,
					hashPassword,
					tokenLocation,
					callClientRequester,
					owner,
					startDate,
					endDate,
					numToSkip,
					numToReturn,
					thingName);
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
