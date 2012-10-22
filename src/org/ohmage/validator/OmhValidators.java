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
		
		PayloadId result = null;
		
		String trimmedValue = value.trim();
		String[] split = trimmedValue.split(":");
		if(split.length < 4) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"It must contain at least 4 sections, " +
						"\"omh\", " +
						"\"ohmage\", " +
						"a type (\"campaign\" or \"observer\"), " +
						"and an ID for that type " +
						"each divided by a ':': " +
					trimmedValue);
		}
		
		// The first part must be "omh".
		if(! "omh".equals(split[0])) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The first section must be 'omh': " +
					trimmedValue);
		}
		
		// The second part must be "ohmage".
		String domain = split[1];
		if(! "ohmage".equals(domain)) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The domain, '" +
					domain +
					"' must be 'ohmage': " +
					trimmedValue);
		}

		String type = split[2];
		if("campaign".equals(type)) {
			// There have to be at least 7 parts for a 'campgign'-based payload
			// ID.
			if(split.length < 7) {
				throw
					new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The payload ID is too short for a 'campaign'-based payload ID.");
			}
			
			int surveyIdIndex;
			String surveyId;
			String promptId = null;
			
			// Get the second to last part.
			String lastIdType = split[split.length - 2];
			
			// If it is "survey_ID", then the last part must be the survey ID.
			if("survey_id".equals(lastIdType)) {
				surveyId = split[split.length - 1];
				surveyIdIndex = split.length - 2;
			}
			// If it is "prompt_id", then the last part must be the prompt ID
			// and the part just before it must be the survey ID.
			else if("prompt_id".equals(lastIdType)) {
				surveyId = split[split.length - 3];
				promptId = split[split.length - 1];
				surveyIdIndex = split.length - 4;
				
				if(! "survey_id".equals(split[surveyIdIndex])) {
					throw 
						new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_ID,
							"The 'campaign-based' payload ID is incorrectly formatted. " +
								"It must be of the form: " +
								"omh:ohmage:campaign:<campaign_id>:survey_id:<survey_id>[:prompt_id:<prompt_id>]");
				}
			}
			else {
				throw 
					new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The 'campaign-based' payload ID is incorrectly formatted. " +
							"It must be of the form: " +
							"omh:ohmage:campaign:<campaign_id>:survey_id:<survey_id>[:prompt_id:<prompt_id>]");
			}
			
			// Build the campaign ID.
			StringBuilder campaignIdBuilder = new StringBuilder();
			boolean firstPass = true;
			for(int i = 3; i < surveyIdIndex; i++) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					campaignIdBuilder.append(':');
				}
				campaignIdBuilder.append(split[i]);
			}
			
			try {
				result = 
					new CampaignPayloadId(
						campaignIdBuilder.toString(), 
						surveyId, 
						promptId);
			}
			catch(DomainException e) {
				throw new ValidationException(
					ErrorCode.SYSTEM_GENERAL_ERROR,
					"Could not construct the PayloadId object.",
					e);
			}
		}
		else if("observer".equals(type)) {
			String observerId;
			try {
				observerId = ObserverValidators.validateObserverId(split[3]);
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
				streamId = ObserverValidators.validateStreamId(split[4]);
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
		else if(result == null) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The type must either be 'campaign' or 'observer': " +
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
