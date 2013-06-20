package org.ohmage.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.request.survey.SurveyResponseRequest;
import org.ohmage.request.survey.SurveyUploadRequest;

/**
 * This class represents a campaign-specific payload ID.
 *
 * @author John Jenkins
 */
public class CampaignPayloadId implements PayloadId {
	private static final JsonFactory JSON_FACTORY = 
		// Create a mapping JSON factory.
		(new MappingJsonFactory())
			// Ask the writer to always close the content even when there is an
			// error.
			.configure(Feature.AUTO_CLOSE_JSON_CONTENT, true);
	
	private final String campaignId;
	private final String surveyId;
	private final String promptId;
	
	/**
	 * Defines a campaign payload ID that contains a campaign ID and survey ID
	 * and, optionally, a prompt ID.
	 * 
	 * @param campaignId The campaign ID.
	 * 
	 * @param surveyId The survey ID.
	 * 
	 * @param promptId The prompt ID. Optionally, null, but not only 
	 * 				   whitespace.
	 * 
	 * @throws DomainException The campaign ID or survey ID is null or 
	 * 						   whitespace only or the prompt ID is whitespace
	 * 						   only.
	 */
	public CampaignPayloadId(
		final String[] campaignPayloadParts)
		throws ValidationException {
		
		// There have to be at least 7 parts for a 'campgign'-based payload
		// ID.
		if(campaignPayloadParts.length < 7) {
			throw
				new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID is too short for a 'campaign'-based payload ID.");
		}
		
		int surveyIdIndex;
		
		// Get the second to last part.
		String lastIdType =
			campaignPayloadParts[campaignPayloadParts.length - 2];
		
		// If it is "survey_ID", then the last part must be the survey ID.
		if("survey_id".equals(lastIdType)) {
			surveyId = campaignPayloadParts[campaignPayloadParts.length - 1];
			surveyIdIndex = campaignPayloadParts.length - 2;
			promptId = null;
		}
		// If it is "prompt_id", then the last part must be the prompt ID
		// and the part just before it must be the survey ID.
		else if("prompt_id".equals(lastIdType)) {
			surveyId = campaignPayloadParts[campaignPayloadParts.length - 3];
			promptId = campaignPayloadParts[campaignPayloadParts.length - 1];
			surveyIdIndex = campaignPayloadParts.length - 4;
			
			if(! "survey_id".equals(campaignPayloadParts[surveyIdIndex])) {
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
			campaignIdBuilder.append(campaignPayloadParts[i]);
		}
		campaignId = campaignIdBuilder.toString();
	}

	/**
	 * Returns the campaign ID.
	 * 
	 * @return The campaign ID.
	 */
	public String getCampaignId() {
		return campaignId;
	}
	
	/**
	 * Returns the survey ID.
	 * 
	 * @return The survey ID.
	 */
	public String getSurveyId() {
		return surveyId;
	}
	
	/**
	 * Returns the prompt ID.
	 * 
	 * @return The prompt ID, which may be null.
	 */
	public String getPromptId() {
		return promptId;
	}

	/**
	 * Creates a survey_response/read request.
	 * 
	 * @return A survey_response/read request.
	 */
	@Override
	public SurveyResponseReadRequest generateReadRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			boolean callClientRequester,
			final long version,
			final String owner,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {
		
		Collection<String> usernames = 
			SurveyResponseRequest.URN_SPECIAL_ALL_LIST;
		if(owner != null) {
			usernames = new ArrayList<String>(1);
			usernames.add(owner);
		}
		
		Collection<String> surveyIds = null;
		Collection<String> promptIds = null;
		if(promptId == null) {
			surveyIds = new ArrayList<String>(1);
			surveyIds.add(surveyId);
		}
		else {
			promptIds = new ArrayList<String>(1);
			promptIds.add(promptId);
		}
		
		try {
			return
				new SurveyResponseReadRequest(
					httpRequest,
					parameters,
					true,
					campaignId,
					usernames,
					surveyIds,
					promptIds,
					null,
					startDate,
					endDate,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					numToSkip,
					numToReturn);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the HTTP request.",
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"Error parsing the parameters.",
				e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
				"One of the parameters was invalid.",
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.PayloadId#generateWriteRequest(javax.servlet.http.HttpServletRequest, java.util.Map, java.lang.Boolean, org.ohmage.request.UserRequest.TokenLocation, boolean, long, java.lang.String)
	 */
	@Override
	public UserRequest generateWriteRequest(
		final HttpServletRequest httpRequest,
		final Map<String, String[]> parameters,
		final Boolean hashPassword,
		final TokenLocation tokenLocation,
		final boolean callClientRequester,
		final long version,
		final String data)
		throws DomainException {
		
		// We don't allow writes for specific prompts. The user is only allowed
		// to write to surveys.
		if(promptId != null) {
			throw
				new DomainException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"Write is disallowed for prompt-specific payload IDs.");
		}
		
		// Create a parser for the desired data.
		JsonNode dataNode;
		try {
			dataNode =
				JSON_FACTORY.createJsonParser(data).readValueAsTree();
		}
		catch(JsonProcessingException e) {
			throw
				new DomainException(
					ErrorCode.OMH_INVALID_DATA,
					"The data was not valid JSON.",
					e);
		}
		catch(IOException e) {
			throw new DomainException("Could not read the uploaded data.", e);
		}
		
		// Be sure that it is an array.
		if(! dataNode.isArray()) {
			throw
				new DomainException(
					ErrorCode.OMH_INVALID_DATA,
					"The data is not an array.");
		}
		
		// The writer that will be used to write the result.
		StringWriter resultWriter = new StringWriter();
		
		// Create a new JSON array to hold the rest of the data.
		JsonGenerator generator = null;
		try {
			// Create a generator to generate the results for our writer.
			generator = JSON_FACTORY.createJsonGenerator(resultWriter);
			
			// Start the array of points.
			generator.writeStartArray();
			
			// Cycle through the objects
			int numElements = dataNode.size();
			for(int i = 0; i < numElements; i++) {
				// Get the current point.
				JsonNode currPoint = dataNode.get(i);
				
				// Make sure the current point is an object.
				if(! currPoint.isObject()) {
					throw
						new DomainException(
							ErrorCode.OMH_INVALID_DATA,
							"A data point is not an object.");
				}
				
				// Write the beginning of this point.
				generator.writeStartObject();
				
				// Get the metadata and validate that it is an object or null.
				JsonNode metadata = currPoint.get("metadata");
				if(metadata == null) {
					throw
						new DomainException(
							ErrorCode.OMH_INVALID_DATA,
							"The metadata is missing.");
				}
				else if(! metadata.isObject()) {
					throw 
						new DomainException(
							ErrorCode.OMH_INVALID_DATA,
							"The metadata for a data point is not an object.");
				}
				else {	
					// Get the ID and save it in the new point.
					JsonNode id = metadata.get("id");
					if(id == null) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The required ID is missing.");
					}
					else if(! id.isTextual()) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The ID is not a string.");
					}
					else {
						generator
							.writeStringField(
								"survey_key",
								id.getTextValue());
					}
					
					// Get the timestamp and save it in the new point.
					JsonNode timestampNode = metadata.get("timestamp");
					if(timestampNode == null) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The required timestamp is missing.");
					}
					else if(! timestampNode.isTextual()) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The timestamp is not a string.");
					}
					else {
						DateTime timestamp;
						try {
							timestamp =
								ISOW3CDateTimeFormat
									.any()
										.parseDateTime(
											timestampNode.getTextValue());
						}
						catch(IllegalArgumentException e) {
							throw
								new DomainException(
									ErrorCode.OMH_INVALID_DATA,
									"The timestamp is malformed.");
						}
						
						generator
							.writeNumberField(
								"time",
								timestamp.getMillis());
						generator
							.writeStringField(
								"timezone",
								timestamp
									.getZone()
										.getName(timestamp.getMillis()));
					}
					
					// Get the location and save it in the new point.
					JsonNode location = metadata.get("location");
					if(location == null) {
						generator
							.writeStringField(
								"location_status",
								"unavailable");
					}
					else if(! location.isObject()) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The location is not an object.");
					}
					else {
						generator
							.writeStringField(
								"location_status",
								"valid");
						generator
							.writeObjectField(
								"location",
								location);
					}
				}
				
