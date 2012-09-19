package org.ohmage.domain;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;

public class EntraPayloadId implements PayloadId {
	private final String id;
	
	public EntraPayloadId(final String id) throws DomainException {
		if(id == null) {
			throw new DomainException("The Entra method is null.");
		}
		
		this.id = id;
	}
	
	/**
	 * @return The Entra method to call.
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
		
		return null;
	}

}
