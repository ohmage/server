package org.ohmage.domain;

import java.io.IOException;
import java.io.StringWriter;
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
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamUploadRequest;
import org.ohmage.util.StringUtils;

/**
 * The observer's payload ID.
 *
 * @author John Jenkins
 */
public class ObserverPayloadId implements PayloadId {
	private static final JsonFactory JSON_FACTORY = 
		// Create a mapping JSON factory.
		(new MappingJsonFactory())
			// Ask the writer to always close the content even when there is an
			// error.
			.configure(Feature.AUTO_CLOSE_JSON_CONTENT, true);
	
	private final String observerId;
	private final String streamId;
	
	/**
	 * Creates a new Observer payload ID.
	 * 
	 * @param observerId The observer's ID.
	 * 
	 * @param streamId The stream's ID.
	 * 
	 * @throws DomainException The observer or stream ID is null or only 
	 * 						   whitespace.
	 */
	public ObserverPayloadId(
			final String observerId, 
			final String streamId)
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(observerId)) {
			throw new DomainException(
				ErrorCode.OBSERVER_INVALID_ID,
				"The observer ID is null or only whitespace.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(streamId)) {
			throw new DomainException(
				ErrorCode.OBSERVER_INVALID_STREAM_ID,
				"The stream ID is null or only whitespace.");
		}
		
		this.observerId = observerId;
		this.streamId = streamId;
	}

	/**
	 * Returns the observer ID.
	 * 
	 * @return The observer ID.
	 */
	public String getObserverId() {
		return observerId;
	}

	/**
	 * Returns the stream ID.
	 * 
	 * @return The stream ID.
	 */
	public String getStreamId() {
		return streamId;
	}

	/**
	 * Creates a stream/read request.
	 * 
	 * @return A stream read request.
	 */
	@Override
	public StreamReadRequest generateReadRequest(
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
				new StreamReadRequest(
					httpRequest,
					parameters,
					true,
					TokenLocation.EITHER,
					callClientRequester,
					owner,
					observerId,
					null,
					streamId,
					version,
					startDate,
					endDate,
					null,
					false,
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

	/**
	 * Creates a stream/upload request.
	 * 
	 * @return A stream/upload request.
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
				
				// Write the stream ID.
				generator.writeStringField("stream_id", streamId);
				
				// Write the stream version.
				generator.writeNumberField("stream_version", version);
				
				// Get the metadata and validate that it is an object or null.
				JsonNode metadata = currPoint.get("metadata");
				if(metadata != null) {
					if(! metadata.isObject()) {
						throw 
							new DomainException(
								ErrorCode.OMH_INVALID_DATA,
								"The metadata for a data point is not an object.");
					}
					
					// Write the metadata.
					generator.writeObjectField("metadata", metadata);
				}
				
				// Get the data and add it.
				JsonNode pointData = currPoint.get("data");
				if(pointData == null) {
					throw
						new DomainException(
							ErrorCode.OMH_INVALID_DATA,
							"The data is missing.");
				}
				else {
					generator.writeObjectField("data", pointData);
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
				new StreamUploadRequest(
					httpRequest,
					parameters,
					observerId,
					null,
					resultWriter.toString());
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