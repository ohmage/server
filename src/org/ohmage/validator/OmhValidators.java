package org.ohmage.validator;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.BodyMediaPayloadId;
import org.ohmage.domain.CampaignPayloadId;
import org.ohmage.domain.EntraPayloadId;
import org.ohmage.domain.GingerIoPayloadId;
import org.ohmage.domain.HealthVaultPayloadId;
import org.ohmage.domain.MindMyMedsPayloadId;
import org.ohmage.domain.ObserverPayloadId;
import org.ohmage.domain.PayloadId;
import org.ohmage.domain.RunKeeperPayloadId;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.omh.OmhReadBodyMediaRequest.BodyMediaApi;
import org.ohmage.request.omh.OmhReadBodyMediaRequest.BodyMediaApiFactory;
import org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod;
import org.ohmage.request.omh.OmhReadEntraRequest.EntraMethodFactory;
import org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing;
import org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThingFactory;
import org.ohmage.request.omh.OmhReadRunKeeperRequest.RunKeeperApi;
import org.ohmage.request.omh.OmhReadRunKeeperRequest.RunKeeperApiFactory;
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
		if(split.length < 2) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"It must contain at least 2 sections, " +
					"\"omh\" and the domain, each divided by a ':': " +
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
			// This is the shim layer that will process the other types of 
			// requests. Normally, this would simply throw an exception, but,
			// for now, it will return the payload ID that corresponds to the
			// remote resource.
			if("run_keeper".equals(domain)) {
				try {
					result = new RunKeeperPayloadId(split[2]);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The RunKeeper API value is invalid: " + split[2],
						e);
				}
			}
			else if("body_media".equals(domain)) {
				try {
					result = new BodyMediaPayloadId(split[2]);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The RunKeeper API value is invalid: " + split[2],
						e);
				}
			}
			else if("mind_my_meds".equals(domain)) {
				result = new MindMyMedsPayloadId();
			}
			else if("entra".equals(domain)) {
				try {
					result = new EntraPayloadId(split[2]);
				}
				catch(IndexOutOfBoundsException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The payload ID for the Entra domain requires a third section indicating the method: " +
							trimmedValue,
						e);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The Entra method is invalid: " + split[2],
						e);
				}
			}
			else if("health_vault".equals(domain)) {
				try {
					result = new HealthVaultPayloadId(split[2]);
				}
				catch(IndexOutOfBoundsException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The payload ID for the HealthVault domain requires a third section indicating the type of data desired: " +
							trimmedValue,
						e);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"The HealthVault method is invalid: " + split[2],
						e);
				}
			}
			else if("ginger_io".equals(domain)) {
				result = new GingerIoPayloadId();
			}
			else {
				throw new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is not valid. " +
						"The domain, '" +
						domain +
						"' is unknown: " +
						trimmedValue);
			}
		}

		String type = (result == null) ? split[2] : null;
		if((result == null) && "campaign".equals(type)) {
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
		else if((result == null) && "observer".equals(type)) {
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
	
	/**
	 * Validates that the API is known.
	 * 
	 * @param value The path string to use to create a {@link RunKeeperApi}
	 * 				object.
	 * 
	 * @return The {@link RunKeeperApi} object that matches the path String.
	 * 
	 * @throws ValidationException The path string doesn't reference any known
	 * 							   {@link RunKeeperApi}.
	 */
	public static RunKeeperApi validateRunKeeperApi(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return RunKeeperApiFactory.getApi(value);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The path is invalid: " + value,
				e);
		}
	}
	
	/**
	 * Validates that the API is known.
	 * 
	 * @param value The path string to use to create a {@link BodyMediaApi}
	 * 				object.
	 * 
	 * @return The {@link BodyMediaApi} object that matches the path String.
	 * 
	 * @throws ValidationException The path string doesn't reference any known
	 * 							   {@link BodyMediaApi}.
	 */
	public static BodyMediaApi validateBodyMediaApi(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return BodyMediaApiFactory.getApi(value);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The path is invalid: " + value,
				e);
		}
	}
	
	/**
	 * Validates that the method is known.
	 * 
	 * @param value The path string to use to create a {@link EntraMethod}
	 * 				object.
	 * 
	 * @return The {@link EntraMethod} object that matches the path String.
	 * 
	 * @throws ValidationException The path string doesn't reference any known
	 * 							   {@link EntraMethod}.
	 */
	public static EntraMethod validateEntraMethod(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return EntraMethodFactory.getMethod(value);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The path is invalid: " + value,
				e);
		}
	}
	
	/**
	 * Validates that the thing is known.
	 * 
	 * @param value The thing name to use to create a {@link HealthVaultThing}
	 * 				object.
	 * 
	 * @return The {@link HealthVaultThing} object that matches the thing name.
	 * 
	 * @throws ValidationException The thing name doesn't reference any known
	 * 							   {@link HealthVaultThing}.
	 */
	public static HealthVaultThing validateHealthVaultThing(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return HealthVaultThingFactory.getThing(value);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The path is invalid: " + value,
				e);
		}
	}
}
