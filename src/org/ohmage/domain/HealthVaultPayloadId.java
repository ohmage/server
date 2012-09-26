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
	private final String id;
	
	/**
	 * Creates a new HealthVault payload ID.
	 * 
	 * @param id The type of data desired from HealthVault.
	 * 
	 * @throws DomainException The ID was null.
	 */
	public HealthVaultPayloadId(final String id) throws DomainException {
		if(id == null) {
			throw new DomainException("The HealthVault type is null.");
		}
		
		this.id = id;
	}
	
	/**
	 * @return The HealthVault data type desired.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @return Always returns null.
	 */
	@Override
	public String getSubId() {
		return null;
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
					startDate,
					endDate,
					numToSkip,
					numToReturn,
					id);
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
