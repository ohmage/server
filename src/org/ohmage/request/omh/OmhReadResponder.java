package org.ohmage.request.omh;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.ohmage.exception.DomainException;

/**
 * This interface defines the methods for requests that can be used to respond
 * to an OMH read request. Presently, this includes survey_response/read and
 * stream/read.
 *
 * @author John Jenkins
 */
public interface OmhReadResponder {
	/**
	 * Returns the number of data points. This will always be 0 if the request
	 * has not been serviced or if the request has failed.
	 * 
	 * @return The number of data points being returned.
	 */
	long getNumDataPoints();
	
	/**
	 * The generator will already be set at the data array for the response. 
	 * The implementer is responsible for generating the series of JSON objects
	 * or arrays of data as defined by its schema. The data's schema is based 
	 * on the one it would have returned to registry/read.
	 * 
	 * @param generator The generator to use to write the data.
	 * 
	 * @throws JsonGenerationException There was an error generating the JSON.
	 * 
	 * @throws IOException There was an error writing to the generator.
	 * 
	 * @throws DomainException There was an error while aggregating / 
	 * 						   generating the data.
	 */
	void respond(
		final JsonGenerator generator) 
		throws JsonGenerationException, IOException, DomainException;
}