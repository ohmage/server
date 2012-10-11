package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadRunKeeperRequest;
import org.ohmage.util.StringUtils;

/**
 * This class represents a payload ID for Run Keeper.
 *
 * @author John Jenkins
 */
public class RunKeeperPayloadId implements PayloadId {
	private final String api;
	
	/**
	 * Creates a payload ID that contains a type representing the type of 
	 * information desired, e.g. "profile", "sleep", "weight", etc..
	 * 
	 * @param api The type of information desired.
	 * 
	 * @throws DomainException The API value is null or whitespace.
	 */
	public RunKeeperPayloadId(final String api) throws DomainException {
		if(StringUtils.isEmptyOrWhitespaceOnly(api)) {
			throw new DomainException("The ID is missing or whitespace.");
		}
		
		this.api = api;
	}
	
	/**
	 * The type of information desired, e.g. "profile", "sleep", "weight", 
	 * etc..
	 * 
	 * @return The API name.
	 */
	public String getApi() {
		return api;
	}

	/**
	 * Returns an OMH read request for Run Keeper.
	 * 
	 * @return An OmhReadMoodMapRequest object.
	 */
	@Override
	public OmhReadRunKeeperRequest generateSubRequest(
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
				new OmhReadRunKeeperRequest(
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
					api);
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
