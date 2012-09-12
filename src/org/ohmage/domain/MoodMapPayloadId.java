package org.ohmage.domain;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.omh.OmhReadMoodMapRequest;

/**
 * This class represents a Mood Map payload ID. This is used when retrieving 
 * the data from a remote source. When it is being retrieved as a survey 
 * response, the {@link org.ohmage.domain.CampaignPayloadId CampaignPayloadId}
 * should be used.
 *
 * @author John Jenkins
 */
public class MoodMapPayloadId implements PayloadId {
	/**
	 * There are not IDs associated with a mood map payload.
	 */
	@Override
	public String getId() {
		return null;
	}

	/**
	 * There are no sub-IDs associated with a mood map payload.
	 */
	@Override
	public String getSubId() {
		return null;
	}

	/**
	 * Generates an OMH read for a mood map reading.
	 * 
	 * @return An OmhReadMoodMapRequest.
	 */
	@Override
	public OmhReadMoodMapRequest generateSubRequest(
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
				new OmhReadMoodMapRequest(
					httpRequest, 
					parameters, 
					hashPassword,
					tokenLocation,
					callClientRequester,
					startDate, 
					endDate,
					numToSkip, 
					numToReturn);
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