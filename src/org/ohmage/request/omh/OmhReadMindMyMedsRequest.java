package org.ohmage.request.omh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.service.OmhServices;

public class OmhReadMindMyMedsRequest extends UserRequest{
	private static final Logger LOGGER =
		Logger.getLogger(OmhReadMindMyMedsRequest.class);
	
	private final DateTime startDate;
	private final DateTime endDate;
	private final long numToSkip;
	private final long numToReturn;
	
	// The response from the Mind My Meds request.
	private HttpResponse response = null;
	
	/**
	 * Creates a new OMH read request for Mind My Meds.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters already decoded from the HTTP request.
	 * 
	 * @param hashPassword Whether or not to hash the user's password on
	 * 					   authentication. If null, username and password are
	 * 					   not allowed for this API.
	 * 
	 * @param tokenLocation Where to search for the user's token. If null, a
	 * 						token is not allowed for this API.
	 * 
	 * @param callClientRequester Refers to the "client" parameter as the
	 * 							  "requester".
	 * 
	 * @param startDate Limits the results to only those on or after this date.
	 * 
	 * @param endDate Limits the results to only those on or before this date.
	 * 
	 * @param columns Limits the data output based on the given columns.
	 * 
	 * @param numToSkip The number of responses to skip. Responses are in 
	 * 					reverse-chronological order.
	 * 
	 * @param numToReturn The number of responses to return after the required
	 * 					  responses have been skipped.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhReadMindMyMedsRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws IOException, InvalidRequestException {
		
		super(
			httpRequest,
			hashPassword,
			tokenLocation,
			parameters,
			callClientRequester);
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH read reqeust for Mind My Meds.");
		}

		this.startDate = startDate;
		this.endDate = endDate;
		this.numToSkip = numToSkip;
		this.numToReturn = numToReturn;
	}
	
	/**
	 * Creates the registry entry for GingerIO.
	 * 
	 * @param generator The generator to use to write the definition.
	 * 
	 * @throws JsonGenerationException There was an error creating the 
	 * 								   JSON.
	 * 
	 * @throws IOException There was an error writing to the generator.
	 */
	public static void writeRegistryEntry(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException {
		
		// Root of the definition
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
		generator.writeStringField(
			"payload_id", 
			"urn:mind_my_meds");
		
		// Set the payload version. For now, all surveys have 
		// the same version, 1.
		generator.writeStringField(
			"payload_version", 
			"1");
		
		// Set the payload definition.
		generator.writeFieldName("payload_definition"); 
		toConcordia(generator);

		// End the root of the definition.
		generator.writeEndObject();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read request for Mind My Meds.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}

		try {
			// TODO: Verify that the user is allowed to query this data through
			// ohmage.
			// This is being ignored for now because there is no "user" 
			// parameter. When the "user" parameter is added, the ACL states
			// that the requesting user must be a supervisor in any campaign to
			// which the user is a participant.
			// Note: This should not be pushed to a public server because, by
			// default everyone is a participant in every campaign. Therefore,
			// anyone who manages to elevate their privileges to supervisor of
			// a campaign will be able to execute this call against everyone
			// else in the system.
			
			// Get the authentication information from the database.
			LOGGER
				.info(
					"Getting the authentication credentials for Mind My Meds.");
			Map<String, String> mindMyMedsCredentials =
				OmhServices.instance().getCredentials("mind_my_meds");
			
			// Retrieve the users's Mind My Meds username.
			String mmmUsername = 
				mindMyMedsCredentials
					.get(getUser().getUsername() + "_username");
			if(mmmUsername == null) {
				throw new ServiceException(
					"This user doesn't have a Mind My Meds username: " +
						getUser().getUsername());
			}

			// Retrieve the users's Mind My Meds password.
			String mmmPassword = 
				mindMyMedsCredentials
					.get(getUser().getUsername() + "_password");
			if(mmmPassword == null) {
				throw new ServiceException(
					"This user doesn't have a Mind My Meds password: " +
						getUser().getUsername());
			}
			
			// Forward the call to the Mind My Meds server.
			HttpClient client = new DefaultHttpClient();
			
			
			// Build the URL.
			StringBuilder urlBuilder =
				new StringBuilder("http://www.mindmymeds.org/omh/v1.0/read?");
			
			// Add the credentials to the parameter list.
			//HttpParams params = new BasicHttpParams();
			urlBuilder.append(InputKeys.USER).append('=').append(mmmUsername);
			urlBuilder
				.append('&')
				.append(InputKeys.PASSWORD)
				.append('=')
				.append(mmmPassword);
			
			// If the start date was given, add it.
			if(startDate != null) {
				urlBuilder
					.append('&')
					.append(InputKeys.OMH_START_TIMESTAMP) 
					.append('=')
					.append(ISODateTimeFormat.dateTime().print(startDate));
			}

			// If the end date was given, add it.
			if(endDate != null) {
				urlBuilder
					.append('&')
					.append(InputKeys.OMH_END_TIMESTAMP)
					.append('=')
					.append(ISODateTimeFormat.dateTime().print(endDate));
			}
			
			// If the number to skip isn't zero, add it.
			if(numToSkip != 0) {
				urlBuilder
					.append('&')
					.append(InputKeys.OMH_NUM_TO_SKIP)
					.append('=')
					.append(numToSkip);
			}
			
			// There may be different limits on the number to return, so we
			// always add it.
			urlBuilder
				.append('&')
				.append(InputKeys.OMH_NUM_TO_RETURN)
				.append('=')
				.append(numToReturn);
			
			// Add the parameters to the request.
			//get.setParams(params);

			// Create the GET with the built URL.
			HttpGet get = new HttpGet(urlBuilder.toString());
			
			try {
				response = client.execute(get);
			}
			catch(ClientProtocolException e) {
				throw new ServiceException(
					"There was an error in the HTTP protocol.",
					e);
			}
			catch(IOException e) {
				throw new ServiceException(
					"There was an error communicating with the Mind My Meds server.",
					e);
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
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
		else {
			// Set the status code from the response to our response.
			httpResponse.setStatus(response.getStatusLine().getStatusCode());
			
			// Get the input stream from the Mind My Meds response.
			InputStream inputStream;
			try {
				inputStream = response.getEntity().getContent();
			}
			catch(IllegalStateException e) {
				LOGGER
					.error(
						"Error getting the Mind My Minds response stream.",
						e);
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			catch(IOException e) {
				LOGGER
					.error(
						"Error reading from the Mind My Minds response stream.",
						e);
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			// Get our output stream back to the client.
			OutputStream outputStream; 
			try {
				outputStream = getOutputStream(httpRequest, httpResponse);
			}
			catch(IOException e) {
				LOGGER.error("Error getting our output stream.", e);
				try {
					inputStream.close();
				}
				catch(IOException errorClosingMmmConnection) {
					LOGGER
						.error(
							"Could not close the input stream to the Mind My Meds Server.",
							errorClosingMmmConnection);
				}
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			
			try {
				int bytesRead;
				byte[] chunk = new byte[4096];
				while((bytesRead = inputStream.read(chunk)) != -1) {
					outputStream.write(chunk, 0, bytesRead);
				}
			}
			catch(IOException e) {
				LOGGER
					.error(
						"There was an error shuffling the contens of the response.");
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			finally {
				try {
					inputStream.close();
				}
				catch(IOException e) {
					LOGGER
						.info(
							"Could not close the input stream to the Mind My Meds Server.",
							e);
				}
				
				try {
					outputStream.close();
				}
				catch(IOException e) {
					LOGGER.info("Could not close the output stream.", e);
				}
			}
		}
	}
	
	/**
	 * Generates the Concordia schema for this Result object.
	 * 
	 * @param generator The generator to use to write the definition.
	 * 
	 * @return The 'generator' that was passed in to facilitate chaining.
	 * 
	 * @throws JsonGenerationException There was a problem generating the
	 * 								   JSON.
	 * 
	 * @throws IOException There was a problem writing to the generator.
	 */
	private static JsonGenerator toConcordia(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException {
		
		// Start the definition.
		generator.writeStartObject();
		
		// The data will always be a JSON object.
		generator.writeStringField("type", "object");
		generator.writeArrayFieldStart("schema");
		
		generator.writeStartObject();
		generator.writeStringField("name", "medicine_name");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		generator.writeStartObject();
		generator.writeStringField("name", "reminder_sent");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		generator.writeStartObject();
		generator.writeStringField("name", "response");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		generator.writeStartObject();
		generator.writeStringField("name", "response_date");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		generator.writeStartObject();
		generator.writeStringField("name", "doctor");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		generator.writeStartObject();
		generator.writeStringField("name", "instruction");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		generator.writeStartObject();
		generator.writeStringField("name", "dose_info");
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		// End the overall schema array.
		generator.writeEndArray();
		
		// End the definition.
		generator.writeEndObject();
		
		// Return the generator.
		return generator;
	}
}