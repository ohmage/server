package org.ohmage.request.omh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.CampaignPayloadId;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.domain.ObserverPayloadId;
import org.ohmage.domain.PayloadId;
import org.ohmage.domain.RunKeeperPayloadId;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Message;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.Survey;
import org.ohmage.domain.campaign.SurveyItem;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.omh.OmhReadRunKeeperRequest.RunKeeperApiFactory;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;
import org.ohmage.validator.OmhValidators;

public class OmhRegistryReadRequest extends Request {
	private static final Logger LOGGER =
		Logger.getLogger(OmhRegistryReadRequest.class);
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	private final PayloadId payloadId;
	private final Long payloadVersion;
	
	private final long numToSkip;
	private final long numToReturn;

	private final Map<String, Collection<Stream>> streams = 
		new HashMap<String, Collection<Stream>>();
	private final Collection<Campaign> campaigns = new ArrayList<Campaign>();
	
	/**
	 * Creates an OMH registry read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhRegistryReadRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, null);
		
		PayloadId tPayloadId = null;
		Long tPayloadVersion = null;
		
		long tNumToSkip = 0;
		long tNumToReturn = StreamReadRequest.MAX_NUMBER_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH registry read request.");
			String[] t;
			
			try {
				tPayloadId = null;
				t = getParameterValues(InputKeys.OMH_PAYLOAD_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"Multiple payload IDs were given: " +
							InputKeys.OMH_PAYLOAD_ID);
				}
				else if(t.length == 1) {
					tPayloadId = OmhValidators.validatePayloadId(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_PAYLOAD_VERSION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
						"Multiple payload versions were given: " +
							InputKeys.OMH_PAYLOAD_VERSION);
				}
				else if(t.length == 1) {
					tPayloadVersion = 
						OmhValidators.validatePayloadVersion(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_NUM_TO_SKIP,
						"Multiple \"number of results to skip\" values were given: " +
							InputKeys.OMH_NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tNumToSkip = ObserverValidators.validateNumToSkip(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_NUM_TO_RETURN,
						"Multiple \"number of results to return\" values were given: " +
							InputKeys.OMH_NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tNumToReturn = 
						ObserverValidators
							.validateNumToReturn(
								t[0], 
								StreamReadRequest.MAX_NUMBER_TO_RETURN);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		payloadId = tPayloadId;
		payloadVersion = tPayloadVersion;
		
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH registry read request.");
		
		try {
			// If the user isn't specifically asking for campaigns, then either
			// they are asking for a specific observer or they want everything 
			// from both observers and campaigns.
			if(! (payloadId instanceof CampaignPayloadId)) {
				LOGGER.info("Gathering the requested observer registry entries.");
				streams
					.putAll(
						ObserverServices
							.instance()
							.getStreams(
								null,
								(payloadId == null) ? null : payloadId.getId(), 
								null, 
								(payloadId == null) ? null : payloadId.getSubId(), 
								payloadVersion, 
								numToSkip, 
								numToReturn));
				LOGGER.info("Found " + streams.size() + " streams.");
			}
			
			// If the user isn't specifically asking for observers, then either
			// they are asking for a specific campaign or they want everything
			// from both observers and campaigns.
			if(! (payloadId instanceof ObserverPayloadId)) {
				List<String> campaignIds = null;
				List<String> surveyIds = null;
				List<String> promptIds = null;
				if(payloadId instanceof CampaignPayloadId) {
					CampaignPayloadId campaignPayloadId = 
						(CampaignPayloadId) payloadId;
					
					campaignIds = new ArrayList<String>(1);
					campaignIds.add(campaignPayloadId.getId());
					
					CampaignPayloadId.Type type = campaignPayloadId.getType();
					if(CampaignPayloadId.Type.SURVEY.equals(type)) {
						surveyIds = new ArrayList<String>(1);
						surveyIds.add(campaignPayloadId.getSubId());
					}
					else if(CampaignPayloadId.Type.PROMPT.equals(type)) {
						promptIds = new ArrayList<String>(1);
						promptIds.add(campaignPayloadId.getSubId());
					}
				}
				
				LOGGER.info("Gathering the requested campaign registry entries.");
				campaigns.addAll(
					CampaignServices
						.instance()
						.getCampaigns(
							campaignIds,
							surveyIds,
							promptIds,
							null,
							null,
							null,
							null,
							null,
							numToSkip,
							numToReturn - streams.size()
						));
				LOGGER.info("Found " + campaigns.size() + " campaigns.");
			}
			
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {
		
		LOGGER.info("Responding to an OMH registry read request");

		// If either request has failed, set the response's status code.
		if(isFailed()) {
			if(
				ErrorCode
					.SYSTEM_GENERAL_ERROR
					.equals(getAnnotator().getErrorCode())) {
					
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			else {
				httpResponse.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			}
			
			// Then, force the appropriate request to respond.
			super.respond(httpRequest, httpResponse, null);
			return;
		}

		// Expire the response, but this may be a bad idea.
		expireResponse(httpResponse);
		
		// Set the content type to JSON.
		httpResponse.setContentType("application/json");
		
		// Connect a stream to the response.
		OutputStream outputStream;
		try {
			outputStream = getOutputStream(httpRequest, httpResponse);
		}
		catch(IOException e) {
			LOGGER.warn("Could not connect to the output stream.", e);
			return;
		}

		// Create the generator that will stream to the requester.
		JsonGenerator generator;
		try {
			generator = JSON_FACTORY.createJsonGenerator(outputStream);
		}
		catch(IOException generatorException) {
			LOGGER.error(
				"Could not create the JSON generator.",
				generatorException);
			
			try {
				outputStream.close();
			}
			catch(IOException streamCloseException) {
				LOGGER.warn(
					"Could not close the output stream.",
					streamCloseException);
			}
			
			return;
		}
		
		try {
			// Start the JSON output.
			generator.writeStartArray();
			
			// For each observer,
			for(String observerId : streams.keySet()) {
				for(Stream stream : streams.get(observerId)) {
					// Start this "payload ID's" object.
					generator.writeStartObject();
					
					// Output the chunk size which will be the same for all 
					// observers.
					generator.writeNumberField(
						"chunk_size", 
						StreamReadRequest.MAX_NUMBER_TO_RETURN);
					
					// There are no external IDs yet. This may change to link   
					// to observer/read, but there are some discrepancies in 
					// the parameters.
					
					// Set the local timezone as authoritative.
					generator.writeBooleanField(
						"local_tz_authoritative",
						true);
					
					// Set the summarizable as false for the time being.
					generator.writeBooleanField("summarizable", false);

					// Set the payload ID.
					StringBuilder payloadIdBuilder = 
						new StringBuilder("omh:ohmage:observer:");
					payloadIdBuilder.append(observerId).append(':');
					payloadIdBuilder.append(stream.getId());
					generator.writeStringField(
						"payload_id", 
						payloadIdBuilder.toString());
					
					// Set the payload version.
					generator.writeStringField(
						"payload_version", 
						String.valueOf(stream.getVersion()));
					
					// Set the payload definition.
					generator.writeObjectField(
						"payload_definition", 
						stream.getSchema().readValueAsTree());

					// End this "payload ID's" object.
					generator.writeEndObject();
				}
			}
			
			// For each campaign,
			for(Campaign campaign : campaigns) {
				// Start building the payload ID.
				StringBuilder payloadIdBuilder = 
					new StringBuilder("omh:ohmage:campaign:");
				payloadIdBuilder.append(campaign.getId());
				
				// For now, there is no concept of querying the entire 
				// campaign. Instead, the coarsest-grained query will be that 
				// of survey responses.
				/*
				// First, do the campaign without any surveys or prompts.
				generator.writeStartObject();
				
				// Output the chunk size which will be the same for all 
				// observers.
				generator.writeNumberField(
					"chunk_size", 
					StreamReadRequest.MAX_NUMBER_TO_RETURN);
				
				// There are no external IDs yet. This may change to link   
				// to observer/read, but there are some discrepancies in 
				// the parameters.
				
				// Set the local timezone as authoritative.
				generator.writeBooleanField(
					"local_tz_authoritative",
					true);
				
				// Set the summarizable as false for the time being.
				generator.writeBooleanField("summarizable", false);

				// Set the payload ID.
				generator.writeStringField(
					"payload_id", 
					payloadIdBuilder.toString());
				
				// Set the payload version. For now, all campaigns have the
				// same version, 1.
				generator.writeStringField(
					"payload_version", 
					"1");
				
				// Set the payload definition.
				generator.writeObjectField(
					"payload_definition", 
					// TODO: Write the definition for all surveys.
					null);

				// End the campaign's object.
				generator.writeEndObject();
				*/
				
