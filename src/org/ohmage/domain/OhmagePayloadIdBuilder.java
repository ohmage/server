package org.ohmage.domain;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;

/**
 * <p>
 * The {@link PayloadIdBuilder} used to handle all of the ohmage sub-types.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmagePayloadIdBuilder implements PayloadIdBuilder {
	/**
	 * Default constructor.
	 */
	public OhmagePayloadIdBuilder() {}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.PayloadIdBuilder#build(java.lang.String[])
	 */
	@Override
	public PayloadId build(
		final String[] payloadIdParts)
		throws ValidationException{
		
		if(payloadIdParts.length < 3) {
			throw
				new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"An ohmage payload ID must have at least 3 parts where " +
						"the 3rd part is the either 'campgin' or 'observer'.");
		}
		
		PayloadId result;
		String payloadIdType = payloadIdParts[2];
		if("campaign".equals(payloadIdType)) {
			result = new CampaignPayloadId(payloadIdParts);
		}
		else if("observer".equals(payloadIdType)) {
			result = new ObserverPayloadId(payloadIdParts);
		}
		else {
			throw
				new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The ohmage sub-type is unknown: " + payloadIdType);
		}
		
		return result;
	}
}