				// Add the survey ID.
				generator.writeStringField("survey_id", surveyId);
				
				// Get the data and save it in the new point as is.
				JsonNode pointData = currPoint.get("data");
				if(pointData == null) {
					throw
						new DomainException(
							ErrorCode.OMH_INVALID_DATA,
							"The data is missing.");
				}
				else {
					// Pull out the survey launch context and put it in the
					// result.
					JsonNode launchContext =
						pointData
							.get(
								SurveyResponse.JSON_KEY_SURVEY_LAUNCH_CONTEXT);
					if(launchContext == null) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA, 
								"The launch context is missing.");
					}
					else if(! launchContext.isObject()) {
						throw 
							new DomainException(
								ErrorCode.OMH_INVALID_DATA, 
								"The launch context was not an object.");
					}
					else {
						generator
							.writeObjectField(
								SurveyResponse.JSON_KEY_SURVEY_LAUNCH_CONTEXT,
								launchContext);
					}
					
					// Get the responses array and put it in the result.
					JsonNode responses = 
						pointData.get(SurveyResponse.JSON_KEY_RESPONSES);
					if(responses == null) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The responses were missing.");
					}
					else if(! responses.isArray()) {
						throw
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The responses value is not an array.");
					}
					else {
						generator
							.writeObjectField(
								SurveyResponse.JSON_KEY_RESPONSES,
								responses);
					}
				}
				
				// Close this point.
				generator.writeEndObject();
			}
			
			// End the overall array.
			generator.writeEndArray();
		}
		// This should never happen as we are always writing to our own writer.
		catch(IOException e) {
			throw
				new DomainException(
					"Could not write to my own string writer.",
					e);
		}
		finally {
			// Always be sure to close the generator as long as it was created.
			// This will ensure that any unclosed arrays or objects are closed.
			if(generator != null) {
				try {
					generator.close();
				}
				catch(IOException e) {
					// This will only ever happen if it cannot close the 
					// underlying stream, which will never happen, or if an
					// error already took place. 
				}
			}
		}
		
		try {
			return
				new SurveyUploadRequest(
					httpRequest,
					parameters,
					campaignId,
					resultWriter.toString(),
					null);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the HTTP request.",
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"Error parsing the parameters.",
				e);
		}
	}
}