				// For each survey,
				Map<String, Survey> surveys = campaign.getSurveys();
				for(String surveyId : surveys.keySet()) {
					// Write the survey.
					Survey survey = surveys.get(surveyId);
					generator.writeStartObject();
					
					// Output the chunk size which will be the same for all 
					// observers.
					generator.writeNumberField(
						"chunk_size", 
						StreamReadRequest.MAX_NUMBER_TO_RETURN);
					
					// There are no external IDs yet. This may change to
					// link to observer/read, but there are some
					// discrepancies in the parameters.
					
					// Set the local timezone as authoritative.
					generator.writeBooleanField(
						"local_tz_authoritative",
						true);
					
					// Set the summarizable as false for the time being.
					generator.writeBooleanField("summarizable", false);
					
					// Set the payload ID.
					StringBuilder surveyPayloadIdBuilder = 
						new StringBuilder(payloadIdBuilder);
					surveyPayloadIdBuilder.append(":survey_id:");
					surveyPayloadIdBuilder.append(survey.getId());
					generator.writeStringField(
						"payload_id", 
						surveyPayloadIdBuilder.toString());
					
					// Set the payload version. For now, all surveys have 
					// the same version, 1.
					generator.writeStringField(
						"payload_version", 
						"1");
					
					// Set the payload definition.
					generator.writeFieldName("payload_definition"); 
					survey.toConcordia(generator, null);

					// End the campaign's object.
					generator.writeEndObject();
					
					// For each prompt in the survey,
					for(SurveyItem surveyItem :
							survey.getSurveyItems().values()) {
						
						if(surveyItem instanceof Message) {
							continue;
						}
						
						// For now, we are ignoring repeatable sets.
						if(surveyItem instanceof RepeatableSet) {
							continue;
						}
						
						// Write the survey item.
						generator.writeStartObject();
						
						// Output the chunk size which will be the same for
						// all observers.
						generator.writeNumberField(
							"chunk_size", 
							StreamReadRequest.MAX_NUMBER_TO_RETURN);
						
						// There are no external IDs yet. This may change
						// to link to observer/read, but there are some
						// discrepancies in the parameters.
						
						// Set the local timezone as authoritative.
						generator.writeBooleanField(
							"local_tz_authoritative",
							true);
						
						// Set the summarizable as false for the time
						// being.
						generator.writeBooleanField("summarizable", false);
						
						// Set the payload ID.
						StringBuilder promptPayloadIdBuilder = 
							new StringBuilder(payloadIdBuilder);
						promptPayloadIdBuilder.append(":prompt_id:");
						promptPayloadIdBuilder.append(surveyItem.getId());
						generator.writeStringField(
							"payload_id", 
							promptPayloadIdBuilder.toString());
						
						// Set the payload version. For now, all surveys have 
						// the same version, 1.
						generator.writeStringField(
							"payload_version", 
							"1");

						// If it's a repeatable set, then it will be the 
						// same as a prompt except that the data will be
						// an array of the same definition as a prompt.
						generator.writeFieldName("payload_definition"); 
						survey.toConcordia(generator, surveyItem.getId());

						// End the campaign's object.
						generator.writeEndObject();
					}
				}
			}
			
			// Compute the number of entries that have been returned and, from
			// that, compute how many of the following entries should be
			// returned.
			long numToReturnLeft = 
				numToReturn - streams.size() - campaigns.size();
			
			// Finally, add the hard-coded ones for the showcase, i.e. 
			// RunKeeper, BodyMedia, etc..
			// If all were ask for, then output all of the RunKeeper APIs.
			if(payloadId == null) {
				// Get markers for all of the APIs.
				RunKeeperApiFactory[] apiFactories = 
					RunKeeperApiFactory.values();
		
				// Compute the number of API definitions to return.
				long numFactoriesToOutput =
					(numToReturnLeft >= apiFactories.length) ?
						apiFactories.length : numToReturnLeft;
				
				for(int i = 0; i < numFactoriesToOutput; i++) {
					// Get the API's string value.
					String apiString = apiFactories[i].getApi();
					
					// Write the API's registry entry. 
					try {
						RunKeeperApiFactory
							.getApi(apiString)
							.writeRegistryEntry(generator);
					}
					catch(DomainException e) {
						LOGGER
							.warn(
								"Could not output the registry entry for the API: " +
									apiString,
								e);
					}
				}	
			}
			// If a specific RunKeeper API was asked for, 
			else if(payloadId instanceof RunKeeperPayloadId) {
				// Type cast the PayloadId object specifically to a
				// RunKeeperPayloadId.
				RunKeeperPayloadId runKeeperPayloadId =
					(RunKeeperPayloadId) payloadId;
				
				// Get the requested API string.
				String apiString = runKeeperPayloadId.getId();
				
				// Write the registry entry.
				try {
					RunKeeperApiFactory
						.getApi(apiString)
						.writeRegistryEntry(generator);
				}
				catch(DomainException e) {
					LOGGER
						.info(
							"The requested RunKeeper API does not exist: " +
								apiString);
				}
			}
			
			// End the JSON output.
			generator.writeEndArray();
		}
		catch(JsonProcessingException e) {
			LOGGER.error("The JSON could not be processed.", e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		catch(IOException e) {
			LOGGER.info(
				"The response could no longer be written to the response",
				e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		finally {
			// Flush and close the writer.
			try {
				generator.close();
			}
			catch(IOException e) {
				LOGGER.info("Could not close the generator.", e);
			}
		}
	}
}