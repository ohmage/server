package org.ohmage.domain;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;

/**
 * <p>
 * The {@link PayloadId} used to handle all of the ohmage sub-types.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmagePayloadId implements PayloadId {
	/**
	 * The internal {@link PayloadId} that will be used to dispatch all of the
	 * calls.
	 */
	private final PayloadId subPayloadId;

	/**
	 * Creates an internal payload ID that is either for "campaign" or
	 * "observer"s and uses that for the subsequen calls.
	 * 
	 * @param ohmagePayloadId
	 *        The payload ID parts to use to build a {@link PayloadId}.
	 * 
	 * @throws ValidationException
	 *         The payload ID parts are invalid.
	 */
	public OhmagePayloadId(
		final String[] ohmagePayloadId)
		throws ValidationException {
		
		if(ohmagePayloadId.length < 3) {
			throw
				new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"An ohmage payload ID must have at least 3 parts where " +
						"the 3rd part is the either 'campgin' or 'observer'.");
		}
		
		String payloadIdType = ohmagePayloadId[2];
		if("campaign".equals(payloadIdType)) {
			subPayloadId = new CampaignPayloadId(ohmagePayloadId);
		}
		else if("observer".equals(payloadIdType)) {
			subPayloadId = new ObserverPayloadId(ohmagePayloadId);
		}
		else {
			throw
				new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The ohmage sub-type is unknown: " + payloadIdType);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.PayloadId#generateReadRequest(javax.servlet.http.HttpServletRequest, java.util.Map, java.lang.Boolean, org.ohmage.request.UserRequest.TokenLocation, boolean, long, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
	 */
	@Override
	public UserRequest generateReadRequest(
		HttpServletRequest httpRequest,
		Map<String, String[]> parameters,
		Boolean hashPassword,
		TokenLocation tokenLocation,
		boolean callClientRequester,
		long version,
		String owner,
		DateTime startDate,
		DateTime endDate,
		long numToSkip,
		long numToReturn) throws DomainException {
		
		return
			subPayloadId
				.generateReadRequest(
					httpRequest,
					parameters,
					hashPassword,
					tokenLocation,
					callClientRequester,
					version,
					owner,
					startDate,
					endDate,
					numToSkip,
					numToReturn);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.PayloadId#generateWriteRequest(javax.servlet.http.HttpServletRequest, java.util.Map, java.lang.Boolean, org.ohmage.request.UserRequest.TokenLocation, boolean, long, java.lang.String)
	 */
	@Override
	public UserRequest generateWriteRequest(
		HttpServletRequest httpRequest,
		Map<String, String[]> parameters,
		Boolean hashPassword,
		TokenLocation tokenLocation,
		boolean callClientRequester,
		long version,
		String data) throws DomainException {
		
		return
			subPayloadId
				.generateWriteRequest(
					httpRequest,
					parameters,
					hashPassword,
					tokenLocation,
					callClientRequester,
					version,
					data);
	}
}