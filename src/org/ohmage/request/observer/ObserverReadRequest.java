package org.ohmage.request.observer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;

/**
 * Reads the definition of observers.<br />
 * <br />
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OBSERVER_ID}</td>
 *     <td>Limits the results to only those observers with this ID.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OBSERVER_VERSION}</td>
 *     <td>Limits the results to only those observers with this version.</td>
 *     <td>false</td>
 *   </tr>
 * </table> 
 * 
 * @author John Jenkins
 */
public class ObserverReadRequest extends Request {
	private static final Logger LOGGER =
		Logger.getLogger(ObserverReadRequest.class);

	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	/**
	 * The marshaller to return the XML version of the observer definitions.
	 */
	private static final Marshaller XML_MARSHALLER;
	static {
		try {
			XML_MARSHALLER =
				JAXBContext
					.newInstance(Observer.class)
					.createMarshaller();
		}
		catch(JAXBException e) {
			throw
				new IllegalStateException(
					"There was a problem creating the marshaller.",
					e);
		}
	}

	/**
	 * The maximum number of records that can be returned.
	 */
	public static final long MAX_NUMBER_TO_RETURN = 100;
	
	private final String observerId;
	private final Long observerVersion;
	
	private final long numToSkip;
	private final long numToReturn;
	
	private final boolean xml;
	
	private final Collection<Observer> observers = new LinkedList<Observer>();
	
	/**
	 * Creates an observer read request.
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
	public ObserverReadRequest(
			final HttpServletRequest httpRequest,
			final boolean xml) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, null);
		
		String tObserverId = null;
		Long tObserverVersion = null;
		
		long tNumToSkip = 0;
		long tNumToReturn = MAX_NUMBER_TO_RETURN; 
		
		if(! isFailed()) {
			LOGGER.info("Creating an observer read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.OBSERVER_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_ID,
						"Multiple observer IDs were given.");
				}
				else if(t.length == 1) {
					tObserverId = ObserverValidators.validateObserverId(t[0]);
				}
				
				t = getParameterValues(InputKeys.OBSERVER_VERSION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_VERSION,
						"Multiple observer versions were given.");
				}
				else if(t.length == 1) {
					tObserverVersion = 
						ObserverValidators.validateObserverVersion(t[0]);
				}
				
				if(xml &&
					((tObserverId == null) || (tObserverVersion == null))) {
					
					throw
						new ValidationException(
							ErrorCode.OBSERVER_INVALID_ID, 
							"When requesting XML output, an observer ID and " +
								"version must be given.");
				}
				
				t = getParameterValues(InputKeys.NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
						"Multiple skip counts were given: " + 
							InputKeys.NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tNumToSkip = ObserverValidators.validateNumToSkip(t[0]);
				}
				
				t = getParameterValues(InputKeys.NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
						"Multiple return counts were given: " + 
							InputKeys.NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tNumToReturn = 
						ObserverValidators
							.validateNumToReturn(t[0], MAX_NUMBER_TO_RETURN);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		observerId = tObserverId;
		observerVersion = tObserverVersion;
		
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		this.xml = xml;
	}
	
	/**
	 * Returns an unmodifiable copy of the results. If {@link #service()} has
	 * not been called on this request, this will be an empty list.
	 * 
	 * @return The list of results generated thus far.
	 */
	public Collection<Observer> getResults() {
		return Collections.unmodifiableCollection(observers);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an observer read request.");
		
		try {
			// Get all observers visible to the requesting user based on the
			// parameters.
			LOGGER.info("Gathering the observers.");
			observers
				.addAll(
					ObserverServices
						.instance()
						.getObservers(
							observerId, 
							observerVersion, 
							numToSkip, 
							numToReturn
						)
				);
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

		try {
			// Return the result as XML. If multiple observer definitions are
			// requested, 
			if(xml) {
				if(observers.size() == 0) {
					httpResponse
						.sendError(
							HttpServletResponse.SC_BAD_REQUEST, 
							"The observer ID-version pair is unknown.");
				}
				else {
					try {
						XML_MARSHALLER
							.marshal(
								observers.iterator().next(),
								outputStream);
					}
					catch(JAXBException e) {
						LOGGER
							.error(
								"There was an error marshalling the " +
									"observer definition as XML.",
								e);
						httpResponse
							.sendError(
								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"There was an unspecified internal error.");
					}
				}
			}
			// Otherwise, return it as JSON.
			else {
				// Create the generator that will stream to the requester.
				JsonGenerator generator;
				try {
					generator = JSON_FACTORY.createJsonGenerator(outputStream);
				}
				catch(IOException e) {
					LOGGER.error("Could not create the JSON generator.", e);
					return;
				}
				
				try {
					// Start the resulting object.
					generator.writeStartObject();
					
					// Add the result to the object.
					generator.writeObjectField("result", "success");
					
					// Output the data for each observer.
					generator.writeArrayFieldStart("data");
					for(Observer observer : observers) {
						// Write the observer to the output.
						observer.toJson(generator);
					}
					generator.writeEndArray();
					
					// End the overall object.
					generator.writeEndObject();
				}
				catch(JsonProcessingException e) {
					LOGGER.error("The JSON could not be processed.", e);
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
		catch(IOException e) {
			LOGGER.info(
				"The response could no longer be written to the response",
				e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		finally {
			try {
				outputStream.close();
			}
			catch(IOException e) {
				LOGGER.info("Could not close the output stream.", e);
			}
		}
	}
}