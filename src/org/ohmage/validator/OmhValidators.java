package org.ohmage.validator;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.CampaignPayloadId;
import org.ohmage.domain.ObserverPayloadId;
import org.ohmage.domain.PayloadId;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * Class for validating OMH parameters.
 *
 * @author John Jenkins
 */
public class OmhValidators {
	/**
	 * Default constructor made private as the static methods should be used.
	 */
	private OmhValidators() {}
	
	/**
	 * Validates that a payload ID is valid and returns a PayloadId object that
	 * represents that payload ID.
	 * 
	 * @param value The payload ID to validate.
	 * 
	 * @return A PayloadId representing the payload ID.
	 * 
	 * @throws ValidationException The payload ID is not valid.
	 */
	public static PayloadId validatePayloadId(
			final String value) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		String trimmedValue = value.trim();
		String[] split = trimmedValue.split(":");
		if(split.length < 4) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"It must contain at least 4 sections, each divided by a ':': " +
					trimmedValue);
		}
		
		// The first part must be "omh".
		if("omh".equals(split[0])) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The first section must be 'omh': " +
					trimmedValue);
		}
		
		// The second part must be "ohmage".
		if("ohmage".equals(split[1])) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The second section must be 'ohmage': " +
					trimmedValue);
		}

		PayloadId result;
		String type = split[2];
		if("campaign".equals(type)) {
			String campaignId;
			try {
				campaignId = CampaignValidators.validateCampaignId(split[3]);
			}
			catch(ValidationException e) {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid because the campaign ID is not valid: " +
						split[3],
					e);
			}
			if(campaignId == null) {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid because the campaign ID was only whitespace: " +
						split[3]);
			}
			
			if(split.length > 4) {
				if(split.length != 6) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"A payload ID that defines a campaign may have exactly two additional parameters. " +
							"The first must be either 'survey_id' or 'prompt_id' and the second must be the respective value.");
				}
				
				CampaignPayloadId.Type subType;
				try {
					subType = CampaignPayloadId.Type.getType(split[4]);
				}
				catch(IllegalArgumentException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The payload ID's campaign sub-type is unknown: " +
							split[4],
						e);
				}
				
				String subId; 
				try {
					if(CampaignPayloadId.Type.SURVEY.equals(subType)) {
						subId = CampaignValidators.validateSurveyId(split[5]);
					}
					else {
						subId = CampaignValidators.validatePromptId(split[5]);
					}
				}
				catch(ValidationException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The payload ID's sub-value is invalid: " +
							e.getMessage(),
						e);
				}
				if(subId == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The payload ID is not valid because the sub-ID was only whitespace: " +
							split[5]);
				}
				
				try {
					result = new CampaignPayloadId(campaignId, subType, subId);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.SYSTEM_GENERAL_ERROR,
						"Could not construct the PayloadId object.",
						e);
				}
			}
			else {
				try {
					result = new CampaignPayloadId(campaignId);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.SYSTEM_GENERAL_ERROR,
						"Could not construct the PayloadId object.",
						e);
				}
			}
		}
		else if("observer".equals(type)) {
			String observerId;
			try {
				observerId = ObserverValidators.validateObserverId(split[2]);
			}
			catch(ValidationException e) {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid. " +
						"The third section must be a valid observer ID: " +
						trimmedValue);
			}
			if(observerId == null) {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid. " +
						"The third section is empty: " +
						trimmedValue);
			}
			
			String streamId;
			try {
				streamId = ObserverValidators.validateStreamId(split[3]);
			}
			catch(ValidationException e) {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid. " +
						"The forth section must be a valid stream ID: " +
						trimmedValue);
			}
			if(streamId == null) {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid. " +
						"The forth section is empty: " +
						trimmedValue);
			}
			
			try {
				result = new ObserverPayloadId(observerId, streamId);
			}
			catch(DomainException e) {
				throw new ValidationException(
					ErrorCode.SYSTEM_GENERAL_ERROR,
					"Could not construct the PayloadId object.",
					e);
			}
		}
		else {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The third section must either be 'campaign' or 'observer': " +
					trimmedValue);
		}

		return result;
	}
	
	/**
	 * Validates that a payload version string is a valid payload version and
	 * then returns it.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return The long value representation of the version.
	 * 
	 * @throws ValidationException The version was not a valid number.
	 */
	public static Long validatePayloadVersion(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return Long.decode(value);
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
				"The payload version is not a whole number: " +
					value);
		}
	}
}
